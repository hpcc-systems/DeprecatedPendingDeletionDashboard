package org.hpccsystems.dashboard.dao;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.springframework.dao.DataAccessException;


/**
 * DashboardDao class is used for database operations for Dashboard project.
 *
 */
public interface DashboardDao {
		
	/**
	 * Inserts Dashboard details to dashboard_details table.
	 * @param sourceId
	 * @param dashBoardName
	 * @param layout
	 * @throws SQLException
	 */
	 int addDashboardDetails(Dashboard dashboard, String applicationId, String sourceId,String userId) throws DataAccessException ;
	
	/**
	 * Fetching DashboardMenuPages details from dashboard_details table.
	 * @param applicationId
	 * 	Must be specified always
	 * @param userId
	 * 	Can be null, while specifying sourceId
	 * @param dashboardIdList
	 *  Can be null when both applicationId and userId are specified
	 * @param sourceId
	 *  Can be null.
	 * @return
	 *  A list of dahboards based on the arguments passed.
	 * @throws DataAccessException
	 */
	List<Dashboard> fetchDashboardDetails(String applicationId,String userId,List<String> dashboardIdList, String sourceId) throws DataAccessException;	
	
	/**
	 * method to delete dashboard
	 * @param dashboardId
	 * @param deleteStatus
	 */
	int deleteDashboard(Integer dashboardId, String userId) throws DataAccessException;
	
	/**
	 * Method to update empty state of dashboard
	 * @param dashboardId
	 * @param emptyState
	 * @param sequence
	 * @param dashboardName
	 */
	void updateDashboardState(Integer dashboardId,String emptyState,int sequence,String dashboardName, Date updatedDate) throws DataAccessException;
	
	/**
	 * Method to update entire dashboard details
	 * @param dashboardId
	 * @param sequence
	 * @param dashboardName
	 * @param columnCount
	 */
	void updateDashboardDetails(Integer dashboardId,int sequence,String dashboardName,int columnCount, Date updatedDate) throws DataAccessException;
}
