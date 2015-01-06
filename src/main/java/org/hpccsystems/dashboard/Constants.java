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
 
    public static enum CHART_TYPES {PIE, DONUT, LINE, BAR, COLUMN, US_MAP, TABLE, STEP, SCATTER, AREA};
    
    public static final Map<CHART_TYPES, ChartConfiguration> CHART_CONFIGURATIONS = new LinkedHashMap<CHART_TYPES, ChartConfiguration>(){
        private static final long serialVersionUID = 1L;
        {
            put(CHART_TYPES.PIE, new ChartConfiguration(CHART_TYPES.PIE, "Pie", "assets/img/pie.png", "widget/pie.zul" ,"PIE", "C3_PIE"));
            put(CHART_TYPES.DONUT, new ChartConfiguration(CHART_TYPES.DONUT, "Donut", "assets/img/donut.png", "widget/pie.zul" , "DONUT", "C3_DONUT"));
            put(CHART_TYPES.LINE, new ChartConfiguration(CHART_TYPES.LINE, "Line", "assets/img/line.png", "widget/xyChart.zul", "LINE", "C3_LINE"));
            put(CHART_TYPES.BAR, new ChartConfiguration(CHART_TYPES.BAR, "Bar", "assets/img/bar.png", "widget/xyChart.zul", "BAR", "C3_BAR"));
            put(CHART_TYPES.COLUMN, new ChartConfiguration(CHART_TYPES.COLUMN, "Column", "assets/img/column.png", "widget/xyChart.zul", "COLUMN","C3_COLUMN"));
            put(CHART_TYPES.US_MAP, new ChartConfiguration(CHART_TYPES.US_MAP, "US_Map", "assets/img/geo.png", "widget/usMap.zul","CHORO" ,"C3_US_MAP"));
            put(CHART_TYPES.TABLE, new ChartConfiguration(CHART_TYPES.TABLE, "Table", "assets/img/table.png", "widget/xyChart.zul", "TABLE", "C3_TABLE"));
            put(CHART_TYPES.STEP, new ChartConfiguration(CHART_TYPES.STEP, "Step", "assets/img/bar.png", "widget/xyChart.zul", "STEP","C3_STEP"));
            put(CHART_TYPES.SCATTER, new ChartConfiguration(CHART_TYPES.SCATTER, "Scatter", "assets/img/bar.png", "widget/xyChart.zul", "SCATTER","C3_SCATTER"));
            put(CHART_TYPES.AREA, new ChartConfiguration(CHART_TYPES.AREA, "Area", "assets/img/bar.png", "widget/xyChart.zul", "AREA","C3_AREA"));
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

