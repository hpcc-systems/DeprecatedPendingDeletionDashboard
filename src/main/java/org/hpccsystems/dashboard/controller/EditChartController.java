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
import org.hpccsystems.dashboard.entity.ApiConfiguration;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.entity.chart.Filter;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.XYModel;
import org.hpccsystems.dashboard.entity.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.helper.DashboardHelper;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
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
	private DashboardService dashboardService;
	
	@WireVariable
	ChartRenderer chartRenderer;
	
	@WireVariable
	HPCCService hpccService;
	
	@WireVariable
	private WidgetService widgetService;	
	
	@WireVariable
	private DashboardHelper dashboardHelper;

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
	private ApiConfiguration apiConfig;
	private Dashboard dashboard;
	private Application application;
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		super.doAfterCompose(comp);
		apiConfig =(ApiConfiguration) Sessions.getCurrent().getAttribute("apiConfiguration");
		Map<String,String> columnSchemaMap = null;
		//API chart config flow without chart
		if(apiConfig != null && apiConfig.isApiConfig())
		{
			columnSchemaMap = configureChartSettingData();
			filterListBox.setStyle("backgroundr:gray;");
			filterListBox.invalidate();			
		}
		//Dashboard chart edit flow and API Dashboard View flow,API chart config flow with chart
		if(apiConfig == null || (apiConfig != null && !apiConfig.isApiConfig()))
		{			
			//API chart config flow with chart
			if(apiConfig != null && apiConfig.isApiChartConfig())
			{
				configureDashboardPortlet();
			} else {
				//Other flows:Dashboard chart edit flow & API Dashboard View flow
				portlet = (Portlet) Executions.getCurrent().getAttribute(
						Constants.PORTLET);

				chartData = (XYChartData) Executions.getCurrent().getAttribute(
						Constants.CHART_DATA);

				doneButton = (Button) Executions.getCurrent().getAttribute(
						Constants.EDIT_WINDOW_DONE_BUTTON);
			}
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
			validateYAxisDrops();
			try	{
				chartRenderer.constructChartJSON(chartData, portlet, true);
				chartRenderer.drawChart(chartData,Constants.EDIT_WINDOW_CHART_DIV, portlet);
			} catch(Exception ex) {
				Clients.showNotification("Unable to fetch column data from Hpcc", "error", comp, "middle_center", 3000, true);
				LOG.error("Exception while fetching column data from Hpcc", ex);
			}
			
			if(chartData.getIsFiltered()) {
				createFilterListItem(chartData.getFilter().getColumn());
				filterListBox.setDroppable("false");
			}
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
			if(checkNumeric(entry.getKey(), entry.getValue())){
				listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.NUMERIC_DATA);
				listItem.setParent(measureListBox);
			} else {
				listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.STRING_DATA);
				listItem.setParent(attributeListBox);
			}
		}} 
	}	
	
	/**
	 * Checks whether a column is numeric
	 * @param column
	 * @param dataType
	 * @return
	 */
	private boolean checkNumeric(final String column, final String dataType)
	{
		boolean numericColumn = false;
			if(dataType.contains("integer")	|| 
					dataType.contains("real") || 
					dataType.contains("decimal") ||  
					dataType.contains("unsigned"))	{
				numericColumn = true;
			}
		return numericColumn;
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
		} else if(chartData.getYColumnNames().contains(draggedListitem.getLabel()) || 
				chartData.getXColumnNames().contains(draggedListitem.getLabel())) {
			Clients.showNotification("A column can only be used once while plotting the graph", "error", YAxisListBox, "end_center", 3000, true);
			return;
		}
		
		createYListChild(draggedListitem.getLabel());
		
		// passing X,Y axis values to draw the chart
		yAxisDropped = true;
		chartData.getYColumnNames().add(draggedListitem.getLabel());
		
		if (xAxisDropped) {
			constructChartData();	
		}
		
		validateYAxisDrops();
	}
	
	/**
	 * Method to process with X/Y column data add/clearance function
	 */
	private void constructChartData() {		
		//Disabling filter and chart clearance function in Api chart config/edit flow without chart
		if(apiConfig == null || (apiConfig != null && !apiConfig.isApiConfig())){				
			try {
				chartRenderer.constructChartJSON(chartData, portlet, true);
				chartRenderer.drawChart(chartData,	Constants.EDIT_WINDOW_CHART_DIV, portlet);
			} catch (Exception ex) {
				Clients.showNotification(
						"Unable to fetch column data from Hpcc", "error",
						this.getSelf(), "middle_center", 3000, true);
				LOG.error("Exception while fetching column data from Hpcc",ex);
			}			
			filterListBox.setDroppable("true");
		}
		//Disabling done button in API Chart Config flow with & without chart
		if(apiConfig == null || (apiConfig != null && (!apiConfig.isApiChartConfig()
				&& !apiConfig.isApiConfig()))){
			doneButton.setDisabled(false);				
		}		

	}



	/**
	 * Disables Drops in Y axis list box based on conditions
	 *  2 Columns for Line & Bar chart
	 *  1 Column for other type of charts
	 */
	private void validateYAxisDrops() {
		//disabling drops based on chart type
		// 2 Columns for Bar/Line chart - 1 for others
		if(Constants.BAR_CHART.equals(portlet.getChartType()) || Constants.LINE_CHART.equals(portlet.getChartType())) {
			if(chartData.getYColumnNames().size() > 1) {
				YAxisListBox.setDroppable("false");
			}
		} else {
			if(chartData.getYColumnNames().size() > 0) {
				YAxisListBox.setDroppable("false");
			}
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
			yAxisDropped = false;
			chartData.getYColumnNames().remove(axisName);
			
			//Disabling filter and chart clearance function in Api chart config/edit flow without chart
			if(apiConfig == null || (apiConfig != null && !apiConfig.isApiConfig())){				
			filterListBox.setDroppable("false");			
			Clients.evalJavaScript("clearChart('" + Constants.EDIT_WINDOW_CHART_DIV +  "')");
			}
			//Disabling done button in API Chart Config flow
			if(apiConfig == null || (apiConfig != null && (!apiConfig.isApiChartConfig()
					&& !apiConfig.isApiConfig()))){
				doneButton.setDisabled(true);				
			}
			//Enabling drops based on chart type
			// 2 Columns for Bar/Line chart - 1 for others
			if(Constants.BAR_CHART.equals(portlet.getChartType()) || Constants.LINE_CHART.equals(portlet.getChartType())) {
				if(chartData.getYColumnNames().size() < 2) {
					YAxisListBox.setDroppable("true");
				}
			} else {
				if(chartData.getYColumnNames().size() < 1) {
					YAxisListBox.setDroppable("true");
				}
			}
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
		if(!Constants.STRING_DATA.equals(draggedListitem.getAttribute(Constants.COLUMN_DATA_TYPE))){
			Clients.showNotification("You have dropped a Measure. It will only be treated as descrete values", "warning", XAxisListBox, "end_center", 5000, true);
		} else if(chartData.getYColumnNames().contains(draggedListitem.getLabel()) || 
				chartData.getXColumnNames().contains(draggedListitem.getLabel())) {
			Clients.showNotification("A column can only be used once while plotting the graph", "error", XAxisListBox, "end_center", 3000, true);
			return;
		}
		
		createXListChild(draggedListitem.getLabel());
					
		//passing X,Y axis values to draw the chart
		xAxisDropped = true;
		chartData.getXColumnNames().add(draggedListitem.getLabel());
		
		if(yAxisDropped){
			constructChartData();
		}
		
		//disabling drops if Atleast one column is dropped
		if(chartData.getXColumnNames().size() > 0) {
			XAxisListBox.setDroppable("false");
		}
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
			if(apiConfig == null || (apiConfig != null && !apiConfig.isApiConfig())){				
			filterListBox.setDroppable("false");			
			Clients.evalJavaScript("clearChart('" + Constants.EDIT_WINDOW_CHART_DIV +  "')");
			}
			//Disabling done button in API Chart Config flow
			if(apiConfig == null || (apiConfig != null && (!apiConfig.isApiChartConfig()
					&& !apiConfig.isApiConfig()))){
				doneButton.setDisabled(true);				
			}
			//Enabling drops if no column is dropped
			if(LOG.isDebugEnabled()){
				LOG.debug("axisName" + axisName);
				LOG.debug("Removing" + chartData.getXColumnNames().remove(axisName));
				LOG.debug("Removed item from x Axis box, XColumnNames size  - " + chartData.getXColumnNames().size());
				LOG.debug("List - " + chartData.getXColumnNames());
			}
			if(chartData.getXColumnNames().size() < 1) {
				XAxisListBox.setDroppable("true");
			}
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
		if(apiConfig == null || (apiConfig != null && (!apiConfig.isApiChartConfig()
				&& !apiConfig.isApiConfig()))){
		include.setDynamicProperty(Constants.EDIT_WINDOW_DONE_BUTTON, doneButton);
		}
		if(apiConfig != null && apiConfig.isApiChartConfig()){
			if(Constants.NUMERIC_DATA.equals(chartData.getFilter().getType())){
				include.setSrc("numeric_filter_popup.zul");
			} else {
				include.setSrc("string_filter_popup.zul");
			}
		}else
		{
			if(Constants.NUMERIC_DATA.equals(chartData.getFilter().getType())){
				include.setSrc("layout/numeric_filter_popup.zul");
			} else {
				include.setSrc("layout/string_filter_popup.zul");
			}
			
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
			if(apiConfig == null || (apiConfig != null && !apiConfig.isApiChartConfig())){
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
		String source = exe.getParameter("source");
		String sourceId = exe.getParameter("source_id");
		String format = exe.getParameter("format");
		String config = exe.getParameter("config");
		ChartConfiguration configuration = null;
		try{
			configuration = new GsonBuilder().create().fromJson(config,ChartConfiguration.class);
		}catch(Exception ex)
		{
			LOG.error("Exception while forming ChartConfiguration data from request using Gson", ex);
		}
		//creating chart data
		dashboard = new Dashboard();
		dashboard.setApplicationId(sourceId);
		dashboard.setColumnCount(1);
		dashboard.setName(configuration.getDashboardTitle());
		dashboard.setUpdatedDate(new Date(new java.util.Date().getTime()));
		application = new Application(sourceId,source,Constants.SOURCE_TYPE_ID.get(source));
		//creating portlet
		Portlet portlet = new Portlet();
		portlet.setWidgetState(Constants.STATE_LIVE_CHART);
		portlet.setChartType(configuration.getChartType());
		//TODO: when external source creating more than one chart,
		//have to set portlet column dynamically
		portlet.setColumn(0);
		portlet.setWidgetSequence(1);
		portlet.setName(configuration.getChartTitle());
		portlet.setPersisted(false);		
		portlet.setId(portlet.getWidgetSequence());
		this.portlet = portlet;		
		//creating chartData
		chartData.setSourceType(Constants.SOURCE_TYPE_ID.get(source));
		chartData.setFileName(configuration.getDatasetName());			
		chartData.setHpccConnection(configuration.getHpccConnection());
		List<Field> feildList = configuration.getFields();
		Map<String,String> columnSchemaMap = new HashMap<String,String>();
		for(Field field :feildList)
		{
			columnSchemaMap.put(field.getColumnName(), field.getDataType());
		}
		apiSaveButton.addEventListener(Events.ON_CLICK, saveApiChartSettings);
		apiCancelButton.addEventListener(Events.ON_CLICK, cancelApiChartSettings);
		return columnSchemaMap;
	}
	
	/**
	 * Listener to Save API chart setting data
	 */
	EventListener<Event> saveApiChartSettings = new EventListener<Event>() {
		public void onEvent(Event event) throws Exception {
			User user = (User)Sessions.getCurrent().getAttribute("user");
			portlet.setChartDataXML(chartRenderer.convertToXML(chartData));
			dashboard.getPortletList().add(portlet);
			dashboard.setDashboardId(
					dashboardService.addDashboardDetails(dashboard,application ,user.getUserId()));
			List<Portlet> newPortlets = new ArrayList<Portlet>();
			for(Portlet widget : dashboard.getPortletList())
			{
				newPortlets.add(widget);
			}
			widgetService.addWidgetDetails(dashboard.getDashboardId(), newPortlets);
			Messagebox.show("The Chart Settings data are Saved.You can close the window","",1,Messagebox.ON_OK);			
			Sessions.getCurrent().removeAttribute("apiConfiguration");
			Sessions.getCurrent().removeAttribute("user");
			Sessions.getCurrent().removeAttribute("userCredential");	
			editWindowLayout.detach();
		}
	};
	
	/**
	 * Listener to Cancel API chart setting data
	 */
	EventListener<Event> cancelApiChartSettings = new EventListener<Event>() {
		public void onEvent(Event event) throws Exception {
			Messagebox.show("The Chart Settings data are not Saved.You can close the window","",1,Messagebox.ON_OK);			
			Sessions.getCurrent().removeAttribute("apiConfiguration");
			Sessions.getCurrent().removeAttribute("user");
			Sessions.getCurrent().removeAttribute("userCredential");	
			editWindowLayout.detach();
		}
	};
	
	/**
	 * Method to create a Dashboard from DB
	 * @throws Exception
	 */
	private void configureDashboardPortlet()throws Exception {
		Execution exe = Executions.getCurrent();
		String source = exe.getParameter("source");
		String sourceId = exe.getParameter("source_id");		
		String dashboardId = exe.getParameter(Constants.DASHBOARD_ID);
		final Application application = new Application(sourceId,source,Constants.SOURCE_TYPE_ID.get(source));		
		List<String> dashboardIdList =new ArrayList<String>();
		dashboardIdList.add(dashboardId);
		User user = (User)Sessions.getCurrent().getAttribute("user");
		List<Dashboard> sideBarPageList = null;
		try{
			sideBarPageList =new ArrayList<Dashboard>(dashboardService.retrieveDashboardMenuPages(application,user.getUserId(),dashboardIdList));	
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
		//portlet.setChartType(Integer.valueOf(chartType));
			if(!portlet.getWidgetState().equals(Constants.STATE_DELETE)){
				//Constructing chart data only when live chart is drawn
				if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
					chartData = chartRenderer.parseXML(portlet.getChartDataXML());					
				}
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
			try
			{
				dashboardHelper.updateDashboardWidgetDetails(dashBoardIdList);
				Messagebox.show("Your Chart details are Saved.You can close the window","",1,Messagebox.ON_OK);
				Sessions.getCurrent().removeAttribute("apiConfiguration");
				Sessions.getCurrent().removeAttribute("user");
				Sessions.getCurrent().removeAttribute("userCredential");
			}catch(Exception ex)
			{
				Clients.showNotification("Unable to update Chart details into DB", true);
	        	LOG.error("Exception saveApiChartConfigData Listener in DashboardController", ex);
			}
		
		}
	};
}
