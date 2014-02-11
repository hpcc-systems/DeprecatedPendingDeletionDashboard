package org.hpccsystems.dashboard.services.impl;

import java.util.List;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.dao.WidgetDao;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

/**
 * Service class to define Widget related services
 *
 */
@Service("widgetService") 
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WidgetServiceImpl implements WidgetService {
	private static final  Log LOG = LogFactory.getLog(WidgetServiceImpl.class); 
	WidgetDao widgetDao;

	public WidgetDao getWidgetDao() {
		return widgetDao;
	}

	@Autowired
	public void setWidgetDao(WidgetDao widgetDao) {
		this.widgetDao = widgetDao;
	}

	public void updateWidgetDetails(Integer dashboardId,List<Portlet> portlets)throws Exception {
		try
		{
		widgetDao.updateWidgetDetails(dashboardId,portlets);
		}catch(DataAccessException ex)
		{
			LOG.error("DataAccessException in updateWidgetDetails() in WidgetServiceImpl"+ex);
			throw ex;
		}
	}

	public void addWidgetDetails(Integer dashboardId,List<Portlet> portlets) throws Exception{
		try
		{
		widgetDao.addWidgetDetails(dashboardId,portlets);
		}catch(DataAccessException ex)
		{
			LOG.error("DataAccessException in addWidgetDetails() in WidgetServiceImpl"+ex);
			throw ex;
		}
		
	}

	public void deleteWidgets(Integer dashboardId, List<Portlet> portlets)
			throws Exception {
		try
		{
		widgetDao.deleteWidgets(dashboardId,portlets);
		}catch(DataAccessException ex)
		{
			LOG.error("DataAccessException in deleteWidgets() in WidgetServiceImpl"+ex);
			throw ex;
		}
		
	}
	
	public List<Portlet> retriveWidgetDetails(Integer dashboardId) throws Exception {
		try
		{
		return widgetDao.retriveWidgetDetails(dashboardId);
		}catch(DataAccessException ex)
		{
			LOG.error("DataAccessException in retriveWidgetDetails() in WidgetServiceImpl"+ex);
			throw ex;
		}
	}

	@Override
	public void deleteWidget(Integer portletId) throws Exception {
		try	{
			widgetDao.deleteWidget(portletId);
		}catch(DataAccessException ex)	{
			LOG.error("DataAccessException in deleteWidgets() in WidgetServiceImpl"+ex);
			throw ex;
		}
	}
	
	@Override
	public void updateWidgetSequence(Dashboard dashboard) throws Exception {
		try
		{
			widgetDao.updateWidgetSequence(dashboard.getDashboardId(),dashboard.getPortletList());
		}catch(DataAccessException ex)
		{
			LOG.error("DataAccessException in updateWidgetSequence() in WidgetServiceImpl"+ex);
			throw ex;
		}
		
	}

	@Override
	public void updateWidget(Portlet portlet) throws Exception {
		try
		{
			widgetDao.updateWidget(portlet);
		}catch(DataAccessException ex)
		{
			LOG.error("DataAccessException in updateWidget() in WidgetServiceImpl"+ex);
			throw ex;
		}
		
	}

	@Override
	public void updateWidgetTitle(Portlet portlet)throws Exception {
		try
		{
			widgetDao.updateWidgetTitle(portlet);
		}catch(DataAccessException ex)
		{
			LOG.error("DataAccessException in updateWidgetTitle() in WidgetServiceImpl"+ex);
			throw ex;
		}
		
	}

	@Override
	public void addWidget(Integer dashboardId, Portlet portlet, Integer sequence)
			throws Exception {
		try {
			widgetDao.addWidget(dashboardId, portlet, sequence);
		} catch(DataAccessException ex) {
			LOG.error("DataAccessException in addWidgetDetails() in WidgetServiceImpl"+ex);
			throw ex;
		}
	}

}
