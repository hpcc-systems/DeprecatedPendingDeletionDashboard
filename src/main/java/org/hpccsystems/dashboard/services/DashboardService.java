package org.hpccsystems.dashboard.services;
import java.sql.Date;
import java.util.List; 
import org.hpccsystems.dashboard.entity.Dashboard;

/**
 * DashboardService is used to interact with Database for Dashboard project.
 *
 */
public interface DashboardService {

	/**
	 *  Inserts Dashboard details to dashboard_details table.
	 * @param dashboard
	 * @param applicationId
	 * @param sourceId
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	int addDashboardDetails(Dashboard dashboard,String applicationId, String sourceId,String userId) throws Exception ;
	
	/**
	 * @param applicationId
	 * 	Must be provided always
	 * @param userId
	 *  Can only be null when sourceId is specified
	 * @param dashboardIdList
	 * 	Can be null
	 * @param sourceId
	 *  can be null
	 * @return
	 *  A list of dahboard based on the paramenters specified
	 * @throws Exception
	 */
	List<Dashboard> retrieveDashboardMenuPages(String applicationId,String userId,List<String> dashboardIdList, String sourceId)throws Exception;	
	
	/**
	 * @param dashboardId
	 * @param deleteStatus
	 */
	int deleteDashboard(Integer dashboardId,String userId)throws Exception;	
	
	/**
	 * updateSidebarDetails() is responsible for update the sidebar details into dashboard_details table.
	 * @param dashboardIdList
	 * @throws Exception
	 */
	void updateSidebarDetails(List<Integer> dashboardIdList)throws Exception;
	

	/**
	 * Service to update dashboard details into DB
	 * @param dashboard
	 * @throws Exception
	 */
	void updateDashboard(Dashboard dashboard)throws Exception;
	
}
