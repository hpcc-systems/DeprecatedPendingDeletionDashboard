package org.hpccsystems.dashboard.common;
import java.util.HashMap;
import java.util.Map; 
 
/**
 * Constants class is used to maintain the constant's for Dashboard project.
 *
 */ 
public class Constants {
    public static final String  PARENT = "PARENT";
    public static final String  BUSY_COMPONENT = "busyComponent";
    
    public static final String ACTIVE_DASHBOARD_ID = "activeDashboardId";
    public static final String ACTIVE_DASHBOARD_NAME = "activeDashboard";
    public static final String ACTIVE_DASHBOARD_ROLE = "activeDashboardRole";
    
    public static final String PORTLET = "portlet";
    public static final String LEVEL = "level";
    public static final String LEVEL_ELEMENT = "levelElement";
    public static final String FILTER = "filter";
    public static final String FIELD = "field";
    public static final String CHART_DATA = "chartData";
    public static final String MEASURE = "measure";
    public static final String ATTRIBUTE = "attribute";
    public static final String DASHBOARD = "dashboard";
    public static final String HPCC_CONNECTION = "hpccConnection";
    public static final String Hierarchical_Chart_Data = "/heirarchical_chartdata.do";
    
    public static final String EDIT_WINDOW_DONE_BUTTON = "doneButton";
    
    public static final String DASHBOARD_ID = "dashboardId";
    public static final String DASHBOARD_LIST = "dashboardList";
    public static final String DASHBOARD_ROLE = "dashboardRole";
    
    public static final String NAVBAR = "navBar";
    
    public static final String MAX_DASHBOARD_ID = "maxDashboardId";     
   
    
    public static final String CHART_TYPE = "chartType";
    public static final String ERROR = "ERROR";
    
    public static final String COLUMN_DATA_TYPE = "dataType";
    public static final String DATA_TYPE_DATASET_STRING = "Dataset";
    //Refers Filter column data type 
    public static final Integer DATA_TYPE_NUMERIC = 1;
    public static final Integer DATA_TYPE_STRING = 2;
    public static final Integer DATA_TYPE_DATASET = 5;
    public static final Integer CURRENT_DATE_NUMERIC = 3;
    public static final Integer CURRENT_DATE_STRING = 4;
    
    public static final String STATE_LIVE_CHART = "L";
    public static final String STATE_GRAYED_CHART = "G";
    public static final String STATE_EMPTY = "E";
    public static final String ACTIVE_FLAG =  "Y";
    public static final String INACTIVE_FLAG =  "N";
    public static final String COLUMN_NAME = "getColumnSchema::userName:";
    public static final String EDIT_URL =  "getColumnSchema::url:";
    public static final String EDIT_SQL =  "getColumnSchema::Sql:";
    public static final String TREE_DIRECTORY = "Directory";
    public static final String TREE_FILE = "File";
    public static final String TREE_IS_DIRECTORY = "isDirectory";
    public static final String DFU_FILE_RESPONSE = "DFUQueryResponse";
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
    public static final String REMOVE_GLOBAL_FILTERS = "Are you sure,Want to remove Dashboard filters?";
    public static final String REMOVE_GLOBAL_FILTERS_TITLE = "Remove Dashboard Filter";
    public static final String USERNAME="userName";
    public static final String CREDENTIAL="credential";
    public static final String INVALID_USER="Invalid User";
    

    public static final String EDIT_WINDOW_CHART_DIV = "chart";
    
    public static final Integer FILTER_MINIMUM = 0;
    public static final Integer FILTER_MAXIMUM = 1;
    
    public static final Integer EDIT_WINDOW_TYPE_DATA_SELECTION = 0;
    public static final Integer EDIT_WINDOW_JOIN_DATA = 1;
    public static final Integer EDIT_WINDOW_TYPE_CHART = 2;
    public static final Integer EDIT_WINDOW_TYPE_TABLE = 3;

    public static final String ACTIVE_PORTLET = null;

    public static final String PARAMS = "params";
    
    public static final String JSON ="json";
    public static final String SCORE = "score";
    public static final String PORTLET_ID = "portletId";
    public final static String HTTPS = "https://";
    public final static String HTTP = "http://";
    //For Circuit project
    
    public static final String CIRCUIT_VALIDATE_REQ = "/validate.do";
    public static final String CIRCUIT_CONFIG = "config";
    public static final String CIRCUIT_DASHBOARD_ID = "dashboard_id";
    public static final String FIELD_NOT_EXIST = " is missing";
    public static final String FIELDS_NOT_EXIST = " are missing";
    public static final String LAST_UPDATED_DATE = "lastUpdatedDate";
    public static final String CHART_TITLE = "Chart Title";
    public static final String DASHBOARD_NOT_EXISTS = "The Provided Dashboard doesn't exists";

    public static final String COMMON_FILTERS = "commonFilters";
    public static final String ROW_CHECKED = "rowChecked";
    
    //Chart Categories
    public static final int CATEGORY_XY_CHART = 1;
    public static final int CATEGORY_PIE = 2;
    public static final int CATEGORY_HIERARCHY = 3;
    public static final int CATEGORY_TABLE = 4;
    public static final int CATEGORY_GAUGE = 5;
    public static final int CATEGORY_TEXT_EDITOR = 6;
    public static final int CATEGORY_CLUSTER = 7;
    public static final int CATEGORY_USGEO = 8;
    public static final int CATEGORY_SCORED_SEARCH_TABLE = 9;
    public static final int RELEVANT_CONFIG = 10;

