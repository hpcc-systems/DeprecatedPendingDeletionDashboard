package org.hpccsystems.dashboard.common;

/**
 * Class to persist all the DB Queries
 *
 */
public class Queries {
	
	public static final String INSERT_DASHBOARD = "INSERT INTO dashboard_details(dashboard_name,user_id,application_Id,last_updated_date,column_count,sequence,source_id) VALUES(?,?,?,?,?,?,?)";
	public static final String DELETE_DASHBOARD_WIDGETS = "delete from widget_details where dashboard_id=?";
	public static final String DELETE_WIDGETS = "delete from widget_details where widget_id=?";
	public static final String DELETE_DASHBOARD = "delete from dashboard_details where dashboard_id=? and user_id =? ";	
	public static final String INSERT_WIDGET_DETAILS = "insert into widget_details(dashboard_id,widget_name,widget_state,chart_type,column_identifier,widget_sequence,chart_data) values(?,?,?,?,?,?,?)";
	public static final String GET_USER_DETAILS = "select user_id,password,active_flag,user_name from user_details where user_name=?"; 
	public static final String GET_APPLICATIONS = "SELECT dash_app_id,dash_app_name FROM dash_application";
	public static final String RESET_USER_FLAG = "update user_details set active_flag=? where user_id=?";
	public static final String GET_WIDGET_DETAILS = "SELECT widget_id,widget_name,widget_state,column_identifier,chart_type,chart_data FROM widget_details where  dashboard_id = ";
	public static final String API_DELETE_DASHBOARD = "delete from dashboard_details where dashboard_id=? ";
	public static final String RETRIEVE_DASHBOARD_DETAILS = "select * from dashboard_details where application_id = ";
	public static final String DASHBOARD_IN_CLAUSE = " and dashboard_id in ";
	public static final String UPDATE_SIDEBAR_DETAILS = "update dashboard_details set sequence=? where dashboard_id=?";
	public static final String UPDATE_DASHBOARD =  "update dashboard_details set dashboard_name=?,column_count=?,source_id=?,last_updated_date=? where dashboard_id=?";
	public static final String UPDATE_WIDGET_SEQUENCE = "update widget_details set column_identifier=?,widget_sequence=? where widget_id=? and dashboard_id=?";
	public static final String ADD_CHART_DATA = "update widget_details set widget_state=?, chart_type=?  where widget_id=?";
	public static final String CLEAR_CHART_DATA = "update widget_details set widget_name=?, widget_state=?,chart_type=?,chart_data=?  where widget_id=?";
	public static final String UPADET_LIVE_CHART_DATA = "update widget_details set widget_state=? , chart_type=?,chart_data=?  where widget_id=?";
	public static final String UPADET_WIDGET_NAME = "update widget_details set widget_name=? where widget_id=?";


}
