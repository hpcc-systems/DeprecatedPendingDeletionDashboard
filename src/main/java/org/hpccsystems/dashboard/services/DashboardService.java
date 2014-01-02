package org.hpccsystems.dashboard.services;
import java.sql.SQLException;
import java.util.List;

import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;

/**
 * DashboardService is used to interact with Database for Dashboard project.
 *
 */
public interface DashboardService {

	 /**
		 * Inserts Dashboard details to dashboard_details table.
		 * @param applnId
		 * @param dashBoardName
		 * @param layout
		 * @throws SQLException
	 */
	int addDashboardDetails(String applnId,String dashBoardName,String userId) throws SQLException ;
	
	
	/**
	 * Retrieving DashboardMenuPages details from dashboard_details table.
	 * @param viewModel
	 * @return List<SidebarPage>
	 */
	List<Dashboard> retrieveDashboardMenuPages(Application application,String userId);
	
	/**service to update sequence of a dashboard
	 * @param dashboard
	 */
	void updateSequence(Integer dashboardId,int sequence,String dashboardName);
	
	/**
	 * @param dashboardId
	 * @param deleteStatus
	 */
	void deleteDashboard(Integer dashboardId,String userId);
	
	/**
	 * @param dashboardId
	 * @param emptyState
	 * @param sequence
	 */
	void updateDashboardSate(Integer dashboardId,String emptyState,int sequence,String dashboardName);
	
	/**
	 * @param dashboardId
	 * @param sequence
	 * @param dashboardName
	 * @param columnCount
	 */
	void updateDashboardDetails(Integer dashboardId,int sequence,String dashboardName,int columnCount);

}