    public static final Map<Integer, String> EDIT_SCREEN_URL_BY_CATEGORY = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;
        {
            put(CATEGORY_XY_CHART, "layout/edit_chart.zul");
            put(CATEGORY_PIE, "layout/edit_chart.zul");
            put(CATEGORY_HIERARCHY, "layout/edit_tree.zul");
            put(CATEGORY_TABLE, "layout/edit_table.zul");
            put(CATEGORY_GAUGE, "layout/edit_gauge.zul");
            put(CATEGORY_TEXT_EDITOR, "layout/edit_text_editor.zul");
            put(CATEGORY_CLUSTER, "layout/edit_cluster_chart.zul");
            put(CATEGORY_USGEO, "layout/edit_chart.zul");
            put(CATEGORY_SCORED_SEARCH_TABLE, "layout/scored_search_table.zul");
            put(RELEVANT_CONFIG, "layout/relevant_config.zul");
        }
    };
    
    
    public static final String CHARTS="charts";
    public static final String PNG=".png";
    
    public static final String BAR_CHART = "Bar Chart";
    public static final String LINE_CHART = "Line Chart";
    public static final String PIE_CHART = "Pie Chart";
    public static final String DONUT_CHART = "Donut Chart";
    
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CONTRIBUTOR = "contributor";
    public static final String ROLE_CONSUMER = "consumer";
    public static final String COLON = ":";
    
    // Both of the following values are used in dashboard_config.zul, without reference
    public static final Integer VISIBLITY_PUBLIC = 1;
    public static final Integer VISIBLITY_PRIVATE = 0;
    
    public static final String GROUP = "group";
    public static final String SUPER_USER_GROUP_CODE = "HPCCDASHDEV_SUPER_USERS"; //Hard Coded for MBS Service
    public static final String SUPER_USER_GROUP_NAME = "super_users"; 
    public static final Integer ZERO = 0;
    public static final String GOOGLE_CHART_API = "Google chart Api";
    public static final String SAVE_BUTTON = "glyphicon glyphicon-save";
    public static final String BUTTON_ZCLASS = "btn btn-xs btn-info";
    public static final String MAIN_INCLUDE = "#mainInclude";
    public static final String DASHBOARD_URL = "/demo/layout/dashboard.zul";
    public static final String USER_CREDENTIAL = "userCredential";
    public static final String APPLN_ACCESS = "applnAccess";
    public static final String APPLN_CREATE_ACCESS = "applnCreateAccess";
    public static final String DASHBOARD_CREATE_ROLE = "dashboardCreateRole";
    
    //Service Names
    public static final String AUTHENTICATION_SERVICE = "authenticationService";
    public static final String DASHBOARD_SERVICE = "dashboardService";
    public static final String HPCC_SERVICE = "hpccService";
    public static final String HPCC_QUERY_SERVICE = "hpccQueryService";
    public static final String CHART_SERVICE = "chartService";
    public static final String WIDGET_SERVICE = "widgetService";
    public static final String GROUP_SERVICE = "groupService";
    
    
    // Ued for NONE Aggregation
    public static final String NONE = "none";
    public static final String AVERAGE = "avg";
    public static final String COUNT = "count";
    public static final String MINIMUM = "min";
    public static final String MAXIMUM = "max";
    public static final String SUM = "sum";
    public static final String AGGREGATE_FLAG = "aggregateflag";
    
    //Chart panel button states
    public static final int SHOW_ALL_BUTTONS = 1;
    public static final int SHOW_EDIT_ONLY = 2;
    public static final int SHOW_NO_BUTTONS = 3;

    
    public static final String EXCEPTION = "Exception";
    
    // Style class names
    public static final String STYLE_POPUP = "popup";
    public static final String CLOSE_BUTTON_STYLE = "glyphicon glyphicon-remove btn btn-link img-btn close-btn";
    
    // Notification positions
    public static final String POSITION_CENTER = "middle_center";
    public static final String POSITION_END_CENTER = "end_center";
    
    // Notification Types
    public static final String ERROR_NOTIFICATION = "error";
    
    //File type
    public static final String LOGICAL_FILE = "logicalfile";
    public static final String QUERY = "query";
    
    public static final String UNABLE_TO_FETCH_DATA = "Unable to fetch Hpcc data";
	public static final String INPUT_PARAM_BTN = "isInputParamBtn";
	
	public static final String TRUE = "true";
	
	//EVENTS
	public static final String DRAW_CHART_EVENT = "onDrawChart";
	public static final String CREATE_FILTER_EVENT = "onFilterAdded";
	public static final String CREATE_PARAM_EVENT = "onCreateParams";
	public static final String Y_MIN = "yMin";
	public static final String Y_MAX = "yMax";
	public static final String Y2_MIN = "y2Min";
	public static final String Y2_MAX = "y2Max";
	public static final String COMMON_FILTERS_ENABLED = "isCommonFilterEnabled";
	public static final String HAS_INPUT_PARAM_VALUES = "hasInputParamValues";
	public static final String API_FILTER_EVENT = "onEnableApiFilter";
	public static final String SCORED_SEARCH = "Scored Search";
	public static final String ON_ADD_FILTER = "onAddFilter";
	public static final String ON_DELETE_FILTER = "onDeleteFilter";
	public static final String GENERIC = "GENERIC";
	public static final String FETCH_INPUT_PARAM = "fetch_input_parameters";
    public static final String ON_SAVE_INTERACTIVITY = "onSaveInteractivity";
    public static final String ON_SELECT_INTERACTIVITY_FILTER = "onSelectInteractivityFilter"; 
	
}
