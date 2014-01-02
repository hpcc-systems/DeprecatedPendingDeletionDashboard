package org.hpccsystems.dashboard.common;

import java.util.HashMap;
import java.util.Map;
/**
 * Constants class is used to maintain the constant's for Dashboard project.
 *
 */
public class Constants {
	public static final String  PARENT = "PARENT";
	
	public static final String ACTIVE_DASHBOARD_ID = "activeDashboardId";
	public static final String ACTIVE_DASHBOARD_NAME = "activeDashboard";
	public static final String ACTIVE_PORTLET = "activePortlet";
	
	public static final String DASHBOARD_ID = "dashboardId";
	public static final String DASHBOARD_NAME = "dashboardName";
	public static final String DASHBOARD_LAYOUT = "dashboardLayout"; 
	public static final String DASHBOARD_LIST = "dashboardList";
	
	public static final String NAVBAR = "navBar";
	
	public static final String MAX_DASHBOARD_ID = "maxDashboardId"; 
	
	public static final String LAYOUT_1X2 = "1X2";
	public static final String LAYOUT_2X2 = "2X2";
	public static final String LAYOUT_3X3 = "3X3";
	
	public static final String CHART_TYPE = "chartType";
	public static final String ERROR = "ERROR";
	
	public static final Integer BAR_CHART = 1;
	public static final Integer LINE_CHART = 2;
	public static final Integer PIE_CHART = 3;
	
	public static final String COLUMN_DATA_TYPE = "dataType";
	public static final Integer NUMERIC_DATA = 1;
	public static final Integer STRING_DATA = 2;
	
	public static final short NonEmptyPortChild = 10;
	public static final short ReorderPotletPanels = 20;
	public static final short ResizePotletPanels = 30;
	public static final short updateNonEmptyPortletCnt = 40;
	public static final short appendPortelChidren = 50;
	public static final short hideShowAddPanelIcon = 60;
	
	public static final String STATE_DELETE = "D";
	public static final String STATE_LIVE_CHART = "L";
	public static final String STATE_GRAYED_CHART = "G";
	public static final String STATE_EMPTY = "E";
	public static final String ACTIVE_FLAG =  "Y";
	public static final String INACTIVE_FLAG =  "N";
	public static final String ERROR_RETRIEVE_COLUMNS ="Unable to retrive the column schema from HPCC server.Please provide a valid Sql";
	public static final String ERROR_HPCC_SERVER ="Unable to connect the Server.Please provide the valid Widget Information.";
	public static final String COLUMN_NAME = "getColumnSchema::userName:";
	public static final String EDIT_URL =  "getColumnSchema::url:";
	public static final String EDIT_SQL =  "getColumnSchema::Sql:";
	public static final String DELETE_DASHBOARD = "Are you sure you want to delete this dashboard?";
	public static final String DELETE_DASHBOARD_TITLE = "Delete Dashboard";
	
	public static final  Map<Integer, String> CHART_URL = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;

		{
			put(BAR_CHART, "chart/Barchart_black.jpg");
			put(LINE_CHART, "chart/Linechart_black.jpg");
			put(PIE_CHART, "chart/PieChart_black.jpg");
		}
	};

	public static final String EDIT_WINDOW_CHART_DIV = "chart";
	
	public Constants(){
		
	}
	
}
