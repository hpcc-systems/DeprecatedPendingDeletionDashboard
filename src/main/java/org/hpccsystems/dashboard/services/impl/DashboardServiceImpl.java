package org.hpccsystems.dashboard.services.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.services.DashboardService;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * DashboardServiceImpl is implementaiton class for DashboardService.
 *
 */
@Service("dashboardService") 
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DashboardServiceImpl implements DashboardService {
	
	private static final  Log LOG = LogFactory.getLog(DashboardServiceImpl.class); 
	
	private DashboardDao dashboardDao;

	
	/**
	 * Retrieving DashboardMenuPages details from dashboard_details table.
	 * @param viewModel
	 * @return List<SidebarPage>
	 */

	public List<Dashboard> retrieveDashboardMenuPages(final Application application,String userId) {
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Handling 'retrieveDashboardMenuPages' in DashboardServiceImpl");
		}
		
		final HashMap<String,Dashboard> pageMap = new LinkedHashMap<String,Dashboard>();
		
		final List<Dashboard> menuList = dashboardDao.fetchDashboardDetails(application,userId);
		
		final String fnName = "fn";

		for (int i = 0; i < menuList.size(); i++) 
		{
			final Dashboard entry = (Dashboard)menuList.get(i);
			final int j = i + 1;									
			final String fnNameStr = fnName + j;
			if (LOG.isDebugEnabled()) {
				LOG.debug("dasboardname:"+entry.getName());
			}
			pageMap.put(fnNameStr, entry);
						
		}
		return new ArrayList<Dashboard>(pageMap.values());
	}

	public DashboardDao getDashboardDao() {
		return dashboardDao;
	}

	public void setDashboardDao(final DashboardDao dashboardDao) {
		this.dashboardDao = dashboardDao;
	}

	 /**
		 * Inserts Dashboard details to dashboard_details table.
		 * @param applnId
		 * @param dashBoardName
		 * @param layout
		 * @throws SQLException
	 */
	
	public int addDashboardDetails(final String applnId, final String dashBoardName,
			final String userId) throws SQLException {
		return dashboardDao.addDashboardDetails(applnId,dashBoardName,userId);
	}
	
	
	/* 
	 * service to update sequence of a dashboard
	 *
	 */
	public void updateSequence(Integer dashboardId, int sequence,String dashboardName) {		
		try {
			dashboardDao.updateSequence(dashboardId,sequence,dashboardName);
		} catch (SQLException e) {
			LOG.error("SQLException", e);
		}
	}
	/* 
	 * service to delete a dashboard
	 *
	 */
	public void deleteDashboard(Integer dashboardId, String userId) {
		try {
			dashboardDao.deleteDashboard(dashboardId,userId);
		} catch (SQLException e) {
			LOG.error("SQLException", e);
		}		
	}
	/* 
	 * service to update sequence,name,state of a dashboard
	 *
	 */
	public void updateDashboardSate(Integer dashboardId, String emptyState,
			int sequence,String dashboardName) {
		try {
			dashboardDao.updateDashboardState(dashboardId,emptyState,sequence,dashboardName);
		} catch (SQLException e) {
			LOG.error("SQLException", e);
		}
	}
	/* 
	 * service to update entire details of a dashboard
	 *
	 */
	public void updateDashboardDetails(Integer dashboardId, int sequence,
			String dashboardName, int columnCount) {
		try {
			dashboardDao.updateDashboardDetails(dashboardId,sequence,dashboardName,columnCount);
		} catch (SQLException e) {
			LOG.error("SQLException", e);
		}
	}

}
