package org.hpccsystems.dashboard.controller;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.helper.DashboardHelper;
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
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Include;
import org.zkoss.zul.Menuitem;


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
	
	@WireVariable
	private DashboardHelper dashboardHelper;
	
	@Wire
	Menuitem logout;
	
	@Listen("onClick=#logout")
	public void doLogout() throws Exception {
		try
        {
		final List<Dashboard> dashBoardIdList = dashboardHelper.getSessionDashboardList();		
        final String sourceid = (String) (Sessions.getCurrent().getAttribute("sourceid"));
        if(LOG.isDebugEnabled()){
        	LOG.debug("Source Id details while logut the application" +sourceid);
			LOG.debug("dashBoardIdList details while logut the application" +dashBoardIdList);
		}   
        //Call to DB update        
        	dashboardHelper.updateDashboardWidgetDetails(dashBoardIdList); 
        	authenticationService.logout(session.getAttribute("user"));
        } catch(Exception ex) {
        	Clients.showNotification("Error occurred while loging out. Please try again.", true);
			LOG.error("Exception while doing logout", ex);
			return;
        }      
           
        session.removeAttribute("user");
        if(LOG.isDebugEnabled()){
    		LOG.debug("Logged out sucessfully");
    		}
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
	
}	
