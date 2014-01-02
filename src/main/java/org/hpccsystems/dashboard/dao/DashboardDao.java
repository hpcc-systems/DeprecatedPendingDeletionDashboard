package org.hpccsystems.dashboard.dao;

import java.sql.SQLException;
import java.util.List;

import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.springframework.dao.DataAccessException;

/**
 * DashboardDao class is used for database operations for Dashboard project.
 *
 */
public interface DashboardDao {
		
	/**
	 * Inserts Dashboard details to dashboard_details table.
	 * @param applnId
	 * @param dashBoardName
	 * @param layout
	 * @throws SQLException
	 */
	 int addDashboardDetails(String applnId,String dashBoardName,String userId) throws SQLException ;
	
	 /**
	  * Fetching DashboardMenuPages details from dashboard_details table.
	 * @param viewModel
	 * @return List<DashboardMenu>
	 * @throws DataAccessException
	 */
	List<Dashboard> fetchDashboardDetails(Application application,String userId) throws DataAccessException;


	
	/**
	 * method to update sequence of dashboard
	 * @param dashboardId
	 * @param sequence
	 */
	void updateSequence(Integer dashboardId,int sequence,String dashboardName) throws SQLException;
	
	/**
	 * method to delete dashboard
	 * @param dashboardId
	 * @param deleteStatus
	 */
	void deleteDashboard(Integer dashboardId, String userId) throws SQLException;
	
	/**
	 * Method to update empty state of dashboard
	 * @param dashboardId
	 * @param emptyState
	 * @param sequence
	 * @param dashboardName
	 */
	void updateDashboardState(Integer dashboardId,String emptyState,int sequence,String dashboardName) throws SQLException;
	
	/**
	 * Method to update entire dashboard details
	 * @param dashboardId
	 * @param sequence
	 * @param dashboardName
	 * @param columnCount
	 */
	void updateDashboardDetails(Integer dashboardId,int sequence,String dashboardName,int columnCount) throws SQLException;

}
