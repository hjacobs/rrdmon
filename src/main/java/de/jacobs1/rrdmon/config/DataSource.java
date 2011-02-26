package de.jacobs1.rrdmon.config;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author henning
 */
public class DataSource {
    
    public static class RrdDataSourceEntry {
        private String name;
        private String rrdPath;
        private String dsName;

        public String getDsName() {
            return dsName;
        }

        public void setDsName(final String dsName) {
            this.dsName = dsName;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getRrdPath() {
            return rrdPath;
        }

        public void setRrdPath(final String rrdPath) {
            this.rrdPath = rrdPath;
        }
        
    }
    
    public static class ExprDataSourceEntry {
        private String name;
        private String rpnExpression;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getRpnExpression() {
            return rpnExpression;
        }

        public void setRpnExpression(final String rpnExpression) {
            this.rpnExpression = rpnExpression;
        }
    }

    public static class GraphLine {
        private String srcName;
        private Paint color;
        private String legend;
        private float width;
        private boolean stack;

        public Paint getColor() {
            return color;
        }

        public void setColor(final Paint color) {
            this.color = color;
        }

        public String getLegend() {
            return legend;
        }

        public void setLegend(final String legend) {
            this.legend = legend;
        }

        public String getSrcName() {
            return srcName;
        }

        public void setSrcName(final String srcName) {
            this.srcName = srcName;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(final float width) {
            this.width = width;
        }

        public boolean isStack() {
            return stack;
        }

        public void setStack(boolean stack) {
            this.stack = stack;
        }




    }

    public  static class GraphArea {
        private String srcName;
        private Paint color;
        private String legend;
        private boolean stack;

        public Paint getColor() {
            return color;
        }

        public void setColor(final Paint color) {
            this.color = color;
        }

        public String getLegend() {
            return legend;
        }

        public void setLegend(final String legend) {
            this.legend = legend;
        }

        public String getSrcName() {
            return srcName;
        }

        public void setSrcName(final String srcName) {
            this.srcName = srcName;
        }

        public boolean isStack() {
            return stack;
        }

        public void setStack(boolean stack) {
            this.stack = stack;
        }

        


    }

    private String name;
    private String verticalLabel;
    private String statDsName;

    private List<RrdDataSourceEntry> rrdDataSources = new ArrayList<RrdDataSourceEntry>();
    private List<ExprDataSourceEntry> exprDataSources = new ArrayList<ExprDataSourceEntry>();
    private List<GraphLine> lines = new ArrayList<GraphLine>();
    private List<GraphArea> areas = new ArrayList<GraphArea>();

    public List<ExprDataSourceEntry> getExprDataSources() {
        return exprDataSources;
    }

    public void setExprDataSources(final List<ExprDataSourceEntry> exprDataSources) {
        this.exprDataSources = exprDataSources;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVerticalLabel() {
        return verticalLabel;
    }

    public void setVerticalLabel(final String verticalLabel) {
        this.verticalLabel = verticalLabel;
    }

    public String getStatDsName() {
        return statDsName;
    }

    public void setStatDsName(final String statDsName) {
        this.statDsName = statDsName;
    }





    public List<RrdDataSourceEntry> getRrdDataSources() {
        return rrdDataSources;
    }

    public void setRrdDataSources(final List<RrdDataSourceEntry> rrdDataSources) {
        this.rrdDataSources = rrdDataSources;
    }

    public List<GraphLine> getLines() {
        return lines;
    }

    public void setLines(final List<GraphLine> lines) {
        this.lines = lines;
    }

    public List<GraphArea> getAreas() {
        return areas;
    }

    public void setAreas(final List<GraphArea> areas) {
        this.areas = areas;
    }

    




}
