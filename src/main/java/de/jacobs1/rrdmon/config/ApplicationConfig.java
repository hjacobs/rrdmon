package de.jacobs1.rrdmon.config;

import java.awt.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author  henning
 */
public class ApplicationConfig {

    private final Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
    private static ApplicationConfig instance;
    private static boolean listeners = false;

    public static ApplicationConfig getInstance() throws Exception {
        if (instance == null) {
            instance = new ApplicationConfig();
            instance.load();
        }

        return instance;
    }

    private ApplicationConfig() {
        // private constructor
    }

    public DataSource getDataSource(final String name) {
        return dataSources.get(name);
    }

    public Set<String> getDataSourceNames() {
        return dataSources.keySet();
    }

    private void loadDataSourcesFromProperties(final Properties props) {
        String val;
        int i, j;

        i = 1;
        while ((val = props.getProperty("datasource." + i + ".name")) != null) {
            final DataSource ds = new DataSource();
            ds.setName(val);
            ds.setVerticalLabel(props.getProperty("datasource." + i + ".vlabel"));
            ds.setStatDsName(props.getProperty("datasource." + i + ".statds"));
            dataSources.put(ds.getName(), ds);

            j = 1;
            while ((val = props.getProperty("datasource." + i + ".rrd." + j)) != null) {
                final DataSource.RrdDataSourceEntry entry = new DataSource.RrdDataSourceEntry();
                final String[] parts = val.split(":", 3);
                entry.setName(parts[0]);
                entry.setRrdPath(parts[1]);
                entry.setDsName(parts[2]);
                ds.getRrdDataSources().add(entry);
                j++;
            }

            j = 1;
            while ((val = props.getProperty("datasource." + i + ".expr." + j)) != null) {
                final DataSource.ExprDataSourceEntry entry = new DataSource.ExprDataSourceEntry();
                final String[] parts = val.split(":", 2);
                entry.setName(parts[0]);
                entry.setRpnExpression(parts[1]);
                ds.getExprDataSources().add(entry);
                j++;
            }

            j = 1;
            while ((val = props.getProperty("datasource." + i + ".line." + j)) != null) {
                final DataSource.GraphLine line = new DataSource.GraphLine();
                final String[] parts = val.split(":", 5);
                line.setSrcName(parts[0]);
                line.setColor(Color.decode(parts[1]));
                line.setLegend(parts[2]);
                line.setWidth(Float.valueOf(parts[3]));
                if (parts.length > 4) {
                    line.setStack(true);
                }

                ds.getLines().add(line);
                j++;
            }

            j = 1;
            while ((val = props.getProperty("datasource." + i + ".area." + j)) != null) {
                final DataSource.GraphArea area = new DataSource.GraphArea();
                final String[] parts = val.split(":", 4);
                area.setSrcName(parts[0]);
                area.setColor(Color.decode(parts[1]));
                area.setLegend(parts[2]);
                if (parts.length > 3) {
                    area.setStack(true);
                }

                ds.getAreas().add(area);
                j++;
            }

            i++;
        }
    }

