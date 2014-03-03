package org.hpccsystems.dashboard.services;
import java.util.List;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.springframework.dao.DataAccessException;

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
	 * 	Dashboard ID
	 * @throws Exception
	 */
	int addDashboardDetails(Dashboard dashboard,String applicationId, String sourceId,String userId) throws DataAccessException ;
	
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
	List<Dashboard> retrieveDashboardMenuPages(String applicationId,String userId,List<String> dashboardIdList, String sourceId)throws DataAccessException;	
	
	/**
	 * @param dashboardId
	 * @param deleteStatus
	 */
	int deleteDashboard(Integer dashboardId,String userId)throws DataAccessException;	
	
	/**
	 * updateSidebarDetails() is responsible for update the sidebar details into dashboard_details table.
	 * @param dashboardIdList
	 * @throws Exception
	 */
	void updateSidebarDetails(List<Integer> dashboardIdList)throws DataAccessException;
	

	/**
	 * Service to update dashboard details into DB
	 * @param dashboard
	 * @throws Exception
	 */
	void updateDashboard(Dashboard dashboard)throws DataAccessException;
	
}
