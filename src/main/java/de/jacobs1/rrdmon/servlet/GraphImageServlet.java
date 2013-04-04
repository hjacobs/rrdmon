package de.jacobs1.rrdmon.servlet;

import java.awt.Color;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rrd4j.ConsolFun;

import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;

import de.jacobs1.rrdmon.config.ApplicationConfig;
import de.jacobs1.rrdmon.config.DataSource;

/**
 * @author  henning
 */
public class GraphImageServlet extends HttpServlet {

    private static final int SECOND_AGO = -1;
    private static final int MINUTE_AGO = -60;
    private static final int HOUR_AGO = -3600;
    private static final int DAY_AGO = HOUR_AGO * 24;
    private static final int WEEK_AGO = DAY_AGO * 7;

    public static byte[] graph(final DataSource dataSource, final long startTime, final long endTime, final int width,
            final int height) throws IOException {

        final RrdGraphDef graphDef = new RrdGraphDef();
        graphDef.setImageFormat("png");
        graphDef.setTimeSpan(startTime, endTime);

        String firstDsName = null;
        for (DataSource.RrdDataSourceEntry entry : dataSource.getRrdDataSources()) {
            if (firstDsName == null) {
                firstDsName = entry.getName();
            }

            graphDef.datasource(entry.getName(), entry.getRrdPath(), entry.getDsName(), ConsolFun.AVERAGE);
        }

        for (DataSource.ExprDataSourceEntry entry : dataSource.getExprDataSources()) {
            graphDef.datasource(entry.getName(), entry.getRpnExpression());
        }

        boolean hasLineOrArea = false;
        for (DataSource.GraphArea area : dataSource.getAreas()) {
            hasLineOrArea = true;
            if (area.isStack()) {
                graphDef.stack(area.getSrcName(), area.getColor(), area.getLegend());
            } else {
                graphDef.area(area.getSrcName(), area.getColor(), area.getLegend());
            }
        }

        for (DataSource.GraphLine line : dataSource.getLines()) {
            hasLineOrArea = true;
            if (line.isStack()) {
                graphDef.stack(line.getSrcName(), line.getColor(), line.getLegend());
            } else {
                graphDef.line(line.getSrcName(), line.getColor(), line.getLegend(), line.getWidth());
            }
        }

        if (!hasLineOrArea) {
            graphDef.line(firstDsName, new Color(0xFF, 0, 0), null, 2);
        }

        String statDs = firstDsName;
        if (dataSource.getStatDsName() != null) {
            statDs = dataSource.getStatDsName();
        }

        graphDef.gprint(statDs, ConsolFun.MIN, "%10.2lf MIN");
        graphDef.gprint(statDs, ConsolFun.AVERAGE, "%10.2lf AVG");
        graphDef.gprint(statDs, ConsolFun.MAX, "%10.2lf MAX");
        if (dataSource.getVerticalLabel() != null) {
            graphDef.setVerticalLabel(dataSource.getVerticalLabel());
        }

        graphDef.setAntiAliasing(true);
        graphDef.setFilename("-");
        graphDef.setShowSignature(false);
        graphDef.setWidth(width);
        graphDef.setHeight(height);

        final RrdGraph graph = new RrdGraph(graphDef);
        return graph.getRrdGraphInfo().getBytes();
    }

    public static long parseTime(final String str) {
        switch (str.charAt(str.length() - 1)) {

            case 's' :
                return Long.valueOf(str.substring(0, str.length() - 1)) * SECOND_AGO;

            case 'm' :
                return Long.valueOf(str.substring(0, str.length() - 1)) * MINUTE_AGO;

            case 'h' :
                return Long.valueOf(str.substring(0, str.length() - 1)) * HOUR_AGO;

            case 'd' :
                return Long.valueOf(str.substring(0, str.length() - 1)) * DAY_AGO;

            case 'w' :
                return Long.valueOf(str.substring(0, str.length() - 1)) * WEEK_AGO;

            default :
                return HOUR_AGO;
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param   request   servlet request
     * @param   response  servlet response
     *
     * @throws  ServletException  if a servlet-specific error occurs
     * @throws  IOException       if an I/O error occurs
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
        IOException {

        final String[] path = request.getRequestURI().split("/");

        long startTime = HOUR_AGO;
        long endTime = System.currentTimeMillis() / 1000;
        int width = 200;
        int height = 100;
        String dsName = null;

        for (int i = 1; i < path.length; i++) {
            final String pathElement = path[path.length - 1 - i];
            switch (i) {

                case 1 :

                    final String[] widthHeight = pathElement.split("x");
                    if (widthHeight.length == 2) {
                        try {
                            width = Integer.valueOf(widthHeight[0]);
                            height = Integer.valueOf(widthHeight[1]);
                        } catch (NumberFormatException nfe) {
                            // ignore
                        }
                    }

                    break;

                case 2 :

                    // 10m, 1h, 1d, 1w
                    startTime = parseTime(pathElement);

                    break;

                case 3 :
                    dsName = pathElement;
                    break;
            }

        }

        ApplicationConfig config = null;
        try {
            config = ApplicationConfig.getInstance();
        } catch (Exception e) { }

        final DataSource dataSource = config.getDataSource(dsName);

        if (dataSource == null) {
            response.setContentType("text/plain");
            response.getOutputStream().print("Datasource not found");
            return;
        }

        final byte[] image = graph(dataSource, startTime, endTime, width, height);

        response.setContentType("image/png");

        final OutputStream outputStream = response.getOutputStream();
        try {
            outputStream.write(image);
        } finally {
            outputStream.close();
        }

    }
}
