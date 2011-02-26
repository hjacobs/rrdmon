package de.jacobs1.rrdmon.servlet;

import de.jacobs1.rrdmon.config.ApplicationConfig;
import de.jacobs1.rrdmon.config.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;

/**
 *
 * @author henning
 */
public class FetchDataServlet extends HttpServlet {

    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final ApplicationConfig config = ApplicationConfig.getInstance();

        final String[] path = request.getRequestURI().split("/");

        // we use a 2sec delay to make sure all data is already collected for our timespan
        final long now = (System.currentTimeMillis() / 1000) - 2;
        long startTime = now - 60; // one minute ago

        for (int i = 1; i < path.length; i++) {
            final String pathElement = path[path.length - 1 - i];
            switch (i) {
                case 1:
                    // 10m, 1h, 1d, 1w
                    startTime = now + GraphImageServlet.parseTime(pathElement);
                    break;
            }
        }

        response.setContentType("application/json");
        final PrintWriter out = response.getWriter();

        out.write('{');

        boolean firstVal = true;

        DataSource dataSource;
        RrdDb rrd;
        FetchRequest fetchRequest;
        FetchData data;
        double val;
        int i;

        String doubleStr;
        int decimalSep;
        for (final String dsName : config.getDataSourceNames()) {

            dataSource = config.getDataSource(dsName);

            if (!firstVal) {
                out.write(',');

            }
            out.write('"');
            out.write(dsName);
            out.write("\":[");

            i = 0;
            for (DataSource.RrdDataSourceEntry entry : dataSource.getRrdDataSources()) {

                rrd = new RrdDb(entry.getRrdPath(), true);
                try {
                    fetchRequest = rrd.createFetchRequest(ConsolFun.AVERAGE, startTime, now);
                    data = fetchRequest.fetchData();
                    val = data.getAggregate(entry.getDsName(), ConsolFun.AVERAGE);
                } finally {
                    rrd.close();
                }

                if (i > 0) {
                    out.write(',');
                }
                
                if (Double.isNaN(val) || Double.isInfinite(val)) {
                    out.write("null");
                } else {
                    doubleStr = String.valueOf(val);
                    decimalSep = doubleStr.indexOf('.');
                    if (doubleStr.length() - decimalSep > 6) {
                        out.write(doubleStr.substring(0, decimalSep + 6));
                    } else {
                        out.write(doubleStr);
                    }
                }
                
                i++;
            }
            out.write(']');
            firstVal = false;
        }
        out.write('}');
    }
}
