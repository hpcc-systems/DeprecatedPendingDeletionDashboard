package org.hpccsystems.dashboard.controller;

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
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
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
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Textbox;
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
	
	private Dashboard dashboard ; 
	
	Integer dashboardId = null;
	
    @Wire
    Textbox dashboardTitleTxt;
    
    @Wire
    Window dashboardWin;
    
    @Wire
    Button addPanel;
    @Wire
    Button addColPanel;
    @Wire 
    Button deleteDashboard;
    
    @Wire("portallayout")
	Portallayout portalLayout;
    
	@Wire("portalchildren")
    List<Portalchildren> portalChildren;
    Integer pcCount= 0;
    Integer panelCount = 0;
    
    private static final String PERCENTAGE_SIGN = "%";
    
    @WireVariable
	private DashboardService dashboardService;
    
    @WireVariable
   	private WidgetService widgetService;
    
    @WireVariable
	private ChartRenderer chartRenderer;
    
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		//TODO Delete dashboard state is not considered right now 
		
		super.doAfterCompose(comp);
				
		
		final Session session = Sessions.getCurrent();
		dashboardId = (Integer) session.getAttribute(Constants.ACTIVE_DASHBOARD_ID);
		final Map<Integer,Dashboard> dashboardMap = (HashMap<Integer, Dashboard>) session.getAttribute(Constants.DASHBOARD_LIST);
		
		if(dashboardId != null && dashboardMap != null){
			dashboard = dashboardMap.get(dashboardId);
			
			if(LOG.isDebugEnabled()){
				LOG.debug("Creating dashboard - Dashboard Id " + dashboardId);
				LOG.debug("Persistance - " + dashboard.isPersisted());
			}
			
			dashboardTitleTxt.setValue(dashboard.getName());
			
			dashboardTitleTxt.setVflex("1");
			dashboardTitleTxt.addEventListener(Events.ON_CHANGE, titleChangeLisnr);
			
			ArrayList<Portlet> portletlist = dashboard.getPortletList();
			
			if(portletlist.isEmpty() && !dashboard.isPersisted()){
				//Deciding Columns and rows to place Portlets
				if(dashboard.getLayout().equals(Constants.LAYOUT_1X2)){
					pcCount = 2;
					panelCount = 2;
				} else if (dashboard.getLayout().equals(Constants.LAYOUT_2X2)){
					pcCount = 2;
					panelCount = 4;
				} else if (dashboard.getLayout().equals(Constants.LAYOUT_3X3)){
					pcCount = 3;
					panelCount = 9;
				}
				
				if(LOG.isDebugEnabled()){
					LOG.debug("Creating A New Dashboard.. and adding panels");
				}
				dashboard.setColumnCount(pcCount);
				for(int i=1; i <= pcCount; i++){
					for(int j=1; j <= panelCount ; j++){
						if(j > (panelCount/pcCount)*(i-1)  && j <= (panelCount/pcCount)*i) {
							if(LOG.isDebugEnabled()){
								LOG.debug("Adding panel " + j);
							}
							final Portlet portlet = new Portlet();
							//generating portlet id
							Integer portletId = j ;
							portlet.setId(portletId);
							portlet.setColumn(i - 1);
							portlet.setWidgetState(Constants.STATE_EMPTY);
							dashboard.getPortletList().add(portlet);
							portalChildren.get(i- 1).appendChild(new ChartPanel(portlet));
							portalChildren.get(i- 1).setVisible(true);
						} else{
							continue;
						}	
					}
					portalChildren.get(i - 1).setWidth( 100/pcCount + PERCENTAGE_SIGN);
				}
			} else if( !portletlist.isEmpty() ) {
				//Dashboard is present in session
				if(LOG.isDebugEnabled()){
					LOG.debug("Creating Dashboard present in session. \nNumber of columns in Session -- " + dashboard.getColumnCount());
				}
				pcCount = dashboard.getColumnCount();
				final Iterator<Portlet> iterator = portletlist.iterator();
				while(iterator.hasNext()){
					final Portlet portlet = iterator.next();
					if(!portlet.getWidgetState().equals(Constants.STATE_DELETE)){
						final ChartPanel panel = new ChartPanel(portlet);
						portalChildren.get(portlet.getColumn()).appendChild(panel);
						portalChildren.get(portlet.getColumn()).setWidth(100/pcCount + PERCENTAGE_SIGN);
						portalChildren.get(portlet.getColumn()).setVisible(true);
						if(panel.drawD3Graph() != null){
							Clients.evalJavaScript(panel.drawD3Graph());
						}
					}
				}
			} else if(portletlist.isEmpty() && dashboard.isPersisted()) {
				//Dashboard is persisted but not present in session
				//Webservices are called to retrive chart data
				if(LOG.isDebugEnabled()){
					LOG.debug("Creating Dashboard from DB.");
				}
				portletlist.addAll(widgetService.retriveWidgetDetails(dashboardId));
				
				pcCount = dashboard.getColumnCount();
				XYChartData chartData = null;
				ChartPanel panel = null;
				for (Portlet portlet : portletlist) {
					if(!portlet.getWidgetState().equals(Constants.STATE_DELETE)){
						//Constructing chart data only when live chart is drawn
						if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
							chartData = chartRenderer.parseXML(portlet.getChartDataXML());
							chartRenderer.constructChartJSON(chartData, portlet, false);
						}
						
						panel = new ChartPanel(portlet);
						portalChildren.get(portlet.getColumn()).appendChild(panel);
						portalChildren.get(portlet.getColumn()).setWidth(100/pcCount + PERCENTAGE_SIGN);
						portalChildren.get(portlet.getColumn()).setVisible(true);
						
						if(panel.drawD3Graph() != null){
							Clients.evalJavaScript(panel.drawD3Graph());
						}
					}
				}
			}
			
		} else {
			dashboardWin.setBorder("none");
			addPanel.setVisible(false);
			deleteDashboard.setVisible(false);
			return;
		}
		
		dashboardWin.addEventListener("onPortalClose", onPanelClose);
		
		if(pcCount < 3){
			addColPanel.setVisible(true);
		}
		
		if(LOG.isDebugEnabled()){
			LOG.debug("Created Dashboard");
			LOG.debug("Panel Count - " + pcCount);
		}
	}	
	

	@Listen("onClick = #addPanel")
	public void addPanelClick() {
		final Portlet portlet = new Portlet();
		
		//generating portlet id
		int nextPortletSeq = dashboard.getPortletList().size()+1;
		portlet.setId(nextPortletSeq);
		
		portlet.setWidgetState(Constants.STATE_EMPTY);
		portlet.setPersisted(false);
		portlet.setColumn(pcCount -1);
		dashboard.getPortletList().add(portlet);
		portalChildren.get(pcCount - 1).appendChild(new ChartPanel(portlet));
	}
	
	@Listen("onClick = #addColPanel")
	public void addColumnAndPanelClick() {
		pcCount ++;
		portalChildren.get(pcCount -1).setVisible(true);
		dashboard.setColumnCount(pcCount);
		addPanelClick();
		if(!(pcCount < 3)){
			addColPanel.setVisible(false);
		}
		manipulatePortletObjects(Constants.ResizePotletPanels);
	}
	
	public Map<Object,Object> getPortletObjects(short option) {
		
		HashMap<Object, Object> portletObj=new HashMap<Object, Object>();
		switch(option)
		{
			case Constants.NonEmptyPortChild:
				Integer colCount = 0;
				for(Portalchildren portalChildren : this.portalChildren) {
					if(portalChildren.getChildren().size() > 0 ) {
						colCount ++;
					}
				}
				portletObj.put(Constants.NonEmptyPortChild, colCount);
			break;
		}
		return portletObj;
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
				
				// Adding portlets in deleted state to the new list
				for (Portlet portlet2 : dashboard.getPortletList()) {
					if(Constants.STATE_DELETE.equals(portlet2.getWidgetState())) {
						newPortletList.add(portlet2);
					}
				}
				
				dashboard.setPortletList(newPortletList);
			break;
			
			case Constants.ResizePotletPanels:
				if(LOG.isDebugEnabled()) {
					LOG.debug("Resizing portlet children");
					LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
				}
				LOG.debug("Resizing portlets. Portlet Child coiunt now - " + pcCount);
				for(final Portalchildren portalChildren : this.portalChildren) {
					if(portalChildren.getChildren().size() > 0){
						portalChildren.setVisible(true);
						portalChildren.setWidth((100/pcCount) + PERCENTAGE_SIGN);
						final List<Component> list = portalChildren.getChildren();
						for (final Component component1 : list) {
								final ChartPanel panel = (ChartPanel) component1;
								if(panel.drawD3Graph() != null) {
									Clients.evalJavaScript(panel.drawD3Graph());
								}
						}
					} else {
						portalChildren.detach();
					}
				}
			break;
			
			case Constants.updateNonEmptyPortletCnt:
				if(LOG.isDebugEnabled()) {
					LOG.debug("Updating non-empty portlet count.");
					LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
				}
				Integer columnCount=(Integer)getPortletObjects(Constants.NonEmptyPortChild).get(Constants.NonEmptyPortChild);
				pcCount = columnCount;
				dashboard.setColumnCount(columnCount);
			break;
			
			case Constants.appendPortelChidren:
				
				if(LOG.isDebugEnabled()) {
					LOG.debug("Appending extra Portlet Children (Columns)");
					LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
				}
				
				int noOfPortelChldrnToAdd=3-pcCount;
				Portalchildren portelChildren=null;
				do
				{
					if(noOfPortelChldrnToAdd>0)
					{
						portelChildren=new Portalchildren();
						portelChildren.setVisible(false);
						portelChildren.setWidth(0+PERCENTAGE_SIGN);
						portalLayout.appendChild(portelChildren);
						noOfPortelChldrnToAdd--;
					}
				}while(noOfPortelChldrnToAdd>0);
			break;
			
			case Constants.hideShowAddPanelIcon:
				if(LOG.isDebugEnabled()) {
					LOG.debug("Hide / Show Add Widget Icons");
					LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
				}
				
				if(pcCount<1)
				{
					addPanel.setVisible(false);
					addColPanel.setVisible(true);
				}
				else if(pcCount>2)
				{
					addPanel.setVisible(true);
					addColPanel.setVisible(false);
				}
				else if(pcCount>0  && pcCount<3)
				{
					addPanel.setVisible(true);
					addColPanel.setVisible(true);
				}
			break;
		}
	}
	
	/**
	 *  Hides empty Portletchildren
	 */
	final EventListener<Event> onPanelClose = new EventListener<Event>() {

		public void onEvent(final Event event) throws Exception {
			if(LOG.isDebugEnabled()) {
				LOG.debug("hide portlet event");
			}
			manipulatePortletObjects(Constants.updateNonEmptyPortletCnt);
			manipulatePortletObjects(Constants.ReorderPotletPanels);
			manipulatePortletObjects(Constants.ResizePotletPanels);
			manipulatePortletObjects(Constants.appendPortelChidren);
			manipulatePortletObjects(Constants.hideShowAddPanelIcon);
			if(LOG.isDebugEnabled()) {
				LOG.debug("Now the portlet size is -> " + DashboardController.this.dashboard.getPortletList().size());
			}
		}
	};	
	
	@Listen("onPortalMove = portallayout")
	public void onPanelMove(final PortalMoveEvent event) {
		if(LOG.isDebugEnabled())
		{
			LOG.debug("onPanelMove");
		}
		final ChartPanel panel = (ChartPanel) event.getDragged();
		if(panel.drawD3Graph() != null)
			Clients.evalJavaScript(panel.drawD3Graph());
		manipulatePortletObjects(Constants.updateNonEmptyPortletCnt);
		manipulatePortletObjects(Constants.ReorderPotletPanels);
		manipulatePortletObjects(Constants.ResizePotletPanels);
		manipulatePortletObjects(Constants.appendPortelChidren);
		manipulatePortletObjects(Constants.hideShowAddPanelIcon);
	}
	
	//Event Listener for Change of dashboard title text
	final EventListener<Event> titleChangeLisnr = new EventListener<Event>() {
		public void onEvent(final Event event) throws Exception {
			if(LOG.isDebugEnabled())
			{
				LOG.debug("Dashboard Title is being changed");
				LOG.debug("dashboardId:"+ dashboard.getDashboardId());
				LOG.debug("dashboardTitleTxtold:"+dashboard.getName());
				LOG.debug("dashboardTitleTxtnew:"+dashboardTitleTxt.getValue());
			}
			dashboard.setName(dashboardTitleTxt.getValue());
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
		
	};
	
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
	           		
	           		dashboard.setDashboardState(Constants.STATE_DELETE);
	             }

	           } 
	       };
	       
       Messagebox.show(Constants.DELETE_DASHBOARD, Constants.DELETE_DASHBOARD_TITLE, new Messagebox.Button[]{
               Messagebox.Button.YES, Messagebox.Button.NO }, Messagebox.QUESTION, clickListener);
   
  }
	
}
