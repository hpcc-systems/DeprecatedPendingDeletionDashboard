package org.hpccsystems.dashboard.entity.chart.utils;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.XYModel;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.util.Clients;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ChartRenderer {
	
	private static final  Log LOG = LogFactory.getLog(ChartRenderer.class);
	
	private DashboardService dashboardService;
	private HPCCService hpccService;
	
	public DashboardService getDashboardService() {
		return dashboardService;
	}

	@Autowired
	public void setDashboardService(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	public HPCCService getHpccService() {
		return hpccService;
	}

	@Autowired
	public void setHpccService(HPCCService hpccService) {
		this.hpccService = hpccService;
	}
	
	/**
	 * Constructs the JSON Object required to draw D3 graph and places the constructed JSON in Portlet Object
	 * @param chartData
	 * 	Chart Data that contains details to draw chart
	 * @param chartType
	 * @param portlet
	 * 	Portlet Object for which chartData is generate. 
	 * 	The JSON data constructed will be available in this portlet.
	 * 
	 * @return
	 *  Generates JSON into passed Portlet object	
	 */
	public void constructChartJSON(XYChartData chartData, Portlet portlet, Boolean isEditWindow) {

		final JsonArray array = new JsonArray();
		
		final JsonObject header = new JsonObject();
		header.addProperty("xName", chartData.getXColumnName());
		header.addProperty("yName", chartData.getYColumnName());
		if(isEditWindow) {
			header.addProperty("portletId", "e_" + portlet.getId());
		} else {
			header.addProperty("portletId", "p_" + portlet.getId());
		}
		
		if(LOG.isDebugEnabled()){
			LOG.debug("Constructing chart \n Is chart has filters - " + chartData.isFiltered());
		}
		
		if(chartData.isFiltered() &&
				Constants.STRING_DATA.equals(chartData.getFilter().getType())) {
			header.addProperty("filterColumn", chartData.getFilter().getColumn());
			header.addProperty("stringFilter", 
					constructFilterTitle(chartData.getFilter().getValues()));
		
		} else if (chartData.isFiltered() && 
				Constants.NUMERIC_DATA.equals(chartData.getFilter().getType())) {
			header.addProperty("filterColumn", chartData.getFilter().getColumn());
			header.addProperty("from", chartData.getFilter().getStartValue());
			header.addProperty("to", chartData.getFilter().getEndValue());
		}
		
		if(LOG.isDebugEnabled()){
			LOG.debug("Drawing chart");
			LOG.debug("Chart Type - " + portlet.getChartType());
		}	
		
			ArrayList<XYModel> list = (ArrayList<XYModel>) getHpccService().getChartData(chartData);
			final Iterator<XYModel> iterator = list.iterator();

			Integer yLength = 0;
			Integer xLength = 0;
			String xVal=null;BigDecimal yVal=null;
			JsonObject json = null;
			while(iterator.hasNext()){
				final XYModel bar = iterator.next();
				json = new JsonObject();
				json.addProperty("xData",(String) bar.getxAxisVal());
				json.addProperty("yData", (BigDecimal)bar.getyAxisVal());
				array.add(json);

				//Finding word count in x Axis Labels
				xVal=(String) bar.getxAxisVal();
				yVal=(BigDecimal)bar.getyAxisVal();
				if(xVal.split(" ").length > xLength){
					xLength = xVal.split(" ").length;
				}	
				//Finding digit count in y axis values
				if(String.valueOf(yVal.intValue()).length() > yLength){
					yLength = String.valueOf(yVal.intValue()).length();
				}
			}	
				
			//Adding a default pading of 5 and 10px per digit
			header.addProperty("yWidth", (yLength<2)? yLength*10 + 30:(yLength<3)? yLength*10 + 10: yLength*10);
			header.addProperty("xWidth", xLength*15 + 5);
			
			header.add("chartData", array);
			
			final String data = header.toString();
			
			portlet.setWidgetState(Constants.STATE_LIVE_CHART);
			portlet.setChartDataJSON(data);
					
	}
	
	
	/**
	 * Must Construct JSON before invoking this method
	 * 
	 * Draws D3 chart onto the 'divToDraw' of the specified type
	 * Constructs the JSON data for the portlet from chartData, if JSON is not constructed already
	 * 
	 * @param chartData
	 * @param chartType
	 * @param divToDraw
	 * @param portlet
	 */
	public void drawChart(XYChartData chartData, String divToDraw, Portlet portlet) {

		if( portlet.getChartDataJSON() == null) {
			if(divToDraw.equals(Constants.EDIT_WINDOW_CHART_DIV)){
				constructChartJSON(chartData, portlet, true);
			} else {
				constructChartJSON(chartData, portlet, false);
			}
		}
		
		if(Constants.BAR_CHART.equals(portlet.getChartType()) )	{
			Clients.evalJavaScript("createChart('" + divToDraw +  "','"+ portlet.getChartDataJSON() +"')" );
		}
		else if(Constants.PIE_CHART.equals(portlet.getChartType()))	{
			Clients.evalJavaScript("createPieChart('" + divToDraw +  "','"+ portlet.getChartDataJSON() +"')" ); 
		} 
		else if (Constants.LINE_CHART.equals(portlet.getChartType())) {
			Clients.evalJavaScript("createLineChart('" + divToDraw +  "','"+ portlet.getChartDataJSON() +"')" );
		}		 
	}
	
	/**
	 * constructing title for chart for string Filter Values 
	 * @param filteredValues
	 * @return String
	 */
	private String constructFilterTitle(List<String> filteredValues)
	{
		StringBuffer stringFilterValue = new StringBuffer(" is ");
		int index=0;
		for(String filter :filteredValues)
		{
			if(index<5 && index != filteredValues.size()-1)
			{
			stringFilterValue.append(filter).append(",");
			}
			else if(index<5 && index == filteredValues.size()-1)
			{
				stringFilterValue.append(filter);
			}
			else if(index >= 5)
			{
				stringFilterValue.append(", ...").append(filteredValues.size()-5).append(" more");
			}
			index++;				
		}
		return stringFilterValue.toString();
	}
	
	/**
	 * Parses the XML string and returns as java Object
	 * @param xml
	 * @return
	 * 	XYChartData object
	 */
	public XYChartData parseXML(String xml) {
		
		XYChartData chartData = null;
		
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(XYChartData.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			chartData = (XYChartData) jaxbUnmarshaller.unmarshal( new StringReader(xml));
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		 
		return chartData;
	}
	
	/**
	 * Constructs XML string from the Object
	 * @param chartData
	 * @return
	 */
	public String convertToXML(XYChartData chartData) {
		
		java.io.StringWriter sw = new StringWriter();
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(XYChartData.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.marshal(chartData, sw);
		} catch (JAXBException e) {
			e.printStackTrace();
		}

	    return sw.toString();
	}
}
