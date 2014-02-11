package org.hpccsystems.dashboard.controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map; 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.ChartPanel;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.helper.DashboardHelper;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkmax.ui.event.PortalMoveEvent;
import org.zkoss.zkmax.zul.Navbar;
import org.zkoss.zkmax.zul.Navitem;
import org.zkoss.zkmax.zul.Portalchildren;
import org.zkoss.zkmax.zul.Portallayout;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Window;

/**
 * DashboardController class is used to add new dashboard into sidebar and 
 *  controller class for dashboard.zul.
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DashboardController extends SelectorComposer<Component>{

	private static final long serialVersionUID = 1L;
	private static final  Log LOG = LogFactory.getLog(DashboardController.class); 
	
	private Dashboard dashboard; 
	private Integer oldColumnCount = null;
	
	Integer dashboardId = null;

	@Wire
	Label nameLabel;
	
    @Wire
    Window dashboardWin;
    
    @Wire 
    Button deleteDashboard;
    @Wire
    Button configureDashboard;
    @Wire
    Button addWidget;    
    @Wire
    Button saveApiView;
    
    @Wire("portallayout")
	Portallayout portalLayout;
    
	@Wire("portalchildren")
    List<Portalchildren> portalChildren;
	
    Integer panelCount = 0;
    
    private static final String PERCENTAGE_SIGN = "%";
    
    @WireVariable
    private AuthenticationService authenticationService;
    
    @WireVariable
	private DashboardService dashboardService;
    
    @WireVariable
   	private WidgetService widgetService;
    
    @WireVariable
	private ChartRenderer chartRenderer;
    
    @WireVariable
	HPCCService hpccService;
    
    @WireVariable
	private DashboardHelper dashboardHelper;
    
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		super.doAfterCompose(comp);
		dashboardId =(Integer) Executions.getCurrent().getAttribute(Constants.ACTIVE_DASHBOARD_ID);
		//For the first Dashboard, getting Id from Session
		if(dashboardId == null ){
			dashboardId = (Integer) Sessions.getCurrent().getAttribute(Constants.ACTIVE_DASHBOARD_ID);
		}
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Dashboard ID - " + dashboardId);
		}
						
		if(dashboardId != null ){
			List<String> dashboardIdList = new ArrayList<String>(); 
			dashboardIdList.add(String.valueOf(dashboardId));
			List<Dashboard> dashboardList =null;
			try{
				dashboardList = dashboardService.retrieveDashboardMenuPages(
						authenticationService.getUserCredential().getApplicationId(),
						authenticationService.getUserCredential().getUserId(),
						dashboardIdList,null);				
			}catch(Exception ex){
				Clients.showNotification(
						"Unable to retrieve selected Dashboard details from DB ",
						"error", comp, "middle_center", 3000, true);
				LOG.error("Exception while fetching widget details from DB", ex);
			}			
			
			if(dashboardList != null && dashboardList.size() > 0){
				dashboard = dashboardList.get(0);
				dashboard.setPersisted(true);
			}
			if(LOG.isDebugEnabled()){
				LOG.debug("dashboardList in DashboardController.doAfterCompose()-->"+dashboardList);
				LOG.debug("Creating dashboard - Dashboard Id " + dashboardId);
				LOG.debug("Persistance - " + dashboard.isPersisted());
			}
			nameLabel.setValue(dashboard.getName());
			
			//Preparing the layout
			Integer count = 0;
			for (Portalchildren portalchildren : portalChildren) {
				if( count < dashboard.getColumnCount()) {
					portalchildren.setVisible(true);
					portalchildren.setWidth(100/dashboard.getColumnCount() + PERCENTAGE_SIGN);
				}
				count ++;
			}		

			try	{
				dashboard.setPortletList((ArrayList<Portlet>) widgetService.retriveWidgetDetails(dashboardId));
			} catch(Exception ex) {
				Clients.showNotification(
						"Unable to retrieve Widget details from DB for the Dashboard",
						"error", comp, "middle_center", 3000, true);
				LOG.error("Exception while fetching widget details from DB", ex);
			}
			
			if(LOG.isDebugEnabled()){
				LOG.debug("PortletList of selected Dashboard -->"+dashboard.getPortletList());
			}
			
			XYChartData chartData = null;
			ChartPanel panel = null;
			for (Portlet portlet : dashboard.getPortletList()) {
				if(!portlet.getWidgetState().equals(Constants.STATE_DELETE)){
					//Constructing chart data only when live chart is drawn
					if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
						chartData = chartRenderer.parseXML(portlet.getChartDataXML());
						if(portlet.getChartType().equals(Constants.TABLE_WIDGET)){
							//Fetching data and setting into portlet to construct Table Widget
							try{
								portlet.setTableDataMap(hpccService.fetchTableData(chartData));
							}catch(Exception e){
								Clients.showNotification(
										"Unable to fetch table data from Hpcc ",
										"error", comp, "middle_center", 3000,true);
								LOG.error("Exception while fetching data from Hpcc for table columns", e);
							}
						} else {
							//For chart widgets
							try	{
								chartRenderer.constructChartJSON(chartData, portlet, false);
							}catch(Exception ex) {
								Clients.showNotification("Unable to fetch column data from Hpcc", 
										"error", comp, "middle_center", 3000, true);
								LOG.error("Exception while fetching column data from Hpcc", ex);
							}
						}
					}
					
					panel = new ChartPanel(portlet);
					portalChildren.get(portlet.getColumn()).appendChild(panel);
					if(panel.drawD3Graph() != null){
						Clients.evalJavaScript(panel.drawD3Graph());
					}
				}
			}
			
		} else {
			dashboardWin.setBorder("none");			
			return;
		}
		
		dashboardWin.addEventListener("onPortalClose", onPanelClose);
		dashboardWin.addEventListener("onLayoutChange", onLayoutChange);
		
		if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){
			saveApiView.addEventListener(Events.ON_CLICK, saveApiChanges); 
		}
		if(LOG.isDebugEnabled()){
			LOG.debug("Created Dashboard");
			LOG.debug("Panel Count - " + dashboard.getColumnCount());
		}
	}	
	
	@Listen("onClick = #addWidget")
	public void addWidget() {
		final Portlet portlet = new Portlet();
		
		portlet.setWidgetState(Constants.STATE_EMPTY);
		portlet.setPersisted(false);
		dashboard.getPortletList().add(portlet);
		
		// Adding new Widget to the column with lowest number of widgets
		Integer count = 0, childCount = 0, column = 0;
		for (Portalchildren portalchildren : portalChildren) {
			if(! (count < dashboard.getColumnCount())) {
				break;
			}
			if(portalchildren.getChildren().size() < childCount) {
				column = count;
			}
			childCount = portalchildren.getChildren().size();
			count ++;
		}
		portlet.setColumn(column);
		ChartPanel chartPanel = new ChartPanel(portlet);
		portalChildren.get(portlet.getColumn()).appendChild(chartPanel);
		chartPanel.focus();
		
		manipulatePortletObjects(Constants.ReorderPotletPanels);
		
		try {
			widgetService.addWidget(dashboardId, portlet, dashboard.getPortletList().indexOf(portlet));
		} catch (Exception e) { 
			LOG.error("Exception while adding widgets into dashboard", e);
		}
	}
	
	@Listen("onClick = #configureDashboard")
	public void configureDashboard(Event event) {
		oldColumnCount = dashboard.getColumnCount();
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(Constants.PARENT, dashboardWin);
		parameters.put(Constants.DASHBOARD, dashboard);
		
		Window window  = (Window) Executions.createComponents("/demo/layout/dashboard_config.zul", dashboardWin, parameters);
		window.doModal();
	}
	
	public void manipulatePortletObjects(short option) {
		
		switch(option)
		{
			case Constants.ReorderPotletPanels:
				if(LOG.isDebugEnabled()) {
					LOG.debug("Reordering portlets.");
					LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
				}
				ArrayList<Portlet> newPortletList = new ArrayList<Portlet>();
				short portletChild=0;int colCount=0;Iterator<Component> iterator=null;
				
				Component component=null;
				Portlet portlet=null;
				do
				{
					if(portalChildren.get(portletChild).getChildren().size()>0)
					{
						iterator = (Iterator<Component>) portalChildren.get(portletChild).getChildren().iterator();
						while(iterator.hasNext()){
							 component = iterator.next();
							 portlet = ((ChartPanel)component).getPortlet();
							 portlet.setColumn(colCount);
							 newPortletList.add(portlet);
						 }
						colCount++;
					}
					portletChild++;
					
				}while(portletChild<3);
				
				dashboard.setPortletList(newPortletList);
			break;
			
			case Constants.ResizePotletPanels:
				if(LOG.isDebugEnabled()) {
					LOG.debug("Resizing portlet children");
					LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
				}
				Integer counter = 0;
				for(final Portalchildren portalChildren : this.portalChildren) {
					if(counter < dashboard.getColumnCount()){
						portalChildren.setVisible(true);
						portalChildren.setWidth((100/dashboard.getColumnCount()) + PERCENTAGE_SIGN);
						final List<Component> list = portalChildren.getChildren();
						for (final Component component1 : list) {
								final ChartPanel panel = (ChartPanel) component1;
								if(panel.drawD3Graph() != null) {
									Clients.evalJavaScript(panel.drawD3Graph());
								}
						}
					} else {
						portalChildren.setVisible(false);
					}
					counter ++;
				}
			break;
		}
	}
	
	/**
	 * Event listener to listen to 'Dashboard Configuration'
	 */
	final EventListener<Event> onLayoutChange = new EventListener<Event>() {

		@Override
		public void onEvent(Event event) throws Exception {
			// Check if any visible panels are hidden when layout is changed
			if(dashboard.getColumnCount() < oldColumnCount) {
				//List to capture hidden panels
				List<Component> hiddenPanels = new ArrayList<Component>();
				
				Integer counter = 0;
				for (Portalchildren component : portalChildren) {
					if( !(counter < dashboard.getColumnCount()) ) {
						hiddenPanels.addAll(component.getChildren());
						component.getChildren().clear();
					}
					counter ++;
				}
				
				//Adding hidden panels to last visible column 
				for (Component component : hiddenPanels) {
					if(component instanceof ChartPanel) {
						portalChildren.get(dashboard.getColumnCount() -1).appendChild(component);
					}
				}
			}
			
			//To update Dashboard Name
			onNameChange();
			
			manipulatePortletObjects(Constants.ReorderPotletPanels);
			manipulatePortletObjects(Constants.ResizePotletPanels);
			
			//updating Dashboard details
			dashboard.setUpdatedDate(new Date(new java.util.Date().getTime()));
			dashboardService.updateDashboard(dashboard);
			
			//updating Widget sequence
			widgetService.updateWidgetSequence(dashboard);
		}
		
	};

	
	
	/**
	 *  Hides empty Portletchildren
	 */
	final EventListener<Event> onPanelClose = new EventListener<Event>() {

		public void onEvent(final Event event) throws Exception {
			if(LOG.isDebugEnabled()) {
				LOG.debug("hide portlet event");
			}
			manipulatePortletObjects(Constants.ReorderPotletPanels);
			manipulatePortletObjects(Constants.ResizePotletPanels);
			if(LOG.isDebugEnabled()) {
				LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
			}
		}
	};	
	
	@Listen("onPortalMove = portallayout")
	public void onPanelMove(final PortalMoveEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("onPanelMove");
		}
		final ChartPanel panel = (ChartPanel) event.getDragged();
		if(panel.drawD3Graph() != null)
			Clients.evalJavaScript(panel.drawD3Graph());
		
		manipulatePortletObjects(Constants.ReorderPotletPanels);
		manipulatePortletObjects(Constants.ResizePotletPanels);
	}
	
	
	public void onNameChange() {
		nameLabel.setValue(dashboard.getName());
		
		final Navbar navBar=(Navbar)Sessions.getCurrent().getAttribute(Constants.NAVBAR);
		final List<Component> childNavBars = navBar.getChildren(); 
		Integer navDashId=0;Navitem dashBoardObj=null;
        for (final Component childNavBar : childNavBars) {
        	if(childNavBar instanceof Navitem){
        		dashBoardObj = (Navitem) childNavBar;
        		navDashId =  (Integer) dashBoardObj.getAttribute(Constants.DASHBOARD_ID);
        		if(dashboard.getDashboardId().equals(navDashId))
        		{
        			dashBoardObj.setLabel(dashboard.getName());
        			break;
        		}
        	}
        }
	}
	
	/**
	 * deleteDashboard() is used to delete the selected Dashboard in the sidebar page.
	 */
	@Listen("onClick = #deleteDashboard")
	public void deleteDashboard() {
		
		 // ask confirmation before deleting dashboard
		 EventListener<ClickEvent> clickListener = new EventListener<Messagebox.ClickEvent>() {
			 public void onEvent(ClickEvent event) throws Exception {
				 ArrayList<Portlet> portletlist = dashboard.getPortletList();
	             if(Messagebox.Button.YES.equals(event.getButton())) {
	               	final Navbar navBar = (Navbar) Sessions.getCurrent().getAttribute(Constants.NAVBAR);
	           		final List<Component> childNavBars = navBar.getChildren();
	           		
	           		Integer navDashId = 0;
	           		Navitem navItem = null;
	           		Navitem navItemToDelete = null;
	           		boolean getFirstNavItem = false;
	           		Navitem firtNavItem = null;
	           		
	           		for (Component component : childNavBars) {
	           			if(component instanceof Navitem) {
	           				navItem = (Navitem) component;
	           				navDashId = (Integer) navItem.getAttribute(Constants.DASHBOARD_ID);
	           				if(navItem.isVisible() && !getFirstNavItem && !navItem.isSelected()){
	           					firtNavItem = navItem;
	           					getFirstNavItem = !getFirstNavItem;
	           				}
	           				if (dashboard.getDashboardId().equals(navDashId) && navItem.isSelected()) {
	           					navItemToDelete = navItem;
	           	           		portletlist.clear(); 
	           				}
	           			}
	           		}
	           		final Include include = (Include) Selectors.iterable(navItemToDelete.getPage(), "#mainInclude")
	           				.iterator().next();
	           		
	           		navItemToDelete.setVisible(false);
	           		if(getFirstNavItem) {
	           			if(LOG.isDebugEnabled()){
	           				LOG.debug("Setting first Nav item as active");
	           			}
	           			firtNavItem.setSelected(true);
	           			Events.sendEvent(Events.ON_CLICK, firtNavItem, null);
	           		} else {
	           			Sessions.getCurrent().setAttribute(Constants.ACTIVE_DASHBOARD_ID, null);
	           			//Detaching the include and Including the page again to trigger reload
	           			final Component component2 = include.getParent();
	           			include.detach();
	           			final Include newInclude = new Include("/demo/layout/dashboard.zul");
	           			newInclude.setId("mainInclude");
	           			component2.appendChild(newInclude);
	           			Clients.evalJavaScript("showPopUp()");
	           		}	           		
	           		dashboardService.deleteDashboard(dashboard.getDashboardId(),authenticationService.getUserCredential().getUserId());
	             }

	           } 
	       };
	       
       Messagebox.show(Constants.DELETE_DASHBOARD, Constants.DELETE_DASHBOARD_TITLE, new Messagebox.Button[]{
               Messagebox.Button.YES, Messagebox.Button.NO }, Messagebox.QUESTION, clickListener);
   
  }
	/**
	 * Event listener to listen to 'ViewDashboard API request changes'
	 */
	final EventListener<Event> saveApiChanges = new EventListener<Event>() {

		@Override
		public void onEvent(Event event) throws Exception {
			final List<Dashboard> dashBoardIdList = dashboardHelper.getSessionDashboardList();
			try
			{
				dashboardHelper.updateDashboardWidgetDetails(dashBoardIdList);
				Messagebox.show("Your Dahboard details are Saved.You can close the window","",1,Messagebox.ON_OK);
				
			}catch(Exception ex)
			{
				Clients.showNotification("Unable to update Dahboard details into DB", "error", dashboardWin.getParent(), "middle_center", 3000, true);
	        	LOG.error("Exception saveApiChanges Listener in DashboardController", ex);
			}
			finally{				
				authenticationService.logout(null);
			}
		
		}
	};	
}
