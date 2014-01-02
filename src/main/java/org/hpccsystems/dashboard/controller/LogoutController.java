package org.hpccsystems.dashboard.controller;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.Include;


/**
 * Controls User logout operation
 * Button group in the upper right corner is associated with this controller. This includes profile link. 
 *   
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class LogoutController extends SelectorComposer<Component> {
	 final Session session = Sessions.getCurrent(); 
	 String userId =((User) session.getAttribute("user")).getUserId();
	private static final  Log LOG = LogFactory.getLog(LogoutController.class);
	private static final long serialVersionUID = 1L;	
	@WireVariable
	private DashboardService dashboardService;
	
	@WireVariable
	private Dashboard dashBoardMenu;
	
	@WireVariable
	AuthenticationService authenticationService;
	
	@WireVariable
	private WidgetService widgetService;
	
	
	@Listen("onClick=#logout")
	public void doLogout() throws SQLException {
		
		final List<Dashboard> dashBoardIdList = new ArrayList<Dashboard>();
		final Navbar navBar = (Navbar) Sessions.getCurrent().getAttribute(Constants.NAVBAR);
		if(LOG.isDebugEnabled()){
			LOG.debug("Controll in Logout Controller");
			LOG.debug("NavBar details while logut the application" +navBar);
		}
        Dashboard dashBoard = null;
        final List<Component> childNavBars = navBar.getChildren(); 
        Navitem navitem=null;
        Map<Integer, Dashboard> dashboardMap = null;
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
        		dashBoardIdList.add(dashBoard);
        	}
        }
        }
        final String applnid = (String) (Sessions.getCurrent().getAttribute("applnid"));
        
        //Call to DB update
        updateDashboardWidgetDetails(dashBoardIdList); 
       
        if(LOG.isDebugEnabled()){
        	LOG.debug("Application Id details while logut the application" +applnid);
			LOG.debug("dashBoardIdList details while logut the application" +dashBoardIdList);
		}
        //dashboardService.addDashBoardPosition(applnid, dashBoardIdList);
        authenticationService.logout(session.getAttribute("user"));
        session.removeAttribute("user");
        Executions.sendRedirect("/demo/");
        Sessions.getCurrent().invalidate();
       
	}
	
	@Listen("onClick=#profile-link")
	public void onEvent(final Event arg0) throws Exception {
		// use iterable to find the first include only
		final Include include = (Include) Selectors.iterable(this.getPage(), "#mainInclude")
				.iterator().next();
		
		include.setSrc("/demo/profile-mvc.zul");
	}
	
	/**
	 * @param dashBoardIdList
	 */
	private void updateDashboardWidgetDetails(List<Dashboard> dashBoardIdList)
	{
		try {
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
						dashboardService.updateDashboardSate(
								dashboard.getDashboardId(),
								dashboardState, count,
								dashboard.getName());
						
					} else if (!portletList.isEmpty()) {
						// Dashboard is already persisted in the table
						// update dashboard sequence and name,columncount
						dashboardService.updateDashboardDetails(
								dashboard.getDashboardId(), count,
								dashboard.getName(),
								dashboard.getColumnCount());
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
								portlet.setWidgetSequence(index);
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
								portlet.setWidgetSequence(index);
								newPortlets.add(portlet);								
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
		catch (SQLException exception) {
			if(LOG.isDebugEnabled()){
				LOG.debug("Exception In SideBar" +exception);
			}
		}
		if(LOG.isDebugEnabled()){
		LOG.debug("Logged out sucessfully");
		}
	}
}	
