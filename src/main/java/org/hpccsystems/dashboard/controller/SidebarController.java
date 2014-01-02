package org.hpccsystems.dashboard.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.DashboardService;
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
		viewModel.setAppId((String) session.getAttribute("applnid"));
		
		User user = (User)session.getAttribute("user");
		final List<Dashboard> sideBarPageList = new ArrayList<Dashboard>(dashboardService.retrieveDashboardMenuPages(viewModel,user.getUserId()));
		
		Navitem firstNavitem = null; 
		Boolean firstSet = false;
		Dashboard entry=null;
		Navitem navitem=null;
		
		for (final Iterator<Dashboard> iter = sideBarPageList.iterator(); iter.hasNext();) {
			entry = (Dashboard) iter.next();
			navitem  = constructNavItem(entry.getDashboardId(), entry.getName(), entry.getColumnCount(), null);
			navBar.appendChild(navitem);

			// Retriving first NavItem, to set as default
			if(!firstSet){
				firstNavitem = navitem;
				firstSet = !firstSet;
			}
		}
		
		// Displaying first menu item as default page
		if(firstSet) {
			//Setting current dashboard in session will load it when page loads
			Sessions.getCurrent().setAttribute(Constants.ACTIVE_DASHBOARD_ID, firstNavitem.getAttribute(Constants.DASHBOARD_ID));
			firstNavitem.setSelected(true);
		}else {
			Clients.evalJavaScript("showPopUp()");
		}
		
		//Add dashboard
		addDash.addEventListener(Events.ON_CLICK, addDashboardBtnLisnr);
		
		//Setting to session for logout controller
		Sessions.getCurrent().setAttribute(Constants.NAVBAR, navBar);
	}

	/**
	 * Creates the side Navbar and its associated Dashboard object
	 * 
	 * @param dashboardId
	 * @param name
	 * @param columCount - must set null when creating dashboard from preset
	 * @param layout - must set null when not constructing from a preset
	 * @return
	 */
	private Navitem constructNavItem(final Integer dashboardId, final String name,	final Integer columnCount, final String layout) {
		
		final Navitem navitem = new Navitem();
		navitem.setLabel(name);
		
		//Constructing empty dashboards and Add it to session map
		final Dashboard dashboard = new Dashboard();
		dashboard.setName(name);
		
		//Column count will only be present for persisted Dashboards
		//Deciding weather the dashboard is persisted
		if(LOG.isDebugEnabled()) {
			LOG.debug("Constructing nav Bar");
			LOG.debug("Layout - " + layout);
			LOG.debug("Column Count - " + columnCount);
		}
		
		if(columnCount == null) {
			dashboard.setLayout(layout);
			dashboard.setPersisted(false);
		} else if (layout == null) {
			dashboard.setColumnCount(columnCount);
			dashboard.setPersisted(true);
		}
		dashboard.setDashboardId(dashboardId);
		
		Map<Integer, Dashboard> dashboardMap = new HashMap<Integer, Dashboard>();
		final Session session = Sessions.getCurrent(); 
		if(session.getAttribute(Constants.DASHBOARD_LIST) != null){
			dashboardMap = (HashMap<Integer, Dashboard>) session.getAttribute(Constants.DASHBOARD_LIST);
		} else {
			session.setAttribute(Constants.DASHBOARD_LIST, dashboardMap);
		}		
		dashboardMap.put(dashboardId, dashboard);
		//Setting dashboard id to be retrived onClick
		navitem.setAttribute(Constants.DASHBOARD_ID, dashboardId);
		navitem.addEventListener(Events.ON_CLICK, navItemSelectLisnr);
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
					"/demo/layout/add_dash_board.zul", sidebarContainer,
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
		final Object mapObj = event.getData();
		String dashBoardName = "";
		String layoutType = "";
		String applnId = "";
		int dashboardId =0;
		if (mapObj instanceof Map) {
			Map<String, String> paramMap = (HashMap<String, String>) event.getData();
			if (paramMap != null) {
				dashBoardName = paramMap.get(Constants.DASHBOARD_NAME);
				layoutType = paramMap.get(Constants.DASHBOARD_LAYOUT);
				
			}
			// Make entry of new dashboard details into DB
			try {
				Session session = Sessions.getCurrent();
				applnId = (String) session.getAttribute("applnid");
				User user = (User)session.getAttribute("user");
				dashboardId = dashboardService.addDashboardDetails(applnId, dashBoardName,
						user.getUserId());
			} catch (SQLException exception) {
				if(LOG.isDebugEnabled()){
					LOG.debug("Exception In SideBar" +exception);
				}
			}
		}
		final Navitem navitem = constructNavItem(dashboardId, dashBoardName, null, layoutType);
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
						return;
					} else if(currentNavitem.equals(dragged)){
						navBar.insertBefore(dropped, dragged);
						return;
					}
				currentNavitem.addEventListener(Events.ON_CLICK, navItemSelectLisnr);
				}
			}
		}
	};
}
