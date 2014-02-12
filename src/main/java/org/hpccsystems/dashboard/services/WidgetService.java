package org.hpccsystems.dashboard.services;

import java.sql.SQLException;
import java.util.List;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.springframework.dao.DataAccessException;


/**
 * Service class,has abstract methods for Widget related services
 *
 */
public interface WidgetService {
	
	/**
	 * Retrieves Widget list from DB
	 * 
	 * @param dashboardId
	 * @return
	 * 	A list of Portlet objects corresponding to the provided Dashboard Id
	 * @throws SQLException
	 */
	List<Portlet> retriveWidgetDetails(Integer dashboardId) throws DataAccessException;	
	
	/**
	 * Adds the Widget to DB and sets portletId to the portlet Object passed
	 * @param dashboardId
	 * @param portlet
	 * @param sequence
	 * @throws Exception
	 */
	void addWidget(Integer dashboardId, Portlet portlet, Integer sequence)throws DataAccessException;
	
	/**
	 * @param dashboardId
	 * @param portlets
	 * @param widgetSequence
	 */
	void addWidgetDetails(Integer dashboardId,List<Portlet> portlets)throws DataAccessException;	
	
	/**
	 * Deletes widget from DB
	 * @param portletId
	 * @throws Exception
	 */
	void deleteWidget(Integer portletId)throws DataAccessException;
	
	/**
	 * Service call to update Widget sequence alone as a batch service
	 * @param dashboard
	 * @throws Exception
	 */
	void updateWidgetSequence(Dashboard dashboard)throws DataAccessException;

	 /**
	 * Service to update widget details.
	 * This will be invoked when updating chart details in the portlet
	 * @param portlet
	 * @throws Exception
	 */
	void updateWidget(Portlet portlet)throws DataAccessException;

	/**
	 * Service to update chart title
	 * @param portlet
	 */
	void updateWidgetTitle(Portlet portlet)throws DataAccessException;
}
