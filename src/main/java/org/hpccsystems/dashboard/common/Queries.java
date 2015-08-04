package org.hpccsystems.dashboard.common;

/**
 * Class to persist all the DB Queries
 *
 */
public class Queries {
    
    private Queries() {
    }
    
    public static final String INSERT_DASHBOARD = "INSERT INTO dashboard_details(dashboard_name,user_id,application_Id,last_updated_date,column_count,sequence,source_id,visibility,common_filter) VALUES(?,?,?,?,?,?,?,?,?)";
    public static final String DELETE_DASHBOARD_WIDGETS = "delete from widget_details where dashboard_id=?";
    public static final String DELETE_WIDGETS = "delete from widget_details where widget_id=?";
    public static final String DELETE_DASHBOARD = "delete from dashboard_details where dashboard_id=? and user_id =? ";    
    public static final String DELETE_DASHBOARD_ADMIN = "delete from dashboard_details where dashboard_id=?";    
    public static final String INSERT_WIDGET_DETAILS = "insert into widget_details(dashboard_id,widget_name,widget_state,chart_type,column_identifier,widget_sequence,chart_data,single_widget) values(?,?,?,?,?,?,?,?)";
    public static final String GET_USER_DETAILS = "select id,password,active_flag,first_name,last_name from user_details where id=?"; 
    public static final String INSERT_USER = "INSERT INTO user_details(id,first_name,last_name,password) VALUES (?, ?,?,?)";
    public static final String GET_APPLICATIONS = "SELECT dash_app_id,dash_app_name FROM dash_application";
    public static final String RESET_USER_FLAG = "update user_details set active_flag=? where id=?";
    public static final String GET_WIDGET_DETAILS = "SELECT widget_id,widget_name,widget_state,column_identifier,chart_type,chart_data,single_widget FROM widget_details where  dashboard_id = ";
    public static final String API_DELETE_DASHBOARD = "delete from dashboard_details where dashboard_id=? ";
    public static final String RETRIEVE_DASHBOARD_DETAILS = "select * from dashboard_details where application_id = ";
    public static final String DASHBOARD_IN_CLAUSE = " and dashboard_id in ";
    public static final String UPDATE_SIDEBAR_DETAILS = "update dashboard_details set sequence=? where dashboard_id=?";
    public static final String UPDATE_DASHBOARD =  "update dashboard_details set dashboard_name=?,column_count=?,source_id=?,last_updated_date=?,visibility=?,common_filter=?,show_localfilter=? where dashboard_id=?";
    public static final String UPDATE_WIDGET_SEQUENCE = "update widget_details set column_identifier=?,widget_sequence=? where widget_id=? and dashboard_id=?";
    public static final String ADD_CHART_DATA = "update widget_details set widget_state=?, chart_type=?  where widget_id=?";
    public static final String CLEAR_CHART_DATA = "update widget_details set widget_name=?, widget_state=?,chart_type=?,chart_data=?  where widget_id=?";
    public static final String UPADET_LIVE_CHART_DATA = "update widget_details set widget_state=? , chart_type=?,chart_data=?  where widget_id=?";
    public static final String UPADET_WIDGET_NAME = "update widget_details set widget_name=? where widget_id=?";
    public static final String INSERT_PLUGIN = "INSERT INTO chart_details(name,description,configuration,created_by,category,isplugin) VALUES(?,?,?,?,?,?)";
    public static final String GET_CHARTS = "SELECT * FROM chart_details";
    public static final String DELETE_CHART = "delete from chart_details where id=? ";
    public static final String GET_PLUGINS = "SELECT * FROM chart_details WHERE isplugin=true";
    public static final String INSERT_GROUP="insert into acl_public(dashboard_id,group_code,group_name,role,last_updated_date) values(?,?,?,?,?) ";
    public static final String SELECT_GROUP="select * from acl_public where dashboard_id = ?";
    public static final String DELETE_GROUP="delete from acl_public where dashboard_id=? and group_code in(?)";
    public static final String GET_PRIVATE_DASHBOARDS = "SELECT * FROM dashboard_details WHERE user_id=? AND application_id=? order by sequence";
    public static final String GET_ROLE_BASED_DASHBOARDS = "SELECT a.role,d.dashboard_id,d.application_id,d.dashboard_name,d.column_count,d.source_id,d.visibility,d.last_updated_date,d.common_filter,d.show_localfilter FROM acl_public AS a JOIN dashboard_details AS d ON a.dashboard_id = d.dashboard_id AND a.group_code in ";
    public static final String GET_ALL_DASHBOARD = "SELECT * FROM dashboard_details where application_id=? order by sequence";
    public static final String GET_DASHBOARD = "SELECT * FROM dashboard_details WHERE dashboard_id=?";
    public static final String DELETE_ACL_PUBLIC = "delete from acl_public where dashboard_id=?";
    public static final String UPDATE_GROUP_ROLE = "update acl_public set role=? where dashboard_id=? and group_code=?";
    public static final String GET_DASHBOARD_NAME = "SELECT dashboard_name  FROM dashboard_details where user_id = ? and application_id = ?";
    public static final String GET_ALL_GROUPS = "select * from group_details";
    public static final String GET_USER_GROUPS = "select * from group_details as g JOIN user_groups as u ON g.group_code = u.group_code AND u.user_id=?";
    public static final String GET_ALL_USER_IDS = "SELECT id FROM user_details";
    public static final String RESET_USER_PASSWORD = "UPDATE user_details SET password=? WHERE id=?";
    public static final String GET_ALL_USER = "SELECT * FROM user_details";
    public static final String INSERT_GROUP_USER = "INSERT INTO user_groups(group_code,user_id) values(?,?) ";
    public static final String INSERT_NEW_GROUP = "INSERT INTO group_details(group_code,group_name) values(?,?)";
    public static final String DELETE_GROUP_USER = "DELETE FROM user_groups WHERE user_id = ? and group_code = ?";
    
    
}
