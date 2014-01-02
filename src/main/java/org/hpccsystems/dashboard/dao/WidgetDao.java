package org.hpccsystems.dashboard.dao;

import java.sql.SQLException;
import java.util.List;

import org.hpccsystems.dashboard.entity.Portlet;

/**
 *  Dao class,has abstract methods for Widget related DB hits
 * @author 
 *
 */
public interface WidgetDao {
	

	/**
	 * Retrives Widget list from DB
	 * 
	 * @param dashboardId
	 * @return
	 * 	A list of Portlet objects corresponding to the provided Dashboard Id
	 * @throws SQLException
	 */
	List<Portlet> retriveWidgetDetails(Integer dashboardId) throws SQLException;
	
	/**
	 * @param dashboardId
	 * @param portlets
	 * @param widgetSequence
	 */
	void updateWidgetDetails(Integer dashboardId,List<Portlet> portlets)throws SQLException;
	/**
	 * @param dashboardId
	 * @param portlets
	 * @param widgetSequence
	 */
	void addWidgetDetails(Integer dashboardId,final List<Portlet> portlets)throws SQLException;
	/**
	 * @param dashboardId
	 * @param portlets
	 * @throws SQLException
	 */
	void deleteWidgets(Integer dashboardId, List<Portlet> portlets)throws SQLException;
}
