package org.hpccsystems.dashboard.controller;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.api.entity.ChartConfiguration;
import org.hpccsystems.dashboard.api.entity.Field;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.Filter;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.XYModel;
import org.hpccsystems.dashboard.entity.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Vlayout;

import com.google.gson.GsonBuilder;

/**
 * EditChartController class is used to handle the edit page of the Dashboard
 * project and controller class for edit_portlet.zul file.
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditChartController extends SelectorComposer<Component> {
	
	private static final long serialVersionUID = 1L;
	
	private static final  Log LOG = LogFactory.getLog(EditChartController.class); 
	
	@WireVariable
	AuthenticationService  authenticationService;
	
	@WireVariable
	private DashboardService dashboardService;
	
	@WireVariable
	ChartRenderer chartRenderer;
	
	@WireVariable
	HPCCService hpccService;
	
	@WireVariable
	private WidgetService widgetService;

	@Wire
	Listbox measureListBox;
	
	@Wire
	Listbox attributeListBox;
	
	@Wire
	Listbox YAxisListBox;
	
	@Wire
	Listbox XAxisListBox;	
	
	@Wire
	Listbox filterListBox;	
	
	@Wire
	Button fectchFiles;	
	
	Boolean xAxisDropped = false;
	Boolean yAxisDropped = false;
	
	XYChartData chartData = new XYChartData();
	private Button doneButton;
	
	boolean isBarLinePieChart;	

	XYModel xyModal;
	Portlet portlet;
	
	@Wire
	Button apiSaveButton;
	@Wire
	Button apiCancelButton;
	@Wire
	Vlayout editWindowLayout;
	@Wire
	Button apiConfigSaveButton;
	
	List<String> parameterList = new ArrayList<String>();
	final Map<String, Object> parameters = new HashMap<String, Object>();
	private Dashboard dashboard;
	
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		super.doAfterCompose(comp);
		Map<String,String> columnSchemaMap = null;
		//API chart config flow without chart
		if(authenticationService.getUserCredential().getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID) && 
				authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)) {
			columnSchemaMap = configureChartSettingData();
			filterListBox.setStyle("backgroundr:gray;");
			filterListBox.invalidate();			
		}
		//API chart config flow with chart
		if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_CHART)) {
			configureDashboardPortlet();
		} else if (!authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)) {
			//Other flows:Dashboard chart edit flow & API Dashboard View flow
			portlet = (Portlet) Executions.getCurrent().getAttribute(Constants.PORTLET);
			chartData = (XYChartData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
			doneButton = (Button) Executions.getCurrent().getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
		}
		
		if(!authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)){
			// When live chart is present in ChartPanel
			if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
				for (String colName : chartData.getXColumnNames()) {
					createXListChild(colName);
				}
				for (String colName : chartData.getYColumnNames()) {
					createYListChild(colName);
				}
				xAxisDropped = true;
				yAxisDropped = true;
				filterListBox.setDroppable("true");
				XAxisListBox.setDroppable("false");
				
				validateDroppable();
	
				if(chartData.getIsFiltered()) {
					createFilterListItem(chartData.getFilter().getColumn());
					filterListBox.setDroppable("false");
				}
				
				constructChart();
				
			} 		
	
			try	{
				columnSchemaMap = hpccService.getColumnSchema(chartData.getFileName(), chartData.getHpccConnection());
			} catch(Exception e) {
				Clients.showNotification("Unable to fetch columns from HPCC", "error", comp, "middle_center", 3000, true);
				LOG.error(Constants.ERROR_RETRIEVE_COLUMNS, e);
			}
		}

		Listitem listItem;
		if(columnSchemaMap != null){
		for (Map.Entry<String, String> entry : columnSchemaMap.entrySet()) {
			listItem = new Listitem(entry.getKey());
			listItem.setDraggable("true");
			if(DashboardUtil.checkNumeric(entry.getValue())){
				listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.NUMERIC_DATA);
				listItem.setParent(measureListBox);
			} else {
				listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.STRING_DATA);
				listItem.setParent(attributeListBox);
			}
		}} 
	}	
	
	

	/**
	 * Method to render chart when item dropped in Y Axis
	 * @param dropEvent
	 */
	@Listen("onDrop = #YAxisListBox")
	public void onDropToYAxisTabBox(final DropEvent dropEvent) {

		final Listitem draggedListitem = (Listitem) ((DropEvent) dropEvent).getDragged();
		
		//Validations
		if(!Constants.NUMERIC_DATA.equals(draggedListitem.getAttribute(Constants.COLUMN_DATA_TYPE))){
			Clients.showNotification("You can only drop Measures here", "error", YAxisListBox, "end_center", 3000, true);
			return;
		} 
		if(chartData.getYColumnNames().contains(draggedListitem.getLabel()) || 
				chartData.getXColumnNames().contains(draggedListitem.getLabel())) {
			Clients.showNotification("A column can only be used once while plotting the graph", "error", YAxisListBox, "end_center", 3000, true);
			return;
		}
		
		createYListChild(draggedListitem.getLabel());
		
		// passing X,Y axis values to draw the chart
		yAxisDropped = true;
		chartData.getYColumnNames().add(draggedListitem.getLabel());
		
		if (xAxisDropped) {
			constructChart();	
		}
		
		validateDroppable();
	}
	
	/**
	 * Method to process with X/Y column data add/clearance function
	 */
	private void constructChart() {		
		//Disabling filter and chart clearance function in Api chart config/edit flow without chart
		if( !authenticationService.getUserCredential().getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID)||
				authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_CHART) || 
					authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){				
			try {
				chartRenderer.constructChartJSON(chartData, portlet, true);
				chartRenderer.drawChart(chartData,	Constants.EDIT_WINDOW_CHART_DIV, portlet);
			} catch (Exception ex) {
				Clients.showNotification(
						"Unable to fetch column data from Hpcc", "error",
						this.getSelf(), "middle_center", 3000, true);
				LOG.error("Exception while fetching column data from Hpcc",ex);
				return;
			}			
			
			if(!chartData.getIsFiltered()){
				filterListBox.setDroppable("true");
			}
		}
		//Disabling done button in API Chart Config flow with & without chart
		if(!authenticationService.getUserCredential().getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID) || 
				authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){
			doneButton.setDisabled(false);				
		}		

	}



	/**
	 * Enables/Disables Drops in Y & X axis list boxes 
	 * based on conditions from application constants
	 */
	private void validateDroppable() {
		//Measures
		// 0 - is for unlimited drops. So limiting drops only when not equals to 0
		if( ! (Constants.CHART_MAP.get(portlet.getChartType()).getMaxYColumns() == 0)) {
			if(chartData.getYColumnNames().size() > 
				Constants.CHART_MAP.get(portlet.getChartType()).getMaxYColumns() ) {
				YAxisListBox.setDroppable("false");
			} else {
				YAxisListBox.setDroppable("true");
			}
		}
		
		//Attributes
		//Only one attribute for all charts
		if(chartData.getXColumnNames().size() > 1){
			XAxisListBox.setDroppable("false");
		} else {
			XAxisListBox.setDroppable("true");
		}
	}
	
	private void createYListChild(String axisName) {
		final Listitem yAxisItem = new Listitem();
		Listcell listcell = new Listcell();
		listcell.setLabel(axisName);
		
		Button closeBtn = new Button();
		closeBtn.setSclass("glyphicon glyphicon-remove btn btn-link img-btn");
		closeBtn.setStyle("float:right");
		
		closeBtn.addEventListener(Events.ON_CLICK, yAxisItemDetachListener);
		
		listcell.appendChild(closeBtn);
		
		yAxisItem.appendChild(listcell);
		yAxisItem.setParent(YAxisListBox);
	}
	
	private EventListener<Event> yAxisItemDetachListener = new EventListener<Event>() {
		public void onEvent(final Event event) throws Exception {
			Listcell listcell = (Listcell) event.getTarget().getParent();
			Listitem yAxisItem = (Listitem) listcell.getParent();
			String axisName =listcell.getLabel();
			yAxisItem.detach();
			chartData.getYColumnNames().remove(axisName);
			
			//Disabling filter and chart clearance function in Api chart config/edit flow without chart
			if( !authenticationService.getUserCredential().getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID) || 
					authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_CHART) || 
						authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){				
				filterListBox.setDroppable("false");	
				
				// Only clear the existing chart when no columns are present otherwise recreate the chart
				if(chartData.getYColumnNames().size() < 1) {
					yAxisDropped = false;
					Clients.evalJavaScript("clearChart('" + Constants.EDIT_WINDOW_CHART_DIV +  "')");
				} else {
					constructChart();
				}
				
			}
			validateDroppable();
		}
	};
	
	/**
	 * Method to render chart when item dropped in X Axis
	 * @param dropEvent
	 */
	
	@Listen("onDrop = #XAxisListBox")
	public void onDropToXAxisTabBox(final DropEvent dropEvent) {

		final Listitem draggedListitem = (Listitem) ((DropEvent) dropEvent)
				.getDragged();

		//Validations
		if(chartData.getYColumnNames().contains(draggedListitem.getLabel()) || 
				chartData.getXColumnNames().contains(draggedListitem.getLabel())) {
			Clients.showNotification("A column can only be used once while plotting the graph", "error", XAxisListBox, "end_center", 3000, true);
			return;
		}
		if(!Constants.STRING_DATA.equals(draggedListitem.getAttribute(Constants.COLUMN_DATA_TYPE))){
			Clients.showNotification("You have dropped a Measure. It will only be treated as descrete values", "warning", XAxisListBox, "end_center", 5000, true);
		} 
		
		createXListChild(draggedListitem.getLabel());
					
		//passing X,Y axis values to draw the chart
		xAxisDropped = true;
		chartData.getXColumnNames().add(draggedListitem.getLabel());
		
		if(yAxisDropped){
			constructChart();
		}
		validateDroppable();
	}		
	
	private void createXListChild(String axisName) {
		final Listitem xAxisItem = new Listitem();
		Listcell listcell = new Listcell();
		listcell.setLabel(axisName);
		
		Button closeBtn = new Button();
		closeBtn.setSclass("glyphicon glyphicon-remove btn btn-link img-btn");
		closeBtn.setStyle("float:right");
		
		closeBtn.addEventListener(Events.ON_CLICK, xAxisItemDetachListener);
		
		listcell.appendChild(closeBtn);
		
		xAxisItem.appendChild(listcell);
		xAxisItem.setParent(XAxisListBox);		
	}
	
	private EventListener<Event> xAxisItemDetachListener = new EventListener<Event>() {
		public void onEvent(final Event event) throws Exception {
			Listcell listcell = (Listcell) event.getTarget().getParent();
			Listitem xAxisItem = (Listitem) listcell.getParent();
			String axisName =listcell.getLabel();
			
			xAxisItem.detach();
			
			xAxisDropped = false;
			chartData.getXColumnNames().remove(axisName);			
			
			//Disabling filter and chart clearance function in Api chart config/edit flow without chart
			if(!authenticationService.getUserCredential().getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID) || 
					authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_CHART) || 
						authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){				
				filterListBox.setDroppable("false");			
				Clients.evalJavaScript("clearChart('" + Constants.EDIT_WINDOW_CHART_DIV +  "')");
			}
			//Disabling done button in API Chart Config flow
			if(!authenticationService.getUserCredential().getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID) || 
					authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){
				doneButton.setDisabled(true);				
			}
			//Enabling drops if no column is dropped
			if(LOG.isDebugEnabled()){
				LOG.debug("axisName" + axisName);
				LOG.debug("Removing" + chartData.getXColumnNames().remove(axisName));
				LOG.debug("Removed item from x Axis box, XColumnNames size  - " + chartData.getXColumnNames().size());
				LOG.debug("List - " + chartData.getXColumnNames());
			}
			validateDroppable();
		}
	};
	
	/**
	 * Method to handle filters in Edit window
	 * @param dropEvent
	 */
	@Listen("onDrop = #filterListBox")
	public void onDropToFilterItem(final DropEvent dropEvent) {
		final Listitem draggedListitem = (Listitem) ((DropEvent) dropEvent).getDragged();
		
		Filter filter = new Filter();
		chartData.setFilter(filter);
		
		chartData.getFilter().setColumn(draggedListitem.getLabel());
		chartData.getFilter().setType((Integer) draggedListitem.getAttribute(Constants.COLUMN_DATA_TYPE));
		
		createFilterListItem(draggedListitem.getLabel());
		
		//Disabling drops
		filterListBox.setDroppable("false");
	}
	
	private void createFilterListItem (String columnName) {
		Listitem filterList = new Listitem();		
		Listcell labelCell = new Listcell(columnName);
		
		Button playBtn = new Button();
		playBtn.setSclass("glyphicon glyphicon-play btn btn-link img-btn");
		playBtn.setStyle("float:right");
		
		Popup popup = new Popup();
		popup.setZclass("popup");
		popup.setId("filterPopup");
		
		Include include = new Include();
		include.setDynamicProperty(Constants.PORTLET, portlet);
		include.setDynamicProperty(Constants.CHART_DATA, chartData);
		
		if(!authenticationService.getUserCredential().getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID) || 
				authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){
			include.setDynamicProperty(Constants.EDIT_WINDOW_DONE_BUTTON, doneButton);
		}
		
		if(Constants.NUMERIC_DATA.equals(chartData.getFilter().getType())){
			include.setSrc("layout/numeric_filter_popup.zul");
		} else {
			include.setSrc("layout/string_filter_popup.zul");
		}
		
		labelCell.appendChild(popup);
		popup.appendChild(include);
		playBtn.setPopup("filterPopup, position=end_center");
		
		Button closeBtn = new Button();
		closeBtn.setSclass("glyphicon glyphicon-remove btn btn-link img-btn");
		closeBtn.setStyle("float:right");
		closeBtn.addEventListener(Events.ON_CLICK, filterClearListener);
		labelCell.appendChild(closeBtn);
		
		labelCell.appendChild(playBtn);
		
		labelCell.setTooltiptext(chartData.getFilter().getColumn());
		
		filterList.appendChild(labelCell);
		filterList.setParent(filterListBox);
		//Enabling drops to filter list box
		filterListBox.setDroppable("true");
	}
	//Listener to close filter window
	EventListener<Event> filterClearListener = new EventListener<Event>() {
		public void onEvent(final Event event) throws Exception {	
			Listitem listItem =(Listitem) event.getTarget().getParent().getParent();			
			listItem.detach();
			
			chartData.setIsFiltered(false);
			chartData.setFilter(null);
			try {
				chartRenderer.constructChartJSON(chartData, portlet, true);
				chartRenderer.drawChart(chartData, Constants.EDIT_WINDOW_CHART_DIV, portlet);
			} catch(Exception ex) {
				Clients.showNotification("Unable to fetch column data from HPCC", "error", EditChartController.this.getSelf() , "middle_center", 3000, true);
				LOG.error("Exception while fetching column data from Hpcc", ex);
			}			
			//Enabling drops to filter list box
			filterListBox.setDroppable("true");
			
			//Disabling done button in API Chart Config flow
			if(!authenticationService.getUserCredential().getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID) || 
					authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){
				if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
					doneButton.setDisabled(false);
				}
			}
		}
	};
	
	/**
	 * Method to construct portlet based on Chart setting data passed from external source
	 */
	private Map<String,String> configureChartSettingData()
	{
		Execution exe = Executions.getCurrent();
		String sourceId = exe.getParameter("source_id");

		// format is not considered for now
		//String format = exe.getParameter("format");
		String config = exe.getParameter("config");
		ChartConfiguration configuration = null;
		try {
			configuration = new GsonBuilder().create().fromJson(config,ChartConfiguration.class);
		} catch(Exception ex) {
			LOG.error("Exception while forming ChartConfiguration data from request using Gson", ex);
		}
		//creating chart data
		dashboard = new Dashboard();
		dashboard.setSourceId(sourceId);
		dashboard.setColumnCount(1);
		dashboard.setName(configuration.getDashboardTitle());
		dashboard.setUpdatedDate(new Date(new java.util.Date().getTime()));
		//creating portlet
		Portlet portlet = new Portlet();
		portlet.setWidgetState(Constants.STATE_LIVE_CHART);
		portlet.setChartType(configuration.getChartType());
		//TODO: when external source creating more than one chart,
		//have to set portlet column dynamically
		portlet.setColumn(0);
		portlet.setName(configuration.getChartTitle());
		portlet.setPersisted(false);		
		portlet.setId(1);
		this.portlet = portlet;		
		//creating chartData
		chartData.setFileName(configuration.getDatasetName());			
		chartData.setHpccConnection(configuration.getHpccConnection());
		List<Field> feildList = configuration.getFields();
		Map<String,String> columnSchemaMap = new HashMap<String,String>();
		for(Field field :feildList)	{
			columnSchemaMap.put(field.getColumnName(), field.getDataType());
		}
		apiSaveButton.addEventListener(Events.ON_CLICK, saveApiChartSettings);
		apiCancelButton.addEventListener(Events.ON_CLICK, cancelApiChartSettings);
		return columnSchemaMap;
	}
	
	/**
	 * Listener to Save API chart setting data with out chart XML data
	 */
	EventListener<Event> saveApiChartSettings = new EventListener<Event>() {
		public void onEvent(Event event) throws Exception {
			portlet.setChartDataXML(chartRenderer.convertToXML(chartData));
			dashboard.getPortletList().add(portlet);
			dashboard.setDashboardId(
					dashboardService.addDashboardDetails(
							dashboard, 
							authenticationService.getUserCredential().getApplicationId(), 
							dashboard.getSourceId() , 
							authenticationService.getUserCredential().getUserId())
				);
			widgetService.addWidget(dashboard.getDashboardId(), portlet, 0);
			Messagebox.show("The Chart Settings data are Saved.Your window will be closed","",1,Messagebox.ON_OK);			
			authenticationService.logout(null);	
			editWindowLayout.detach();
		}
	};
	
	/**
	 * Listener to Cancel API chart setting data
	 */
	EventListener<Event> cancelApiChartSettings = new EventListener<Event>() {
		public void onEvent(Event event) throws Exception {
			Messagebox.show("The Chart Settings data are not Saved.You can close the window","",1,Messagebox.ON_OK);			
			authenticationService.logout(null);	
			editWindowLayout.detach();
		}
	};
	
	/**
	 * Method to create a Dashboard from DB
	 * @throws Exception
	 */
	private void configureDashboardPortlet()throws Exception {
		Execution exe = Executions.getCurrent();
		String dashboardId = exe.getParameter(Constants.DASHBOARD_ID);
		List<String> dashboardIdList =new ArrayList<String>();
		dashboardIdList.add(dashboardId);
		List<Dashboard> sideBarPageList = null;
		try{
			sideBarPageList =new ArrayList<Dashboard>(dashboardService.retrieveDashboardMenuPages(
					authenticationService.getUserCredential().getApplicationId(), 
					authenticationService.getUserCredential().getUserId(), 
					dashboardIdList,
					null));	
		} catch (Exception ex) {
			Clients.showNotification("Unable to fetch Dashboard from DB.Input valid Dashboard ID",true);
			LOG.error("Exception while fetching column data from Hpcc", ex);
			return;
		}
		if(LOG.isDebugEnabled()){
			LOG.debug("sideBarPageList in configurePortlet(): "+sideBarPageList);
		}
		if(sideBarPageList != null && sideBarPageList.size() > 0){
			dashboard = sideBarPageList.get(0);
		}else
		{
			Clients.showNotification("Invalid Dashboard ID.Please input a valid Dashboard ID",true);
			return;
		}
		try{
			configurePortlet();		
		}catch(Exception ex){
			Clients.showNotification("Unable to Configure Portlet in configureDashboardPortlet()", true);
			LOG.error("Exception in configureDashboardPortlet() ", ex);
			return;
		}
		apiConfigSaveButton.addEventListener(Events.ON_CLICK, saveApiChartConfigData);
	}

	/**
	 * Method to create a portlet for the chart data retrieved from DB
	 */
	private void configurePortlet() throws Exception{
		try	{
			dashboard.setPortletList((ArrayList<Portlet>) widgetService.retriveWidgetDetails(dashboard.getDashboardId()));
			} catch(Exception ex) {
				Clients.showNotification(
						"Unable to retrieve Widget details from DB for the Dashboard", true);
				LOG.error("Exception while fetching widget details from DB", ex);
				return;
			}
		if(LOG.isDebugEnabled()){
			LOG.debug("PortletList in configurePortlet(): "+dashboard.getPortletList());
		}
		if(dashboard.getPortletList() != null && dashboard.getPortletList().size() >0){
		String chartType = Executions.getCurrent().getParameter(Constants.CHART_TYPE);
		//As in Api flow each dashboard has single chart/widget, getting first widget
		portlet = dashboard.getPortletList().get(0);
		
			if(!portlet.getWidgetState().equals(Constants.STATE_DELETE)){
				//Constructing chart data only when live chart is drawn
				if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
					chartData = chartRenderer.parseXML(portlet.getChartDataXML());					
				}
			}
			if(chartType != null){
				portlet.setChartType(Integer.valueOf(chartType));
			}
		}else{
			Clients.showNotification(
					"The requested Dashboard doesn't have any Chart.Input valid Dashboard Id", true);
			return;
		}
	}
	
	/**
	 * Event to Save API chart configuration data to DB
	 */
	final EventListener<Event> saveApiChartConfigData = new EventListener<Event>() {

		@Override
		public void onEvent(Event event) throws Exception {
			portlet.setChartDataXML(chartRenderer.convertToXML(chartData));
			final List<Dashboard> dashBoardIdList = new ArrayList<Dashboard>();
			dashboard.setUpdatedDate(new Date(new java.util.Date().getTime()));
			dashboard.setPersisted(true);
			dashBoardIdList.add(dashboard);
			try	{
				//update Dashboard updated_date value
				dashboardService.updateDashboard(dashboard);
				
				//update widget Chart Xml data & chart Type
				widgetService.updateWidget(portlet);
				
				Messagebox.show("Your Chart details are Saved. This tab will now be closed","",1,Messagebox.ON_OK);				
			} catch(Exception ex) {
				Clients.showNotification("Unable to update Chart details into DB", true);
	        	LOG.error("Exception saveApiChartConfigData Listener in DashboardController", ex);
			}finally{
				authenticationService.logout(null);
			}
		}
	};
}
