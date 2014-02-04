package org.hpccsystems.dashboard.common;

/**
 * Class to persist all the DB Queries
 *
 */
public class Queries {

	private static final long serialVersionUID = 1L;

	public static final String GET_MAX_DASHBOARD_ID = "select max(dashboard_id) from dashboard_details where user_id=?";

	public static final String INSERT_DASHBOARD = "INSERT INTO dashboard_details(dashboard_name,user_id,sourcetype,sourceid,updateddate,column_count) VALUES(?,?,?,?,?,?)";
	public static final String UPDATE_DASHBOARD_SEQUENCE = "update dashboard_details set sequence=?, dashboard_name=? where dashboard_id=?";
	public static final String DELETE_DASHBOARD_WIDGETS = "delete from widget_details where DASHBOARD_ID=?";
	public static final String DELETE_WIDGETS = "delete from widget_details where WIDGET_ID=?";
	public static final String DELETE_DASHBOARD = "delete from dashboard_details where DASHBOARD_ID=? and USER_ID =? ";
	public static final String UPDATE_DASHBOARD_STATE = "update dashboard_details set dashboard_state=?,sequence=?,dashboard_name=?,updateddate=? where dashboard_id=?";
	public static final String UPDATE_DASHBOARD_DETAILS = "update dashboard_details set sequence=?,dashboard_name=?,column_count=?,updateddate=? where dashboard_id=?";
	public static final String UPDATE_WIDGET_DETAILS = "update widget_details set WIDGET_NAME=?,WIDGET_STATE=?,CHART_TYPE=?,COLUMN_IDENTIFIER=?,WIDGET_SEQUENCE=?,CHART_DATA=? where WIDGET_ID=? and DASHBOARD_ID=?";
	public static final String INSERT_WIDGET_DETAILS = "insert into WIDGET_DETAILS(DASHBOARD_ID,WIDGET_NAME,WIDGET_STATE,CHART_TYPE,COLUMN_IDENTIFIER,WIDGET_SEQUENCE,CHART_DATA) values(?,?,?,?,?,?,?)";
	public static final String GET_USER_DETAILs = "select user_id,password,active_flag,user_name from user_details where user_name=?"; 
	public static final String GET_APPLICATIONS = "SELECT dash_app_id,dash_app_name FROM dash_application";
	public static final String RESET_USER_FLAG = "update user_details set active_flag=? where user_id=?";
	public static final String GET_WIDGET_DETAILS = "SELECT WIDGET_ID,WIDGET_NAME,WIDGET_STATE,COLUMN_IDENTIFIER,CHART_TYPE,CHART_DATA FROM widget_details where  DASHBOARD_ID = ";
	public static final String CIRCUIT_DELETE_DASHBOARD = "delete from dashboard_details where DASHBOARD_ID=? ";
	public static final String GET_WIDGET = "select * from widget_details where DASHBOARD_ID=? order by WIDGET_SEQUENCE";
	public static final String RETRIEVE_DASHBOARD_DETAILS = "select * from dashboard_details where sourceid = ";
	public static final String RETRIEVE_DASHBOARD_IDS = "select dashboard_id from dashboard_details where dashboard_id = ";
	public static final String DASHBOARD_IN_CLAUSE = "' and DASHBOARD_ID in ";

}
