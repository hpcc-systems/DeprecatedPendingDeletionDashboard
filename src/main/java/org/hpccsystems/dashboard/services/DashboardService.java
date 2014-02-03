package org.hpccsystems.dashboard.services;
import java.sql.SQLException;
import java.sql.Date;
import java.util.List;

import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Dashboard;

/**
 * DashboardService is used to interact with Database for Dashboard project.
 *
 */
public interface DashboardService {

	 /**
		 * Inserts Dashboard details to dashboard_details table.
		 * @param sourceId
		 * @param dashBoardName
		 * @param layout
		 * @throws SQLException
	 */
	int addDashboardDetails(String sourceId,String source,String dashBoardName,String userId, Date dashBoardDate) throws Exception ;
	
	
	/**
	 * Retrieving DashboardMenuPages details from dashboard_details table.
	 * @param viewModel
	 * @return List<SidebarPage>
	 */
	List<Dashboard> retrieveDashboardMenuPages(Application application,String userId,List<String> dashboardIdList)throws Exception;
	
	
	
	/**
	 * @param dashboardId
	 * @param deleteStatus
	 */
	int deleteDashboard(Integer dashboardId,String userId)throws Exception;
	
	/**
	 * @param dashboardId
	 * @param emptyState
	 * @param sequence
	 */
	void updateDashboardSate(Integer dashboardId,String emptyState,int sequence,String dashboardName, Date updatedDate)throws Exception;
	
	/**
	 * @param dashboardId
	 * @param sequence
	 * @param dashboardName
	 * @param columnCount
	 */
	void updateDashboardDetails(Integer dashboardId,int sequence,String dashboardName,int columnCount,Date updatedDate)throws Exception;
	

}
