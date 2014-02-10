package org.hpccsystems.dashboard.controller;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SerializableEventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zul.Button;
import org.zkoss.zul.Center;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Window;

/**
 * SidebarController is used to handle the sidebar logic for Dashboard project
 *  and controller class for sidebar.zul.
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SidebarController extends GenericForwardComposer<Component>{

	private static final long serialVersionUID = 1L;
	
	private static final  Log LOG = LogFactory
			.getLog(SidebarController.class);
	
	//wire components
	@Wire
	Div sidebarContainer;
	@Wire
	Navbar navBar;
	@Wire
	Button addDash;
	
	@WireVariable
	private DashboardService dashboardService;
	
	@WireVariable
	AuthenticationService  authenticationService;
	
	@WireVariable
	WidgetService  widgetService;
	
	@Override
	public void doAfterCompose(final Component comp) throws Exception{
		super.doAfterCompose(comp);
		if(LOG.isDebugEnabled())
		{
		LOG.debug("Initiating sidebar page");
		}
		
		// Wire Spring Bean
		Selectors.wireVariables(navBar, this, Selectors.newVariableResolvers(getClass(), null));
		final Application viewModel = new Application();
		
		List<Dashboard> sideBarPageList = null;
		try	{
			//Circuit/External Source Flow	
			if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){
				sideBarPageList = getApiViewDashboardList(
						authenticationService.getUserCredential().getUserId(),
						authenticationService.getUserCredential().getApplicationId()
					);				
			} else {
				//Dashboard Flow
				//Add dashboard				
				addDash.addEventListener(Events.ON_CLICK, addDashboardBtnLisnr);				
				sideBarPageList =new ArrayList<Dashboard>(
						dashboardService.retrieveDashboardMenuPages(
								authenticationService.getUserCredential().getApplicationId(), 
								authenticationService.getUserCredential().getUserId(),
								null, null
							)
					);		

			}
		} catch(Exception ex) {
			Clients.showNotification("Unable to retrieve available Dashboards. Please try reloading the page.", true);
			LOG.error("Exception while retrieving dashboards from DB", ex);
		}
		
		Navitem firstNavitem = null; 
		Boolean firstSet = false;
		Dashboard entry=null;
		Navitem navitem=null;
		if(sideBarPageList != null){
		for (final Iterator<Dashboard> iter = sideBarPageList.iterator(); iter.hasNext();) {
			entry = (Dashboard) iter.next();
			entry.setPersisted(true);
			navitem  = constructNavItem(entry);
			navBar.appendChild(navitem);

			// Retriving first NavItem, to set as default
			if(!firstSet){
				firstNavitem = navitem;
				firstSet = !firstSet;
			}
		}}
		
		// Displaying first menu item as default page
		if(firstSet) {
			//Setting current dashboard in session will load it when page loads
			Sessions.getCurrent().setAttribute(Constants.ACTIVE_DASHBOARD_ID, firstNavitem.getAttribute(Constants.DASHBOARD_ID));
			firstNavitem.setSelected(true);
		}else {
			Clients.evalJavaScript("showPopUp()");
		}
		
		//Setting to session for logout controller
		Sessions.getCurrent().setAttribute(Constants.NAVBAR, navBar);
	}

	private Navitem constructNavItem(final Dashboard dashboard) {
		
		final Navitem navitem = new Navitem();
		navitem.setLabel(dashboard.getName());
				
		Map<Integer, Dashboard> dashboardMap = new HashMap<Integer, Dashboard>();
		final Session session = Sessions.getCurrent(); 
		if(session.getAttribute(Constants.DASHBOARD_LIST) != null){
			dashboardMap = (HashMap<Integer, Dashboard>) session.getAttribute(Constants.DASHBOARD_LIST);
		} else {
			session.setAttribute(Constants.DASHBOARD_LIST, dashboardMap);
		}		
		dashboardMap.put(dashboard.getDashboardId(), dashboard);
		
		//Setting dashboard id to be retrived onClick
		navitem.setAttribute(Constants.DASHBOARD_ID, dashboard.getDashboardId());
		
		if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){
			navitem.addEventListener(Events.ON_CLICK, apiNavItemSelectLisnr);
		}else{
			navitem.addEventListener(Events.ON_CLICK, navItemSelectLisnr);
		}
		navitem.setIconSclass("glyphicon glyphicon-stats");
		navitem.setZclass("list");
		
		navitem.setDraggable("true");
		navitem.setDroppable("true");
		navitem.addEventListener(Events.ON_DROP, onDropEvent);
		
		return navitem;
	}
	
	/**
	 * Listener for onClick in dashboard menus
	 */
	EventListener<Event> navItemSelectLisnr = new SerializableEventListener<Event>() {

		private static final long serialVersionUID = 1L;

		public void onEvent(final Event event) throws Exception {
			// use iterable to find the first include only
			final Include include = (Include) Selectors.iterable(sidebarContainer.getPage(), "#mainInclude")
					.iterator().next();
			//Setting currently active Dashboard session
			if(LOG.isDebugEnabled()){
				LOG.debug("Setting active dashboard to session" + event.getTarget().getAttribute(Constants.DASHBOARD_ID));
			}
			Sessions.getCurrent().setAttribute(Constants.ACTIVE_DASHBOARD_ID, event.getTarget().getAttribute(Constants.DASHBOARD_ID));
			//Detaching the include and Including the page again to trigger reload
			final Component component = include.getParent();
			include.detach();
			final Include newInclude = new Include("/demo/layout/dashboard.zul");
			newInclude.setId("mainInclude");
			component.appendChild(newInclude);
		}
	};
	
	/**
	 * Listener for onClick of a dashboard when request triggered from Circuit/external Source 
	 */
	EventListener<Event> apiNavItemSelectLisnr = new SerializableEventListener<Event>() {

		private static final long serialVersionUID = 1L;

		public void onEvent(final Event event) throws Exception {
			if(LOG.isDebugEnabled()){
				LOG.debug("Setting active dashboard to session in Api flow" + event.getTarget().getAttribute(Constants.DASHBOARD_ID));
			}
			//Setting currently active Dashboard in session
			Sessions.getCurrent().setAttribute(Constants.ACTIVE_DASHBOARD_ID, event.getTarget().getAttribute(Constants.DASHBOARD_ID));
			Iterator<Component> iterator = sidebarContainer.getParent().getParent().getFellows().iterator();
			Component centerComp =null;
			while(iterator.hasNext())
			{
				Component comp = iterator.next();
				if(comp instanceof Center)
				{
					centerComp = comp;
				}
			}	
			if(centerComp != null){
				Component childComp = centerComp.getFirstChild();
				centerComp.removeChild(childComp);
				final Include newInclude = new Include("/demo/layout/dashboard.zul");			
				newInclude.setId("mainInclude");
				centerComp.appendChild(newInclude);
			}
		}
	};
	
	/**
	 * Listener for onClick in profile
	 */
	EventListener<Event> profileSelectLisnr = new EventListener<Event>() {

		public void onEvent(final Event arg0) throws Exception {
			// use iterable to find the first include only
			final Include include = (Include) Selectors.iterable(sidebarContainer.getPage(), "#mainInclude")
					.iterator().next();
			
			include.setSrc("/demo/profile-mvc.zul");
		}
		
	};
	
	/**
	 * Listener for Add Dashboard Button
	 */
	EventListener<Event> addDashboardBtnLisnr = new EventListener<Event>() {

		public void onEvent(final Event event) throws Exception {
			// Defining parameters to send to Modal Dialog
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put(Constants.PARENT, sidebarContainer);

			final Window window = (Window) Executions.createComponents(
					"/demo/layout/dashboard_config.zul", sidebarContainer,
					parameters);
			window.doModal();
		}

	};
	
	/**
	 * Event to be triggered when add dashboard form gets submitted Adds a row
	 * to the sidebar
	 * 
	 * @param event
	 */
	public void onCloseDialog(final Event event) {
		
		final Dashboard dashboard = (Dashboard) event.getData();
		//updating dashboard sequence into dashboard_details
		Integer sequence=1;
		List<Component> comp = navBar.getChildren();
		for(Component component : comp){
			sequence++;
		}
		dashboard.setSequence(sequence);
		
		// Make entry of new dashboard details into DB
		try {
			dashboard.setDashboardId(
				dashboardService.addDashboardDetails(
					dashboard,
					authenticationService.getUserCredential().getApplicationId(),
					null,
					authenticationService.getUserCredential().getUserId()
				)
			);
			//adding widget details into db while adding new dashboard.
			widgetService.addWidgetDetails(dashboard.getDashboardId(), dashboard.getPortletList());
		} catch (Exception exception) {
			Clients.showNotification("Adding new Dashboard failed. Please try again", true);
			LOG.error("Exception while adding new dashboard to DB", exception);
			return;
		}
		
		dashboard.setPersisted(false);
		final Navitem navitem = constructNavItem(dashboard);
		navBar.appendChild(navitem);
		
		// Redirect to the recently added page
		Events.sendEvent(new Event("onClick", navitem));
		navitem.setSelected(true);
	}
	
	/**
	 * Method is used to drag and drop the sidebar's dashboard.
	 */
	EventListener<DropEvent> onDropEvent = new EventListener<DropEvent>() {
		public void onEvent(DropEvent event) throws Exception {
			final Navitem dragged = (Navitem) event.getDragged();
			final Navitem dropped = (Navitem) event.getTarget();
			final List<Component> list = navBar.getChildren();
			for (final Component component : list){
				if(component instanceof Navitem){
					final Navitem currentNavitem = (Navitem) component;
					if(currentNavitem.equals(dropped)){
						navBar.insertBefore(dragged, dropped);
						updateDashboardSequence();
						return;
					} else if(currentNavitem.equals(dragged)){
						navBar.insertBefore(dropped, dragged);
						updateDashboardSequence();
						return;
					}
					if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){
						currentNavitem.addEventListener(Events.ON_CLICK, apiNavItemSelectLisnr);
					}else{
						currentNavitem.addEventListener(Events.ON_CLICK, navItemSelectLisnr);
					}
				}
			}
		}
	};
	
	private void updateDashboardSequence() throws Exception {
		Integer dashboard_id = 1;
		List<Integer> dashboardList = new ArrayList<Integer>();
		for(Component navItem : navBar.getChildren()){
			final Navitem navItems = (Navitem) navItem;
			dashboard_id = (Integer)navItems.getAttribute(Constants.DASHBOARD_ID);
			dashboardList.add(dashboard_id);
			dashboardService.updateSidebarDetails(dashboardList);
		}
	}

	
	private List<Dashboard> getApiViewDashboardList(final String userId,final String applicationId)throws Exception {
		String[] DashboardIdArray = ((String[])Executions.getCurrent().getParameterValues(Constants.DB_DASHBOARD_ID));
		List<String> dashboardIdList =Arrays.asList(DashboardIdArray);
		
		if(LOG.isDebugEnabled()){
			LOG.debug("Requested Dashboard Id : "+dashboardIdList);
		}
		List<Dashboard> sideBarPageList =new ArrayList<Dashboard>(dashboardService.retrieveDashboardMenuPages(applicationId,userId,dashboardIdList,null));	
		if(LOG.isDebugEnabled()){
			LOG.debug("sideBarPageList: "+sideBarPageList);
		}
		return sideBarPageList;
		
	}
}
