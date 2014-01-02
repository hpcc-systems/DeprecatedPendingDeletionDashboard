package org.hpccsystems.dashboard.services.impl;

import java.sql.SQLException;
import java.util.List;

import org.hpccsystems.dashboard.dao.WidgetDao;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * Service class to define Widget related services
 *
 */
@Service("widgetService") 
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class WidgetServiceImpl implements WidgetService {
	
	WidgetDao widgetDao;

	public WidgetDao getWidgetDao() {
		return widgetDao;
	}

	@Autowired
	public void setWidgetDao(WidgetDao widgetDao) {
		this.widgetDao = widgetDao;
	}

	public void updateWidgetDetails(Integer dashboardId,List<Portlet> portlets)throws SQLException {
		widgetDao.updateWidgetDetails(dashboardId,portlets);
	}

	public void addWidgetDetails(Integer dashboardId,List<Portlet> portlets) throws SQLException{
		widgetDao.addWidgetDetails(dashboardId,portlets);
		
	}

	public void deleteWidgets(Integer dashboardId, List<Portlet> portlets)
			throws SQLException {
		widgetDao.deleteWidgets(dashboardId,portlets);
		
	}
	
	public List<Portlet> retriveWidgetDetails(Integer dashboardId) throws SQLException {
		return widgetDao.retriveWidgetDetails(dashboardId);
	}

}
