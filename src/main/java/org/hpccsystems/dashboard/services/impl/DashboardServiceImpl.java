package org.hpccsystems.dashboard.services.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Date;
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
import org.springframework.dao.DataAccessException;
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

	public List<Dashboard> retrieveDashboardMenuPages(final Application application,String userId,List<String> dashboardIdList)throws Exception {
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Handling 'retrieveDashboardMenuPages' in DashboardServiceImpl");
		}
		
		final HashMap<String,Dashboard> pageMap = new LinkedHashMap<String,Dashboard>();
		List<Dashboard> menuList = null;
		try
		{
			menuList = dashboardDao.fetchDashboardDetails(application,userId,dashboardIdList);
		}
		catch(final DataAccessException ex)
		{
			throw ex;
		}
		
		final String fnName = "fn";
		if(menuList != null)
		{
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
		 * @param sourceId
		 * @param dashBoardName
		 * @param layout
		 * @throws SQLException
	 */
	
	public int addDashboardDetails(final String sourceId,final String source, final String dashBoardName,
			final String userId, final Date dashBoardDate) throws Exception {
		try
		{
		return dashboardDao.addDashboardDetails(sourceId,source,dashBoardName,userId,dashBoardDate);
		}
		catch(DataAccessException ex)
		{
			LOG.error("DataAccessException in addDashboardDetails()",ex);
			throw ex;
		}
	}
	
	
	
	/* 
	 * service to delete a dashboard
	 *
	 */
	public int deleteDashboard(Integer dashboardId, String userId) throws Exception{
		try {
			return dashboardDao.deleteDashboard(dashboardId,userId);
		} catch (DataAccessException e) {
			LOG.error("DataAccessException in deleteDashboard()", e);
			throw e;
		}		
	}
	/* 
	 * service to update sequence,name,state of a dashboard
	 *
	 */
	public void updateDashboardSate(Integer dashboardId, String emptyState,
			int sequence,String dashboardName, Date updatedDate)throws Exception {
		try {
			dashboardDao.updateDashboardState(dashboardId,emptyState,sequence,dashboardName,updatedDate);
		} catch (DataAccessException e) {
			LOG.error("DataAccessException in updateDashboardSate()", e);
			throw e;
		}
	}
	/* 
	 * service to update entire details of a dashboard
	 *
	 */
	public void updateDashboardDetails(Integer dashboardId, int sequence,
			String dashboardName, int columnCount, Date updatedDate)throws Exception {
		try {
			dashboardDao.updateDashboardDetails(dashboardId,sequence,dashboardName,columnCount,updatedDate);
		} catch (DataAccessException e) {
			LOG.error("DataAccessException in updateDashboardDetails()", e);
			throw e;
		}
	}

	
}
