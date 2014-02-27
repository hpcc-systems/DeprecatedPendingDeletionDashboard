package org.hpccsystems.dashboard.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.api.entity.ChartConfiguration;
import org.hpccsystems.dashboard.api.entity.Field;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.Filter;
import org.hpccsystems.dashboard.entity.chart.Measure;
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
import org.zkoss.zk.ui.event.SelectEvent;
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
import org.zkoss.zul.Popup;
import org.zkoss.zul.Vlayout;

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
	
	XYModel xyModal;
	Portlet portlet;

	@Wire
	Vlayout editWindowLayout;
	
	List<String> parameterList = new ArrayList<String>();
	final Map<String, Object> parameters = new HashMap<String, Object>();
	
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		super.doAfterCompose(comp);
		Execution execution = Executions.getCurrent();
		Map<String,String> columnSchemaMap = null;
		
		chartData = (XYChartData) execution.getAttribute(Constants.CHART_DATA);
		portlet = (Portlet) execution.getAttribute(Constants.PORTLET);
		doneButton = (Button) execution.getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
		
		//API chart config flow without chart
		if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)) {
			ChartConfiguration configuration = (ChartConfiguration) execution.getAttribute(Constants.CIRCUIT_CONFIG);
			columnSchemaMap = new HashMap<String, String>();
			for (Field field : configuration.getFields()) {
				columnSchemaMap.put(field.getColumnName(), field.getDataType());
			}
			
			filterListBox.setDroppable("false");			
		} else {
			try{
				columnSchemaMap = hpccService.getColumnSchema(chartData.getFileName(), chartData.getHpccConnection());
			}catch(Exception e) {
				Clients.showNotification("Unable to fetch columns from HPCC", "error", comp, "middle_center", 3000, true);
				LOG.error(Constants.ERROR_RETRIEVE_COLUMNS, e);
				return;
			}			
		}
		
		// When live chart is present in ChartPanel
		if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
			List<String> columnList = new ArrayList<String>();
			
			for (String colName : chartData.getXColumnNames()) {
				if(columnSchemaMap.containsKey(colName)){
					createXListChild(colName);
					xAxisDropped = true;
				} else {
					columnList.add(colName);
				}
			}
			for (String column : columnList) {
				chartData.getXColumnNames().remove(column);
			}
			
			columnList = new ArrayList<String>();
			for (Measure measure : chartData.getYColumns()) {
				if(columnSchemaMap.containsKey(measure.getColumn())){
					createYListChild(measure);
					yAxisDropped = true;
				} else {
					columnList.add(measure.getColumn());
				}
			}
			for (String column : columnList) {
				chartData.getYColumns().remove(column);
			}
			
			validateDroppable();

			if(chartData.getIsFiltered()) {
				for (Filter filter : chartData.getFilterList()) {
					createFilterListItem(filter);
				}
			}
			
			// Checking to avoid error while on the fly widget type change happens 
			if( (chartData.getXColumnNames().size() > 0) && (chartData.getYColumns().size() > 0)){
				constructChart();
			}
		} 		


		Listitem listItem;
		Listcell listcell;
		if(columnSchemaMap != null){
		for (Map.Entry<String, String> entry : columnSchemaMap.entrySet()) {
			listItem = new Listitem();
			listcell = new Listcell(entry.getKey());
			listItem.appendChild(listcell);
			listItem.setDraggable("true");
			if(DashboardUtil.checkNumeric(entry.getValue())){
				listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.NUMERIC_DATA);
				final Measure measure = new Measure(entry.getKey(), "sum");
				listItem.setAttribute(Constants.MEASURE, measure);
				
				final Popup popup = new Popup();
				popup.setWidth("100px");
				popup.setZclass("popup");
				final Button button = new Button("Sum");
				button.setZclass("btn btn-xs");
				button.setStyle("font-size: 10px; float: right;");
				button.setPopup(popup);
				
				Listbox listbox = new Listbox();
				listbox.setMultiple(false);
				listbox.appendItem("Average", "avg");
				listbox.appendItem("Count", "count");
				listbox.appendItem("Minimum", "min");
				listbox.appendItem("Maximum", "max");
				listbox.appendItem("Sum", "sum");
				
				listbox.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<Component, Object>>() {

					@Override
					public void onEvent(SelectEvent<Component, Object> event) throws Exception {
						Listitem selectedItem = (Listitem) event.getSelectedItems().iterator().next();
						measure.setAggregateFunction(selectedItem.getValue().toString());
						button.setLabel(selectedItem.getLabel());
						popup.close();
					}
				});
				
				popup.appendChild(listbox);
				listcell.appendChild(popup);
				listcell.appendChild(button);
				
				listItem.setParent(measureListBox);
			} else {
				final String columnName = entry.getKey();
				final Popup popup1 = new Popup();
				popup1.setWidth("200px");
				popup1.setZclass("popup");
				
				final Button btn = new Button();
				btn.setSclass("glyphicon glyphicon-cog btn btn-link img-btn");
				btn.setStyle("float:right");
				btn.setVisible(false);
				btn.setPopup(popup1);
				
				Listbox listbox = new Listbox();
				listbox.setMultiple(false);
				listbox.appendItem("Create Measure", "count");
				listbox.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<Component, Object>>() {

					@Override
					public void onEvent(SelectEvent<Component, Object> event) throws Exception {
						Listitem selectedItem = (Listitem) event.getSelectedItems().iterator().next();
						
						if(!selectedItem.getValue().equals("count"))
							return;
						
						// Create Measure
						Listitem listItem = new Listitem();
						Listcell listcell = new Listcell(columnName);
						listItem.appendChild(listcell);
						listItem.setDraggable("true");
						listItem.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.NUMERIC_DATA);
						final Measure measure = new Measure(columnName, "count");
						listItem.setAttribute(Constants.MEASURE, measure);
						
						final Popup popup = new Popup();
						popup.setWidth("100px");
						popup.setZclass("popup");
						final Button button = new Button("Count");
						button.setZclass("btn btn-xs");
						button.setStyle("font-size: 10px; float: right;");
						button.setPopup(popup);
						
						Listbox listbox = new Listbox();
						listbox.setMultiple(false);
						listbox.appendItem("Count", "count");
						
						listbox.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<Component, Object>>() {

							@Override
							public void onEvent(SelectEvent<Component, Object> event) throws Exception {
								Listitem selectedItem = (Listitem) event.getSelectedItems().iterator().next();
								measure.setAggregateFunction(selectedItem.getValue().toString());
								button.setLabel(selectedItem.getLabel());
								popup.close();
							}
						});
						
						popup.appendChild(listbox);
						listcell.appendChild(popup);
						listcell.appendChild(button);
						
						listItem.setParent(measureListBox);
						
						btn.setVisible(false);
						btn.setDisabled(true);
						popup1.close();
					}
				});
				
				popup1.appendChild(listbox);
				listcell.appendChild(btn);
				listcell.appendChild(popup1);
				
				listcell.addEventListener(Events.ON_MOUSE_OVER, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						if(!btn.isDisabled()){
							btn.setVisible(true);
						}
					}
					
				});
				
				listcell.addEventListener(Events.ON_MOUSE_OUT, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						if(!popup1.isVisible()){
							btn.setVisible(false);
						}
					}
					
				});
				
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
		
		//Creating new instance purposefully
		Measure measure = (Measure) draggedListitem.getAttribute(Constants.MEASURE);
		Measure newMeasure = new Measure(measure.getColumn(), measure.getAggregateFunction());

		//Validations
		if(chartData.getYColumns().contains(newMeasure) || 
				chartData.getXColumnNames().contains(newMeasure.getColumn())) {
			Clients.showNotification("Dropped column is already used in plotting this Chart. Please drop another Column", "error", YAxisListBox, "end_center", 3000, true);
			return;
		}
		if(chartData.getXColumnNames().size() > 1 && chartData.getYColumns().size() >0) {
			Clients.showNotification("The chart is already groped with multiple attributes.\nGrouping is aloowed in only one of Measures and Attributes.", "error", YAxisListBox, "end_center", 3000, true);
			return;
		}
		
		createYListChild(newMeasure);
		
		// passing X,Y axis values to draw the chart
		yAxisDropped = true;
		chartData.getYColumns().add(newMeasure);
		
		if (xAxisDropped) {
			constructChart();	
		}
		
		validateDroppable();
	}
	
	/**
	 * Method to process with X/Y column data add/clearance function
	 */
	private void constructChart() {	
		try{
			//Drawing chart except in API chart configuration flow
			if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)){
				try {
					Map<String, String> columnSchemaMap = new HashMap<String, String>();
					columnSchemaMap = hpccService.getColumnSchema(chartData.getFileName(), chartData.getHpccConnection());
					for (String column : chartData.getXColumnNames()) {
						if(!columnSchemaMap.containsKey(column)){
							throw new Exception("X Column " + column + " not present in Dataset");
						}
					}
					for (Measure measure : chartData.getYColumns()) {
						if(!columnSchemaMap.containsKey(measure.getColumn())){
							throw new Exception("Y Column " + measure.getColumn() + " not present in Dataset");
						}
					}
					
					chartRenderer.constructChartJSON(chartData, portlet, true);
					chartRenderer.drawChart(chartData,	Constants.EDIT_WINDOW_CHART_DIV, portlet);
				} catch(Exception e) {
					Clients.showNotification("Couldn't retrive data to draw chart", "error", this.getSelf(), "middle_center", 3000, true);
					LOG.error("Chart Rendering failed", e);
				}
			} else {
				chartRenderer.constructChartJSON(chartData, portlet, true);
				chartRenderer.drawChart(chartData,	Constants.EDIT_WINDOW_CHART_DIV, portlet);
			}
			
			doneButton.setDisabled(false);				
		}catch (Exception ex) {
			Clients.showNotification(
					"Unable to fetch column data from Hpcc", "error",
					this.getSelf(), "middle_center", 3000, true);
			LOG.error("Exception while fetching column data from Hpcc",ex);
			return;
		}	

	}


	/**
	 * Enables/Disables Drops in Y & X axis list boxes 
	 * based on conditions from application constants
	 */
	private void validateDroppable() {
		if(LOG.isDebugEnabled()){
			LOG.debug("Portlet object -- " + portlet);
		}
		// 0 - is for unlimited drops. So limiting drops only when not equals to 0

		//Measures
		if( ! (Constants.CHART_MAP.get(portlet.getChartType()).getMaxYColumns() == 0)) {
			if(chartData.getYColumns().size() < 
				Constants.CHART_MAP.get(portlet.getChartType()).getMaxYColumns() ) {
				YAxisListBox.setDroppable("true");
			} else {
				YAxisListBox.setDroppable("false");
			}
		}
		
		//Attributes
		if( ! (Constants.CHART_MAP.get(portlet.getChartType()).getMaxXColumns() == 0)) {
			if(chartData.getXColumnNames().size() < 
			Constants.CHART_MAP.get(portlet.getChartType()).getMaxXColumns() ) {
				XAxisListBox.setDroppable("true");
			} else {
				XAxisListBox.setDroppable("false");
			}
			
			//Second X Column indicates Grouping
			if(chartData.getXColumnNames().size() > 1) {
				chartData.setIsGrouped(true);
			}
		}
	}
	
	
	private void createYListChild(Measure measure) {
		Listitem yAxisItem = new Listitem();
		yAxisItem.setAttribute(Constants.MEASURE, measure);
		Listcell listcell = new Listcell();
		listcell.setLabel(measure.getColumn() + "_" + measure.getAggregateFunction());
		
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
			Listitem yAxisItem = (Listitem) event.getTarget().getParent().getParent();
			Measure measure = (Measure) yAxisItem.getAttribute(Constants.MEASURE);
			yAxisItem.detach();
			chartData.getYColumns().remove(measure);
			
			// Only clear the existing chart when no columns are present otherwise recreate the chart
			if(chartData.getYColumns().size() < 1) {
				yAxisDropped = false;
				Clients.evalJavaScript("clearChart('" + Constants.EDIT_WINDOW_CHART_DIV +  "')");
			} else {
				constructChart();
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
		if(chartData.getYColumns().contains(draggedListitem.getLabel()) || 
				chartData.getXColumnNames().contains(draggedListitem.getLabel())) {
			Clients.showNotification("A column can only be used once while plotting the graph", "error", XAxisListBox, "end_center", 3000, true);
			return;
		}
		if(!Constants.STRING_DATA.equals(draggedListitem.getAttribute(Constants.COLUMN_DATA_TYPE))){
			Clients.showNotification( "\""+ draggedListitem.getLabel() +"\" is a Measure. It will only be treated as descrete values", "warning", XAxisListBox, "end_center", 5000, true);
		}
		if(chartData.getYColumns().size() > 1 && chartData.getXColumnNames().size() >0){
			Clients.showNotification("The chart is already groped with multiple measures.\nGrouping is aloowed in only one of Measures and Attributes.", "error", XAxisListBox, "end_center", 3000, true);
			return;
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
			
			chartData.getXColumnNames().remove(axisName);			
	
			//Disabling done button
			doneButton.setDisabled(true);				
			
			//Enabling drops if no column is dropped
			if(LOG.isDebugEnabled()){
				LOG.debug("axisName" + axisName);
				LOG.debug("Removing" + chartData.getXColumnNames().remove(axisName));
				LOG.debug("Removed item from x Axis box, XColumnNames size  - " + chartData.getXColumnNames().size());
				LOG.debug("List - " + chartData.getXColumnNames());
			}
			
			// Only clear the existing chart when no columns are present otherwise recreate the chart
			if(chartData.getXColumnNames().size() < 1) {
				xAxisDropped = false;
				Clients.evalJavaScript("clearChart('" + Constants.EDIT_WINDOW_CHART_DIV +  "')");
			} else {
				constructChart();
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
		
		if(chartData.getFilterList().contains(draggedListitem.getLabel())) {
			Clients.showNotification("This column is already added to filters.", "error", filterListBox, "end_center", 3000, true);
			return;
		}
		
		Filter filter = new Filter();
		filter.setColumn(draggedListitem.getLabel());
		filter.setType((Integer) draggedListitem.getAttribute(Constants.COLUMN_DATA_TYPE));
		
		createFilterListItem(filter);
	}
	
	private void createFilterListItem (Filter filter) {
		Listitem filterList = new Listitem();
		filterList.setAttribute(Constants.FILTER, filter);
		
		Listcell labelCell = new Listcell(filter.getColumn());
		
		Button playBtn = new Button();
		playBtn.setSclass("glyphicon glyphicon-play btn btn-link img-btn");
		playBtn.setStyle("float:right");
		
		Popup popup = new Popup();
		popup.setZclass("popup");
		popup.setId( filter.getColumn()+ "_filterPopup");
		
		Include include = new Include();
		include.setDynamicProperty(Constants.FILTER, filter);
		include.setDynamicProperty(Constants.PORTLET, portlet);
		include.setDynamicProperty(Constants.CHART_DATA, chartData);
		
		include.setDynamicProperty(Constants.EDIT_WINDOW_DONE_BUTTON, doneButton);
		
		if(Constants.NUMERIC_DATA.equals(filter.getType())){
			include.setSrc("layout/numeric_filter_popup.zul");
		} else {
			include.setSrc("layout/string_filter_popup.zul");
		}
		
		labelCell.appendChild(popup);
		popup.appendChild(include);
		playBtn.setPopup(filter.getColumn()+ "_filterPopup, position=end_center");
		
		Button closeBtn = new Button();
		closeBtn.setSclass("glyphicon glyphicon-remove btn btn-link img-btn");
		closeBtn.setStyle("float:right");
		closeBtn.addEventListener(Events.ON_CLICK, filterClearListener);
		labelCell.appendChild(closeBtn);
		
		labelCell.appendChild(playBtn);
		
		labelCell.setTooltiptext(filter.getColumn());
		
		filterList.appendChild(labelCell);
		
		filterListBox.appendChild(filterList);
	}
	
	//Listener to close filter window
	EventListener<Event> filterClearListener = new EventListener<Event>() {
		public void onEvent(final Event event) throws Exception {	
			Listitem listItem =(Listitem) event.getTarget().getParent().getParent();			
			
			chartData.getFilterList().remove(listItem.getAttribute(Constants.FILTER));
			if(chartData.getFilterList().size() < 1){
				chartData.setIsFiltered(false);
			}
			
			try {
				chartRenderer.constructChartJSON(chartData, portlet, true);
				chartRenderer.drawChart(chartData, Constants.EDIT_WINDOW_CHART_DIV, portlet);
			} catch(Exception ex) {
				Clients.showNotification("Unable to fetch column data from HPCC", "error", EditChartController.this.getSelf() , "middle_center", 3000, true);
				LOG.error("Exception while fetching column data from Hpcc", ex);
			}
						
			if(xAxisDropped && yAxisDropped){
				doneButton.setDisabled(false);
			}
			
			listItem.detach();
		}
	};
}
