package org.hpccsystems.dashboard.common;
import java.util.HashMap;
import java.util.Map;
import org.hpccsystems.dashboard.entity.ChartDetails;
 
/**
 * Constants class is used to maintain the constant's for Dashboard project.
 *
 */
public class Constants {
	public static final String  PARENT = "PARENT";
	
	public static final String ACTIVE_DASHBOARD_ID = "activeDashboardId";
	public static final String ACTIVE_DASHBOARD_NAME = "activeDashboard";
	
	public static final String PORTLET = "portlet";
	public static final String CHART_DATA = "chartData";
	public static final String DASHBOARD = "dashboard"; 
	
	public static final String EDIT_WINDOW_DONE_BUTTON = "doneButton";
	
	public static final String DASHBOARD_ID = "dashboardId";
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
	public static final Integer TABLE_WIDGET = 4;
	
	public static final String COLUMN_DATA_TYPE = "dataType";
	public static final Integer NUMERIC_DATA = 1;
	public static final Integer STRING_DATA = 2;
	
	public static final short ReorderPotletPanels = 20;
	public static final short ResizePotletPanels = 30;
	
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
	public static final String DELETE_DASHBOARD = "You are about to delete the Active Dashboard. Do you want to proceed?";
	public static final String DELETE_DASHBOARD_TITLE = "Delete Dashboard";
	public static final String TREE_NAME = "name";
	public static final String TREE_TYPE = "type";
	public static final String TREE_DIRECTORY = "Directory";
	public static final String TREE_FILE = "File";
	public static final String TREE_IS_DIRECTORY = "isDirectory";
	public static final String DFU_FILE_RESPONSE = "DFUFileViewResponse";
	public static final String DFU_LOGICAL_FILE = "DFULogicalFile";
	public static final String NAME = "Name";
	public static final String ONE = "1";
	public static final String OS_NAME = "os.name";
	public static final String WINDOWS = "Windows";
	public static final String JAVA_TEMP_DIR = "java.io.tmpdir";
	
	public static final String CIRCUIT_APPLICATION_ID = "circuit";
	public static final String CIRCUIT_ROLE_CONFIG_CHART = "configureChart";
	public static final String CIRCUIT_ROLE_VIEW_CHART = "viewChart";
	public static final String CIRCUIT_ROLE_VIEW_DASHBOARD = "viewDashboard";
	public static final String CIRCUIT_DELETE_REQ = "/delete.do";
	public static final String CIRCUIT_CHARTLIST_REQ = "/chartList.do";
	public static final String CIRCUIT_SEARCH_REQ  = "/search.do";	
	public static final String DB_DASHBOARD_ID = "dashboard_id";
	public static final String STATUS = "status";
	public static final String STATUS_SUCCESS = "success";
	public static final String STATUS_FAIL = "failed";
	public static final String STATUS_MESSAGE = "message";
	public static final String DASHBOARD_NOT_EXIST = "Dashboard does not exist";
	public static final String RES_TEXT_TYPE_JSON = "application/json";
	public static final String CHAR_CODE = "UTF-8";
	public static final String CHARTLIST_FORMAT = "format";
	public static final String DESCRIPTION ="description";
	public static final String VALUE ="value";
	public static final String LABEL = "label";
	public static final String CHART_LIST = "chartList";
	public static final String SOURCE = "source";
	public static final String SOURCE_ID = "source_id";
	public static final String SOURCEID="sourceid"; 
	public static final String NAME_SMALL = "name";
	public static final String DASHBOARDS =  "dashboards";
	
	public static final Map<Integer, ChartDetails> CHART_MAP = new HashMap<Integer, ChartDetails>(){
		private static final long serialVersionUID = 1L;
		{
			put(BAR_CHART, new ChartDetails(BAR_CHART, "chart/Barchart_black.jpg" ,"Bar Chart", "BarChartDescription", 2, 0));
			put(LINE_CHART, new ChartDetails(LINE_CHART, "chart/Linechart_black.jpg" ,"Line Chart", "LineChartDescription", 2,0));
			put(PIE_CHART, new ChartDetails(PIE_CHART, "chart/PieChart_black.jpg" ,"Pie Chart", "PieDescription", 1, 1));
			put(TABLE_WIDGET, new ChartDetails(TABLE_WIDGET, "chart/table_black.png" ,"Table Widget", "TableWidgetDescription", 0, 0));
		}
		
	};
	
	public static final String EDIT_WINDOW_CHART_DIV = "chart";
	
	public static final Integer FILTER_MINIMUM = 0;
	public static final Integer FILTER_MAXIMUM = 1;
	
	public static final Integer EDIT_WINDOW_TYPE_DATA_SELECTION = 0;
	public static final Integer EDIT_WINDOW_TYPE_CHART = 1;
	public static final Integer EDIT_WINDOW_TYPE_TABLE = 2;

	public static final String ACTIVE_PORTLET = null;

	public static final String PARAMS = "params";
	
	public static final String JSON ="json";
	
	public Constants(){
		
	}
	
	//For Circuit project
	
	public static final String CIRCUIT_VALIDATE_REQ = "/validate.do";
	public static final String CIRCUIT_CONFIG = "config";
	public static final String CIRCUIT_DASHBOARD_ID = "dashboard_id";
	public static final String FIELD_NOT_EXIST = " is missing";
	public static final String FIELDS_NOT_EXIST = " are missing";
	public static final String LAST_UPDATED_DATE = "lastUpdatedDate";
	public static final String CHART_TITLE = "Chart Title";
	public static final String DASHBOARD_NOT_EXISTS = "The Provided Dashboard doesn't exists";
	
}
