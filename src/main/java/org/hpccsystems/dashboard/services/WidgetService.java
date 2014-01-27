package org.hpccsystems.dashboard.services;

import java.sql.SQLException;
import java.util.List;

import org.hpccsystems.dashboard.entity.Portlet;

/**
 * Service class,has abstract methods for Widget related services
 *
 */
public interface WidgetService {
	
	/**
	 * Retrives Widget list from DB
	 * 
	 * @param dashboardId
	 * @return
	 * 	A list of Portlet objects corresponding to the provided Dashboard Id
	 * @throws SQLException
	 */
	List<Portlet> retriveWidgetDetails(Integer dashboardId) throws Exception;
	
	/**
	 * @param dashboardId
	 * @param portlets
	 * @param widgetSequence
	 */
	void updateWidgetDetails(Integer dashboardId,List<Portlet> portlets)throws Exception;
	/**
	 * @param dashboardId
	 * @param portlets
	 * @param widgetSequence
	 */
	void addWidgetDetails(Integer dashboardId,List<Portlet> portlets)throws Exception;
	
	/**
	 * @param dashboardId
	 * @param portlets
	 * @throws SQLException
	 */
	void deleteWidgets(Integer dashboardId,List<Portlet> portlets)throws Exception;

}
