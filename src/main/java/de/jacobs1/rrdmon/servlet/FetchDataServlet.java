package de.jacobs1.rrdmon.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rrd4j.ConsolFun;

import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import de.jacobs1.rrdmon.config.ApplicationConfig;
import de.jacobs1.rrdmon.config.DataSource;

/**
 * @author  henning
 */
public class FetchDataServlet extends HttpServlet {

    private LoadingCache<String, String> dataCache;

    public FetchDataServlet() {
        dataCache = CacheBuilder.newBuilder().maximumSize(10000).concurrencyLevel(16)
                                .expireAfterWrite(2, TimeUnit.SECONDS).build(new CacheLoader<String, String>() {
                                        @Override
                                        public String load(final String key) throws Exception {
                                            return getDataJson(key);
                                        }
                                    });
    }

    private String getDataJson(final String timeSpec) throws IOException {

        final ApplicationConfig config = ApplicationConfig.getInstance();

        final StringBuilder out = new StringBuilder();

        // we use a 2sec delay to make sure all data is already collected for our timespan
        final long now = (System.currentTimeMillis() / 1000) - 2;

        // 10m, 1h, 1d, 1w
        final long startTime = now + GraphImageServlet.parseTime(timeSpec);

        out.append('{');

        boolean firstVal = true;

        DataSource dataSource;
        double val;
        int i;

        String doubleStr;
        for (final String dsName : config.getDataSourceNames()) {

            dataSource = config.getDataSource(dsName);

            if (!firstVal) {
                out.append(',');
            }

            out.append('"');
            out.append(dsName);
            out.append("\":[");

            i = 0;
            for (DataSource.RrdDataSourceEntry entry : dataSource.getRrdDataSources()) {
                val = getRrdValue(entry.getRrdPath(), entry.getDsName(), startTime, now);
                if (i > 0) {
                    out.append(',');
                }

                if (Double.isNaN(val) || Double.isInfinite(val)) {
                    out.append("null");
                } else {
                    doubleStr = String.format("%.6f", val);
                    out.append(doubleStr);
                }

                i++;
            }

            out.append(']');
            firstVal = false;

        }

        out.append('}');

        return out.toString();
    }

    private Double getRrdValue(final String rrdPath, final String dsName, final long startTime, final long now)
        throws IOException {

        RrdDb rrd;
        FetchRequest fetchRequest;
        FetchData data;
        double val;
        rrd = new RrdDb(rrdPath, true);
        try {
            fetchRequest = rrd.createFetchRequest(ConsolFun.AVERAGE, startTime, now);
            data = fetchRequest.fetchData();
            val = data.getAggregate(dsName, ConsolFun.AVERAGE);
        } finally {
            rrd.close();
        }

        return val;
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
        Locale.setDefault(Locale.US);

        final String[] path = request.getRequestURI().split("/");
        String timeSpec = "1m";

        for (int i = 1; i < path.length; i++) {
            final String pathElement = path[path.length - 1 - i];
            switch (i) {

                case 1 :
                    timeSpec = pathElement;
                    break;
            }
        }

        response.setContentType("application/json");

        final PrintWriter out = response.getWriter();
        try {
            out.write(dataCache.get(timeSpec));
        } catch (ExecutionException ex) {
            throw Throwables.propagate(ex);
        }

    }
}
