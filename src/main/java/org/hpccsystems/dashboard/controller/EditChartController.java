package org.hpccsystems.dashboard.controller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.Filter;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.XYModel;
import org.hpccsystems.dashboard.entity.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

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
	
	/**
	 * The map which will hold web services output as column details
	 */
	Map<String, String> wsdlOuputputMap = new HashMap<String, String>();
	
	@Wire
	Window editPortletWindow;
	
	@Wire
	Hbox editPortletGrid;
	
	@Wire
	Hlayout editLayout;
	
	@Wire
	Listbox columnListBox;
	
	@Wire
	Listbox yAxisBox;
	
	@Wire
	Listbox xAxisBox;
	
	@Wire
	Button doneButton;
	
	@Wire
	Listitem yDefaultItem;
	
	@Wire
	Listitem xDefaultItem;
	
	@Wire
	Textbox wsdlTxt;
	
	@Wire
	Textbox editUserTxt;
	
	@Wire
	Textbox editPwdTxt;
	
	@Wire
	Textbox editJDBCUrlTxt;		
	
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
	
	Boolean xAxisDropped = false;
	Boolean yAxisDropped = false;
	
	XYChartData chartData = new XYChartData();
	
	boolean isBarLinePieChart;	

	XYModel xyModal;
	Portlet portlet;
	
	Map<Integer,String> portletIdWsdlMap;
	List<String> filterDataList;
	
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		super.doAfterCompose(comp);
		portlet = (Portlet) Sessions.getCurrent().getAttribute(Constants.ACTIVE_PORTLET);
		
		//gets map which has portlet's Wsdl details
		Object mapObj =Sessions.getCurrent().getAttribute("portletIdWsdlMap");		
		if(mapObj != null)	{
			portletIdWsdlMap = (Map<Integer,String>)(mapObj);
		} else {
			portletIdWsdlMap = new HashMap<Integer,String>();
		}	
		
		if(portlet == null)
		{
			LOG.error("Current portlet is not in session");
		}
	}

	/**
	 * Method to be invoked while submitting the edit form 
	 */
	@Listen("onClick=#editSubmit")
	public void editPortlet()
	{
		if(wsdlTxt.getValue()!= null && wsdlTxt.getValue().length()>0)
		{		
			
		//persisting enterred Wsdl details in the session for the current portlet
		portletIdWsdlMap.put(portlet.getId(), wsdlTxt.getValue());	
		Sessions.getCurrent().setAttribute("portletIdWsdlMap", portletIdWsdlMap);
		
		wsdlOuputputMap = hpccService.getColumnSchema(wsdlTxt.getValue(), editUserTxt.getValue(), editPwdTxt.getValue(), editJDBCUrlTxt.getValue());
		//Adding the web service returned columns to the column's side layout
		if(wsdlOuputputMap != null && wsdlOuputputMap.size() > 1 && !wsdlOuputputMap.containsKey(Constants.ERROR))
		{
			//Removing the submit grid from the window 
			editPortletGrid.detach();	
			chartData.setFileName(wsdlTxt.getValue());
			chartData.setURL(editJDBCUrlTxt.getValue());
			chartData.setUserName(editUserTxt.getValue());
			chartData.setPassword(editPwdTxt.getValue());
			
			//Making the chart layout visible in the same window
			editLayout.setVisible(true);
			
			LOG.debug("wsdlOuputputMap -->"+wsdlOuputputMap);
			for(Entry<String, String> entry : wsdlOuputputMap.entrySet())
			{
				if(entry.getValue().contains("integer") 
						||entry.getValue().contains("real")
						||entry.getValue().contains("decimal"))
				{
					
					final Listitem item= new Listitem(entry.getKey().trim());
					item.setDraggable("true");
					item.setDroppable("true");
					item.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.NUMERIC_DATA);
					measureListBox.appendChild(item);
				}
				else
				{
					final Listitem item= new Listitem(entry.getKey().trim());
					item.setDraggable("true");
					item.setDroppable("true");
					item.setAttribute(Constants.COLUMN_DATA_TYPE, Constants.STRING_DATA);
					attributeListBox.appendChild(item);
				}
			}
							
		}
		else
		{
			Clients.showNotification(wsdlOuputputMap.get(Constants.ERROR), "info", null, "top_center", 3000);
		}
		}
		
	}
	
	


	/**
	 * Method to check whether the numeric value is dropped in X/Y axis
	 * @param draggedListitem
	 * @return boolean
	 */
	private boolean checkNumeric(final Listitem draggedListitem)
	{
		boolean numericColumn = false;
			if(wsdlOuputputMap.get(draggedListitem.getLabel().trim()).contains("integer")
					|| wsdlOuputputMap.get(draggedListitem.getLabel().trim()).contains("real")
					|| wsdlOuputputMap.get(draggedListitem.getLabel().trim()).contains("decimal")
					||  wsdlOuputputMap.get(draggedListitem.getLabel().trim()).contains("unsigned"))
			{
				numericColumn = true;
			}
		return numericColumn;
	}
	
	/**
	 * Draws the chart from edit window to actual layout window 
	 * and Adds the chart to session
	 * @param event
	 */
	@Listen("onClick=#doneButton")
	public void closeEditWindow(final MouseEvent event){
		final Session session = Sessions.getCurrent();

		//Getting the current div to draw the map
		final String divToDraw = (String) session.getAttribute("currentDiv");
		
		final Image img=(Image)session.getAttribute("curStaticImg");
		if(img != null)		{
			img.detach();
		}
		
		chartRenderer.constructChartJSON(chartData, portlet, true);
		chartRenderer.drawChart(chartData , divToDraw , portlet);
		portlet.setChartDataXML(chartRenderer.convertToXML(chartData));

		//Removing Edit window
		editPortletWindow.detach();
	}

	/**
	 * Method to render chart when item dropped in Y Axis
	 * @param dropEvent
	 */
	@Listen("onDrop = #YAxisListBox")
	public void onDropToYAxisTabBox(final DropEvent dropEvent) {

		final Listitem draggedListitem = (Listitem) ((DropEvent) dropEvent).getDragged();
		final Listitem yAxisItem = new Listitem();
		Listcell listcell = new Listcell();
		listcell.setLabel(draggedListitem.getLabel());
		
		Button closeBtn = new Button();
		closeBtn.setSclass("glyphicon glyphicon-remove btn btn-link img-btn");
		closeBtn.setStyle("float:right");
		
		closeBtn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			public void onEvent(final Event event) throws Exception {						
				yAxisItem.detach();
				yAxisDropped = false;
				chartData.setYColumnName(null);
				doneButton.setDisabled(true);
				filterListBox.setDroppable("false");
			}
		});
		
		listcell.appendChild(closeBtn);
		
		yAxisItem.appendChild(listcell);
		yAxisItem.setParent(YAxisListBox);		
		
		// passing X,Y axis values to draw the chart
		yAxisDropped = true;
		chartData.setYColumnName(yAxisItem.getLabel());
		
		
		if(xAxisDropped){
			chartRenderer.constructChartJSON(chartData, portlet, true);
			chartRenderer.drawChart(chartData,Constants.EDIT_WINDOW_CHART_DIV , portlet);
			doneButton.setDisabled(false);
			filterListBox.setDroppable("true");
		}
	}
	
	/**
	 * Method to render chart when item dropped in X Axis
	 * @param dropEvent
	 */
	
	@Listen("onDrop = #XAxisListBox")
	public void onDropToXAxisTabBox(final DropEvent dropEvent) {

		final Listitem draggedListitem = (Listitem) ((DropEvent) dropEvent)
				.getDragged();
		final Listitem xAxisItem = new Listitem();
		Listcell listcell = new Listcell();
		listcell.setLabel(draggedListitem.getLabel());
		
		Button closeBtn = new Button();
		closeBtn.setSclass("glyphicon glyphicon-remove btn btn-link img-btn");
		closeBtn.setStyle("float:right");
		
		closeBtn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			public void onEvent(final Event event) throws Exception {						
				xAxisItem.detach();
				xAxisDropped = false;
				chartData.setXColumnName(null);
				doneButton.setDisabled(true);
				filterListBox.setDroppable("false");
			}
		});
		
		listcell.appendChild(closeBtn);
		
		xAxisItem.appendChild(listcell);
		xAxisItem.setParent(XAxisListBox);		
			
		//passing X,Y axis values to draw the chart
		xAxisDropped = true;
		chartData.setXColumnName(xAxisItem.getLabel());
		
		if(yAxisDropped){
			chartRenderer.constructChartJSON(chartData, portlet, true);
			chartRenderer.drawChart(chartData, Constants.EDIT_WINDOW_CHART_DIV, portlet);
			doneButton.setDisabled(false);
			filterListBox.setDroppable("true");
		}
	}		
	
	/**
	 * Method to handle filters in Edit window
	 * @param dropEvent
	 */
	@Listen("onDrop = #filterListBox")
	public void onDropToFilterItem(final DropEvent dropEvent) {
		final Listitem draggedListitem = (Listitem) ((DropEvent) dropEvent).getDragged();
		
		chartData.setIsFiltered(true);
		Filter filter = new Filter();
		chartData.setFilter(filter);
		
		chartData.getFilter().setColumn(draggedListitem.getLabel());
		chartData.getFilter().setType((Integer) draggedListitem.getAttribute(Constants.COLUMN_DATA_TYPE));
		
		Listitem filterList = new Listitem();		
		Listcell labelCell = new Listcell(draggedListitem.getLabel());
		
		Button playBtn = new Button();
		playBtn.setSclass("glyphicon glyphicon-play btn btn-link img-btn");
		playBtn.setStyle("float:right");
		playBtn.addEventListener(Events.ON_CLICK, openFilterListener);
		
		Button closeBtn = new Button();
		closeBtn.setSclass("glyphicon glyphicon-remove btn btn-link img-btn");
		closeBtn.setStyle("float:right");
		closeBtn.addEventListener(Events.ON_CLICK, clearListener);
		labelCell.appendChild(closeBtn);
		
		labelCell.appendChild(playBtn);
		
		labelCell.setTooltiptext(chartData.getFilter().getColumn());
		
		filterList.appendChild(labelCell);
		filterList.setParent(filterListBox);
		
		try	{
			filterDataList = hpccService.fetchFilterData(chartData);			
		}
		catch(Exception e) {
			LOG.error("Error while fetching filter data from Hpcc..."+e.getMessage());
		}
		
	}
	
	//Listener to Open Filter Screen
	EventListener<Event> openFilterListener = new EventListener<Event>() {

		public void onEvent(final Event event) throws Exception {
			final Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("portlet", portlet);
			parameters.put("chartData", chartData);
			
			// Handling String dataType filters.Collections.sort() used to sort the numeric data 
			//to display the data in ascending order in filter window
			if (chartData.getFilter().getType().equals(Constants.STRING_DATA)) {
				
				chartData.getFilter().setValues(filterDataList);
				
				Collections.sort(chartData.getFilter().getValues());
				parameters.put("filterDataList", filterDataList);
				
				final Window window = (Window) Executions.createComponents(
						"/demo/filter_string_popup.zul", editPortletWindow,
						parameters);
				window.doModal();
			}
			// Handling Numeric dataType filters.Sorting 
			else if (chartData.getFilter().getType().equals(Constants.NUMERIC_DATA)){
				try {
					List<Integer> numericDataList = null;
					List<Double> doubleDataList = null;
					List<Float> realDataList = null;
					String filterDataType = wsdlOuputputMap.get(chartData.getFilter().getColumn());
					
					if (filterDataType.contains("integer")
							|| filterDataType.contains("unsigned")) {
						numericDataList = new ArrayList<Integer>();
						for (String filterData : filterDataList) {
							numericDataList.add(Integer.valueOf(filterData));
						}
						Collections.sort(numericDataList);
						parameters.put("filterDataList", numericDataList);
					}
					else if (filterDataType.contains("decimal")
							|| filterDataType.contains("real")) {
						doubleDataList = new ArrayList<Double>();
						for (String filterData : filterDataList) {
							doubleDataList.add(Double.valueOf(filterData));
						}
						Collections.sort(doubleDataList);
						parameters.put("filterDataList", doubleDataList);
					} else {
						Collections.sort(filterDataList);
						parameters.put("filterDataList", filterDataList);
					}
					if(LOG.isDebugEnabled())
					{
					LOG.debug("numericDataList -->" + numericDataList);
					LOG.debug("doubleDataList -->" + doubleDataList);
					LOG.debug("realDataList -->" + realDataList);
					}
				} catch (NumberFormatException exception) {
					LOG.error("Exception while type casting data in Filter ..."
							+ exception.getMessage());
				}
				final Window window = (Window) Executions.createComponents(
						"/demo/filter_numeric_popup.zul", editPortletWindow,
						parameters);
				window.doModal();
			}
		}

	};
	
	//Listener to close filter window
	EventListener<Event> clearListener = new EventListener<Event>() {

		public void onEvent(final Event event) throws Exception {			
			Listitem listItem =(Listitem) event.getTarget().getParent().getParent();			
			listItem.detach();
		}
	};
	
}
