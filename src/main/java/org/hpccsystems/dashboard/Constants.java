package org.hpccsystems.dashboard;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;

public class Constants {

	public static final String EXCEPTION = "EXCEPTION - {}";
	
	//Events
	public static final String ON_DELTE_DASHBOARD = "onDeleteDashboard";
	public static final String ON_DRAW_CHART = "onDrawChart";
	
	public static final String HIPIE_RAW_DATASET = "RawDataset";
	public static final String USER_CREDENTIAL = "userCredential";
    public static final String ON_ADD_DASHBOARD = "onAddDashboard";
    public static final String DASHBOARD = "dashboard";
    public static final String UNABLE_TO_FETCH_DATA = "Unable to fetch Hpcc data";
    public static final String HPCC_CONNECTION = "hpccConnection";
    public static final String MEASURE = "measure";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String FILTER = "filter";
    public static final String DOT=".";
    public static final String COMMA=" , ";

    // Notification Types
    public static final String ERROR_NOTIFICATION = "error";
    // Notification positions
    public static final String POSITION_CENTER = "middle_center";
    
    public static final String WIDGET_CONFIG = "widgetWrapper";
    public static final String ON_SELECT = "onSelect";
    public static final String HPCC_ID = "hpccID";
 
    public static final Map<String, ChartConfiguration> CHART_CONFIGURATIONS = new LinkedHashMap<String, ChartConfiguration>(){
        private static final long serialVersionUID = 1L;
        {
            put(ChartTypes.PIE.getChartCode(), new ChartConfiguration(ChartTypes.PIE.getChartCode(), "Pie", "assets/img/charts/pie.png", "widget/pie.zul" ,"C3_PIE"));
            put(ChartTypes.DONUT.getChartCode(), new ChartConfiguration(ChartTypes.DONUT.getChartCode(), "Donut", "assets/img/charts/donut.png", "widget/pie.zul" , "C3_DONUT"));
            put(ChartTypes.LINE.getChartCode(), new ChartConfiguration(ChartTypes.LINE.getChartCode(), "Line", "assets/img/charts/line.png", "widget/xyChart.zul", "C3_LINE"));
            put(ChartTypes.BAR.getChartCode(), new ChartConfiguration(ChartTypes.BAR.getChartCode(), "Bar", "assets/img/charts/bar.png", "widget/xyChart.zul","C3_BAR"));
            put(ChartTypes.COLUMN.getChartCode(), new ChartConfiguration(ChartTypes.COLUMN.getChartCode(), "Column", "assets/img/charts/column.png", "widget/xyChart.zul","C3_COLUMN"));
            put(ChartTypes.US_MAP.getChartCode(), new ChartConfiguration(ChartTypes.US_MAP.getChartCode(), "US_Map", "assets/img/charts/geo.png", "widget/usMap.zul","CHORO"));
            put(ChartTypes.TABLE.getChartCode(), new ChartConfiguration(ChartTypes.TABLE.getChartCode(), "Table", "assets/img/charts/table.png", "widget/table.zul", "TABLE"));
            put(ChartTypes.STEP.getChartCode(), new ChartConfiguration(ChartTypes.STEP.getChartCode(), "Step", "assets/img/charts/step.png", "widget/xyChart.zul", "C3_STEP"));
            put(ChartTypes.SCATTER.getChartCode(), new ChartConfiguration(ChartTypes.SCATTER.getChartCode(), "Scatter", "assets/img/charts/scatter.png", "widget/xyChart.zul", "C3_SCATTER"));
            put(ChartTypes.AREA.getChartCode(), new ChartConfiguration(ChartTypes.AREA.getChartCode(), "Area", "assets/img/charts/area.png", "widget/xyChart.zul","C3_AREA"));
        }
    };
    public static final String ACTIVE_DASHBOARD = "ActiveDashboard";
    
    public enum AGGREGATION {
        SUM ("SUM"),
        COUNT ("COUNT"),
        MIN ("MIN"),
        MAX ("MAX"),
        AVG ("AVG"),
        NONE ("NONE");

        private final String name;       

        private AGGREGATION(String s) {
            name = s;
        }
        public String toString(){
           return name;
        }

    }
    
 // Style class names
    public static final String STYLE_POPUP = "popup";

    public static final String URL = "url";

    public static final String TARGET = "target";

    
    
}

