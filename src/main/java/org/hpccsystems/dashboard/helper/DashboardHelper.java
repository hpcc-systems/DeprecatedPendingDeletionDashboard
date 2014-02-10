package org.hpccsystems.dashboard.helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.LogoutController;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;

/**
 * Helper Class to implement commons function of Dashboard
 *
 */
public class DashboardHelper {
	
	private static final long serialVersionUID = 1L;	
	private static final  Log LOG = LogFactory.getLog(LogoutController.class);	
	
	private DashboardService dashboardService;
	
	@Autowired
	public void setDashboardService(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}
	
	private WidgetService widgetService;
	
	@Autowired
	public void setWidgetService(WidgetService widgetService) {
		this.widgetService = widgetService;
	}
	
	private AuthenticationService authenticationService;
	
	@Autowired
	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}


	/**
	 * Method to get Dashboards list from session with updated name
	 * @return List<Dashboard>
	 */
	public List<Dashboard> getSessionDashboardList()throws Exception
	{
		final List<Dashboard> dashBoardIdList = new ArrayList<Dashboard>();
		final Session session = Sessions.getCurrent(); 
		final Navbar navBar = (Navbar) Sessions.getCurrent().getAttribute(Constants.NAVBAR);
		if(LOG.isDebugEnabled()){
			LOG.debug("Control in DashboardHelper");
			LOG.debug("NavBar details while logut the application" +navBar);
		}
		final List<Component> childNavBars = navBar.getChildren();
		
		Map<Integer, Dashboard> dashboardMap = null;
		Navitem navitem=null;
		Dashboard dashBoard = null;
		
        if(session.getAttribute(Constants.DASHBOARD_LIST) != null){
			dashboardMap = (HashMap<Integer, Dashboard>) session.getAttribute(Constants.DASHBOARD_LIST);
		}
        if(dashboardMap != null)
        {
        for (final Component childNavBar : childNavBars) {
        	if(childNavBar instanceof Navitem){
        		navitem = (Navitem) childNavBar;
        		dashBoard = dashboardMap.get(navitem.getAttribute(Constants.DASHBOARD_ID));
        		dashBoard.setName(navitem.getLabel());
        		dashBoard.setUpdatedDate(new Date(Calendar.getInstance().getTime().getTime()));
        		dashBoardIdList.add(dashBoard);
        	}
        	}
        }	
        return dashBoardIdList;
	}
	
	/**
	 * Method to update Dashboard & widget details into DB 
	 * @param dashBoardIdList
	 */
	public void updateDashboardWidgetDetails(List<Dashboard> dashBoardIdList) throws Exception
	{
		final String userId = authenticationService.getUserCredential().getUserId();
			if (dashBoardIdList != null && !dashBoardIdList.isEmpty()) {
				for (int count = 1; count <= dashBoardIdList.size(); count++) {
					Dashboard dashboard = dashBoardIdList.get(count - 1);
					List<Portlet> portletList = dashboard.getPortletList();
					 if(LOG.isDebugEnabled()){
					LOG.debug("Portlet list size ->" + portletList);
					 }
					if (Constants.STATE_DELETE.equals(dashboard.getDashboardState())) {
						dashboardService.deleteDashboard(dashboard.getDashboardId(),userId);
					}
					else if(portletList.isEmpty()) {
						String dashboardState = "";
						if (Constants.STATE_EMPTY.equals(dashboard.getDashboardState())) {
							dashboardState = Constants.STATE_EMPTY;							
						}
						dashboardService.updateDashboardState(
								dashboard.getDashboardId(),
								dashboardState, count,
								dashboard.getName(),
								dashboard.getUpdatedDate());
						
					} else if (!portletList.isEmpty()) {
						// Dashboard is already persisted in the table
						// update dashboard sequence and name,columncount
						dashboardService.updateDashboardDetails(
								dashboard.getDashboardId(), count,
								dashboard.getName(),
								dashboard.getColumnCount(),
								dashboard.getUpdatedDate());
						if(LOG.isDebugEnabled()){
							LOG.debug("portletList ->" + portletList); 
						}
						List<Portlet> persistedPortlets = new ArrayList<Portlet>();
						List<Portlet> newPortlets = new ArrayList<Portlet>();
						List<Portlet> deletPortlets = new ArrayList<Portlet>();
						if (dashboard.isPersisted()) {
							
							for (int index = 1; index <= portletList.size(); index++) {
								Portlet portlet = portletList.get(index - 1);
								if(LOG.isDebugEnabled()){
									LOG.debug("Portlet State ->" + portlet.getWidgetState());
								}
								if(portlet.isPersisted() && Constants.STATE_DELETE.equals(portlet.getWidgetState())){
									deletPortlets.add(portlet);
								}
								else if (portlet.isPersisted() && !Constants.STATE_DELETE.equals(portlet.getWidgetState())) {
									persistedPortlets.add(portlet);									
								} else if(!portlet.isPersisted() && !Constants.STATE_DELETE.equals(portlet.getWidgetState())){
									// insert portlet details
									newPortlets.add(portlet);	
								}
							}
						}
						// Newly added Dashboard which is persisted in dashboard
						// table but don't have any portlets
						else if (!dashboard.isPersisted()) {
							
							for (int index = 1; index <= portletList.size(); index++) {
								// insert portlet details into widget table
								Portlet portlet = portletList.get(index - 1);
								if(!"D".equals(portlet.getWidgetState())){
									newPortlets.add(portlet);	
								}
							}
						}
						if(LOG.isDebugEnabled()){
							LOG.debug("persistedPortlets --->" + persistedPortlets);
							LOG.debug("newPortlets --->" + newPortlets);
							LOG.debug("deletPortlets --->" + deletPortlets);
						}
						// update portlet details into widget table
						if(persistedPortlets.size() > 0)
						{
							widgetService.updateWidgetDetails(dashboard.getDashboardId(), persistedPortlets);
						}
						// insert portlets details into widget table
						if(newPortlets.size() > 0)
						{
							widgetService.addWidgetDetails(dashboard.getDashboardId(), newPortlets);
						}
						if(deletPortlets.size() >0)
						{
							widgetService.deleteWidgets(dashboard.getDashboardId(), deletPortlets);
						}

					}
				}
			}
		
	}

}
