package org.hpccsystems.dashboard.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.utils.ChartRenderer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Panelchildren;
import org.zkoss.zul.Slider;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;


/**
 * Class to handle Filters in the edit chart page
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class FilterController extends SelectorComposer<Component>{
	private static final long serialVersionUID = 1L;
	private static final  Log LOG = LogFactory.getLog(FilterController.class); 
	
	Portlet portlet;
	XYChartData chartData;
	
	List<Object> filterDataList;

	@Wire
	Slider sliderRange;
	
	@Wire
	Textbox amountFrom;
	
	@Wire
	Textbox amountTo;
	
	//For filter_string_popup.zul
	
	@Wire
	Panel idPanel1;
	
	@Wire
	Panelchildren idPanelChildren;
	
	@Wire
	Listbox idAllValues;
	
	@Wire
	Listbox idFilteredValues;
	
	@Wire
	Button idAdd;
	
	@Wire
	Window idStringFilter;
	
	@Wire 
	Button populateData;
	
	@Wire
	Button close;
	
	@WireVariable
	ChartRenderer chartRenderer;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		final Execution execution = Executions.getCurrent();
		Map args = execution.getArg();
		portlet = (Portlet) args.get("portlet");
		chartData = (XYChartData) args.get("chartData");
		filterDataList = (List<Object>) args.get("filterDataList");
				
		if(chartData.getFilter().getType().equals(Constants.STRING_DATA))
		{
			idStringFilter.setTitle(chartData.getFilter().getColumn());
		}
		
		filterDataList = (List<Object>)execution.getArg().get("filterDataList");
		Listitem listItem=null;
		if(filterDataList.get(0) instanceof String){
		for(Object filterData : filterDataList){
			listItem= new Listitem((String)filterData);
			idAllValues.appendChild(listItem);
			}
		}

		//sets values to slider in Numeric filter
		if(chartData.getFilter().getType().equals(Constants.NUMERIC_DATA))
		{
			sliderRange.setMaxpos(new BigDecimal((filterDataList.get(filterDataList.size()-1).toString())).intValue());
			sliderRange.setCurpos(new BigDecimal(filterDataList.get(0).toString()).intValue());
			amountFrom.setValue(String.valueOf(filterDataList.get(0)));
			amountTo.setValue(String.valueOf(filterDataList.get(filterDataList.size()-1)));
		}
	}
	
	@Listen("onScroll=#sliderRange")
	public void onScrollSlider(final Event event) {
		amountFrom.setValue(String.valueOf(sliderRange.getCurpos()));
		amountTo.setValue(String.valueOf(sliderRange.getMaxpos()));
		
		chartData.getFilter().setStartValue(Double.valueOf(amountFrom.getValue()));
		chartData.getFilter().setEndValue(Double.valueOf(amountTo.getValue()));
		
		//passing X,Y axis values to draw the chart
		chartRenderer.constructChartJSON(chartData, portlet, true);
		chartRenderer.drawChart(chartData, Constants.EDIT_WINDOW_CHART_DIV, portlet);
	}

	/**
	 * chooseItem() is used to Add the filter item to filter page.
	 * @param event
	 */
	@Listen("onClick = #idAdd")
	public void chooseItem(final Event event) {

		List<Listitem> listItem = new ArrayList<Listitem>(idAllValues.getItems());
		for (int i = 0; i < listItem.size(); i++) {
			if (listItem.get(i).isSelected()) {
				idFilteredValues.appendChild(listItem.get(i));
			}
		}

	}
	
	/**
	 * removeItem() is used to remove the filter item form filter page.
	 * @param event
	 */
	@Listen("onClick = #idRemove")
	public void removeItem(final Event event){
		for (Listitem li : idFilteredValues.getSelectedItems()) {
			if(li instanceof Listitem && li.isSelected()){
				idFilteredValues.removeChild(li);
			}
		}
	}
	
	/**
	 * removeAllItem() is used to remove all the filter Items from filter page.
	 * @param event
	 */
	@Listen("onClick = #idRemoveAll")
	public void removeAllItem(final Event event){
			idFilteredValues.getItems().clear();
		}	
	
	/**
	 * populateChart() is used to draw the chart by filtered values.
	 * @param event
	 */
	@Listen("onClick=#populateData")
	public void populateChart(final Event event){
		List<String> filterListValues = null;
		List<Listitem> listItem = idFilteredValues.getItems();
		if(listItem != null && !listItem.isEmpty())
		{
			filterListValues = new ArrayList<String>();
			for(Listitem listItemValue : listItem){			
				filterListValues.add(listItemValue.getLabel());	
				}
		}
	
		chartData.getFilter().setValues(filterListValues);
		
		//passing X,Y axis values to draw the chart
		chartRenderer.constructChartJSON(chartData, portlet, true);
		chartRenderer.drawChart(chartData, Constants.EDIT_WINDOW_CHART_DIV, portlet);
	}
	
	/**
	 * closeFilterPage() is used to close the Filter String page after values are selected.
	 */
	@Listen("onClick=#close")
	 public void closeFilterPage(){
		idStringFilter.detach();
	}
	}