    /**
     * new style datasources.cfg file.
     */
    private void loadDataSourcesFromFile(final String filename) throws IOException {
        final FileReader fr = new FileReader(filename);
        final BufferedReader br = new BufferedReader(fr);

        final List<String> lines = new ArrayList<String>();
        String srcline;
        final Map<String, Integer> hostIndices = new HashMap<String, Integer>();
        String[] keyValue;

        while ((srcline = br.readLine()) != null) {
            if (srcline.trim().startsWith("#") || srcline.trim().isEmpty()) {

                // comment
                continue;
            }

            if (!srcline.startsWith(" ") && !srcline.startsWith("\t")) {

                // host start
                keyValue = srcline.split("\\s*=\\s*", 2);
                lines.add(keyValue[0]);
                hostIndices.put(keyValue[0], lines.size());
                if (keyValue.length > 1) {

                    // "Preprocessor": include host definition, e.g.
                    // http02 = http01 will include host definition of http01 in http02
                    for (int i = hostIndices.get(keyValue[1]); i < lines.size(); i++) {
                        if (!lines.get(i).startsWith(" ") && !lines.get(i).startsWith("\t")) {
                            break;
                        }

                        lines.add(lines.get(i));
                    }
                }
            } else {
                lines.add(srcline);
            }
        }

        DataSource ds = null;
        String host = null;
        String nameWithoutHost = null;
        for (String line : lines) {
            if (!line.startsWith(" ") && !line.startsWith("\t")) {

                // host start
                host = line.trim();
            } else {
                int indention = 0;
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == ' ') {
                        indention++;
                    } else if (line.charAt(i) == '\t') {

                        // tabs count as 4 spaces
                        indention += 4;
                    } else {
                        break;
                    }
                }

                line = line.trim();
                if (indention <= 4) {
                    keyValue = line.split("\\s*:\\s*", 2);
                    nameWithoutHost = keyValue[0];
                    ds = new DataSource();
                    ds.setName(host + "_" + nameWithoutHost);
                    ds.setVerticalLabel(keyValue[1]);
                    dataSources.put(ds.getName(), ds);
                } else {
                    keyValue = line.split("\\s*=\\s*", 2);
                    if (keyValue[0].equals("vlabel")) {
                        ds.setVerticalLabel(keyValue[1]);
                    } else if (keyValue[0].equals("statds")) {
                        ds.setStatDsName(keyValue[1]);
                    } else if (keyValue[0].equals("rrd")) {
                        final DataSource.RrdDataSourceEntry entry = new DataSource.RrdDataSourceEntry();
                        final String[] parts = keyValue[1].replaceAll("%h", host).replaceAll("%n", nameWithoutHost)
                                                          .split(":", 3);
                        entry.setName(parts[0]);
                        entry.setRrdPath(parts[1]);
                        entry.setDsName(parts[2]);
                        ds.getRrdDataSources().add(entry);
                    } else if (keyValue[0].equals("expr")) {
                        final DataSource.ExprDataSourceEntry entry = new DataSource.ExprDataSourceEntry();
                        final String[] parts = keyValue[1].split(":", 2);
                        entry.setName(parts[0]);
                        entry.setRpnExpression(parts[1]);
                        ds.getExprDataSources().add(entry);
                        if (ds.getStatDsName() == null) {

                            // set default statistics datasource to expression
                            ds.setStatDsName(parts[0]);
                        }
                    } else if (keyValue[0].equals("line")) {
                        final DataSource.GraphLine gline = new DataSource.GraphLine();
                        final String[] parts = keyValue[1].split(":", 5);
                        gline.setSrcName(parts[0]);
                        gline.setColor(Color.decode(parts[1]));
                        gline.setLegend(parts[2]);
                        gline.setWidth(Float.valueOf(parts[3]));
                        if (parts.length > 4) {
                            gline.setStack(true);
                        }

                        ds.getLines().add(gline);
                    } else if (keyValue[0].equals("area")) {
                        final DataSource.GraphArea area = new DataSource.GraphArea();
                        final String[] parts = keyValue[1].split(":", 4);
                        area.setSrcName(parts[0]);
                        area.setColor(Color.decode(parts[1]));
                        area.setLegend(parts[2]);
                        if (parts.length > 3) {
                            area.setStack(true);
                        }

                        ds.getAreas().add(area);
                    }
                }
            }
        }

        fr.close();
    }

    private void listenForChanges(final String configFile) throws Exception {
        File f = new File(configFile);

        String absolutePath = f.getAbsolutePath();
        String filePath = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));

        Path path = Paths.get(filePath);
        WatchService ws = path.getFileSystem().newWatchService();
        path.register(ws, StandardWatchEventKinds.ENTRY_MODIFY);

        WatchKey watch = null;
        while (true) {
            try {
                watch = ws.take();
            } catch (InterruptedException ex) {
                System.err.println("Interrupted");
            }

            List<WatchEvent<?>> events = watch.pollEvents();
            watch.reset();
            for (WatchEvent<?> event : events) {
                WatchEvent.Kind<Path> kind = (WatchEvent.Kind<Path>) event.kind();
                Path context = (Path) event.context();
                if (configFile.equals(context.getFileName().toString())) {
                    if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                        load();
                    }
                }
            }
        }

    }

    public void load() {
        final String configFile = "application.properties";

        final Properties props = new Properties();
        try {
            FileReader fr = new FileReader(configFile);
            props.load(fr);
            fr.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        loadDataSourcesFromProperties(props);

        String sourcesFile = "datasources.cfg";
        try {
            loadDataSourcesFromFile(sourcesFile);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // listen for changes
        if (!listeners) { // already running?

            ThreadConfig threadSourceFile = new ThreadConfig(sourcesFile);
            threadSourceFile.start();
            listeners = true;
        }
    }

    private class ThreadConfig extends Thread {

        private String configFile;

        ThreadConfig(final String configFile) {
            this.configFile = configFile;
        }

        public void run() {
            try {
                listenForChanges(configFile);
            } catch (Exception e) { }
        }
    }
}
