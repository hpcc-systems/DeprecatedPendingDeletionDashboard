package org.hpccsystems.dashboard.entity.chart.utils; 

import java.io.IOException;
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
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.util.EncryptDecrypt;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.XYModel;
import org.hpccsystems.dashboard.services.HPCCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.util.Clients;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ChartRenderer {
	
	private static final  Log LOG = LogFactory.getLog(ChartRenderer.class);
	
	private HPCCService hpccService;
	
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
	public void constructChartJSON(XYChartData chartData, Portlet portlet, Boolean isEditWindow)throws Exception {

		final JsonArray array = new JsonArray();
		
		final JsonObject header = new JsonObject();
		StringBuilder yName = new StringBuilder();
		StringBuilder title = new StringBuilder();
		if(chartData.getYColumnNames().size() > 0 && 
				chartData.getXColumnNames().size() > 0) {
			header.addProperty("xName", chartData.getXColumnNames().get(0));
			
			for (String colName : chartData.getYColumnNames()) {
				yName.append(colName);
				yName.append(" & ");
			}
			yName.replace(yName.lastIndexOf("&"), yName.length(), "");
			header.addProperty("yName", yName.toString());
		}
		title.append(chartData.getXColumnNames().get(0) + " BY " + yName.toString());
		
		if(isEditWindow) {
			header.addProperty("portletId", "e_" + portlet.getId());
		} else {
			header.addProperty("portletId", "p_" + portlet.getId());
		}
		
		if(LOG.isDebugEnabled()){
			LOG.debug("Constructing chart \n Is chart has filters - " + chartData.getIsFiltered());
		}
		
		if(chartData.getIsFiltered() &&
				Constants.STRING_DATA.equals(chartData.getFilter().getType())) {
			header.addProperty("filterColumn", chartData.getFilter().getColumn());
			header.addProperty("stringFilter", 
					constructFilterTitle(chartData.getFilter().getValues()));
			
			title.append(" WHERE " + chartData.getFilter().getColumn());
			title.append(constructFilterTitle(chartData.getFilter().getValues()));
		
		} else if (chartData.getIsFiltered() && 
				Constants.NUMERIC_DATA.equals(chartData.getFilter().getType())) {
			header.addProperty("filterColumn", chartData.getFilter().getColumn());
			header.addProperty("from", chartData.getFilter().getStartValue());
			header.addProperty("to", chartData.getFilter().getEndValue());
			
			title.append(" WHERE " + chartData.getFilter().getColumn());
			title.append(" BETWEEN " + chartData.getFilter().getStartValue());
			title.append(" AND " + chartData.getFilter().getEndValue());
		}
		
				if(LOG.isDebugEnabled()){
			LOG.debug("Drawing chart");
			LOG.debug("Chart Type - " + portlet.getChartType());
		}	
		
			ArrayList<XYModel> list = null;
			Iterator<XYModel> iterator =null;	
			try
			{
			list =(ArrayList<XYModel>) getHpccService().getChartData(chartData);
			iterator = list.iterator();	
			}catch(Exception e)
			{
				throw e;
			}
			Integer yLength = 0;
			Integer xLength = 0;
			String xVal=null;BigDecimal yVal=null;
			JsonObject json = null;
			
			JsonArray xValues = new JsonArray();
			
			JsonArray rows = new JsonArray();
			JsonArray row = new JsonArray();
			
			for (String colName : chartData.getYColumnNames()) {
				row.add(new JsonPrimitive(colName));
			}
			rows.add(row);
			
			if(iterator != null){
			while(iterator.hasNext()){
				final XYModel bar = iterator.next();
				row = new JsonArray();
				for (Object object: bar.getyAxisValues()) {
					row.add(new JsonPrimitive((BigDecimal)object));
				}
				
				rows.add(row);
				xValues.add(new JsonPrimitive(bar.getxAxisVal().toString()));
				
				json = new JsonObject();
				json.addProperty("xData",(String) bar.getxAxisVal());
				
				JsonObject yNames = new JsonObject();
				for (String colName : chartData.getYColumnNames()) {
					if(Constants.BAR_CHART.equals(portlet.getChartType())){
						yNames.addProperty(colName, "bar");
					} else if(Constants.LINE_CHART.equals(portlet.getChartType())){
						yNames.addProperty(colName, "line");
					}
				}
				header.add("yNames", yNames);
				
				//TODO - make this logic dynamic
				if(Constants.BAR_CHART.equals(portlet.getChartType())){
					json.addProperty(chartData.getYColumnNames().get(0), (BigDecimal)bar.getyAxisValues().get(0));
					if(bar.getyAxisValues().size() > 1) {
						json.addProperty(chartData.getYColumnNames().get(1), (BigDecimal)bar.getyAxisValues().get(1));
						header.addProperty("secondLine", true);
					} else {
						header.addProperty("secondLine", false);
					}
				} else {
					json.addProperty("yData", (BigDecimal)bar.getyAxisValues().get(0));
					if(bar.getyAxisValues().size() > 1) {
						json.addProperty("yData2", (BigDecimal)bar.getyAxisValues().get(1));
						header.addProperty("secondLine", true);
					} else {
						header.addProperty("secondLine", false);
					}
				}
				
				array.add(json);

				//Finding word count in x Axis Labels
				xVal=(String) bar.getxAxisVal();
				yVal=(BigDecimal)bar.getyAxisValues().get(0);
				if(xVal.split(" ").length > xLength){
					xLength = xVal.split(" ").length;
				}	
				//Finding digit count in y axis values
				if(String.valueOf(yVal.intValue()).length() > yLength){
					yLength = String.valueOf(yVal.intValue()).length();
				}
			}	}
				
			//Adding a default pading of 5 and 10px per digit
			header.addProperty("yWidth", (yLength<2)? yLength*10 + 30:(yLength<3)? yLength*10 + 10: yLength*10);
			header.addProperty("xWidth", xLength*15 + 5);
			header.addProperty("title", title.toString());
			
			header.add("chartData", array);
			header.add("yValues", rows);
			header.add("xValues", xValues);
			
			final String data = header.toString();
			
			portlet.setChartDataJSON(data);
					
	}
	
	
	/**
	 * Must Construct JSON before invoking this method
	 * 
	 * Draws D3 chart onto the 'divToDraw' of the specified type
	 * Must construct JSON before calling this function
	 * 
	 * @param chartData
	 * @param chartType
	 * @param divToDraw
	 * @param portlet
	 */
	public void drawChart(XYChartData chartData, String divToDraw, Portlet portlet) throws Exception{

		if( portlet.getChartDataJSON() == null) {
			Clients.showNotification("No data available to draw Chart",	true);
		}	

		if((Constants.BAR_CHART.equals(portlet.getChartType()) || 
				Constants.LINE_CHART.equals(portlet.getChartType())) )	{
			Clients.evalJavaScript("createChart('" + divToDraw +  "','"+ portlet.getChartDataJSON() +"')" );
		}
		else if(Constants.PIE_CHART.equals(portlet.getChartType()))	{
			Clients.evalJavaScript("createPieChart('" + divToDraw +  "','"+ portlet.getChartDataJSON() +"')" ); 
		} 
	}
	
	/**
	 * constructing title for chart for string Filter Values 
	 * @param filteredValues
	 * @return String
	 */
	private String constructFilterTitle(List<String> filteredValues)
	{
		StringBuffer stringFilterValue = new StringBuffer(" IS ");
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
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public XYChartData parseXML(String xml) throws ParserConfigurationException, SAXException, IOException {
		String encryptedpassWord="";
		String decryptedPassword="";
		EncryptDecrypt decrypter = new EncryptDecrypt("");
		XYChartData chartData = null;
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(XYChartData.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			chartData = (XYChartData) jaxbUnmarshaller.unmarshal(new StringReader(xml));
			//decrypt password
			encryptedpassWord = chartData.getHpccConnection().getPassword();
			decryptedPassword = decrypter.decrypt(encryptedpassWord);
			chartData.getHpccConnection().setPassword(decryptedPassword);
		} catch (JAXBException e) {
			LOG.error("EXCEPTION: JAXBException in ChartRenderer",e);
		} catch (Exception e) {
			LOG.error("EXCEPTION in parseXML()",e);
		}		
		return chartData;
	}
		
	/**
	 * Constructs XML string from the Object
	 * @param chartData
	 * @return
	 */
	public String convertToXML(XYChartData chartData) {
		
		//encrypt password
		EncryptDecrypt encrypter = new EncryptDecrypt("");
		String encrypted = encrypter.encrypt(chartData.getHpccConnection().getPassword());
		chartData.getHpccConnection().setPassword(encrypted);
		java.io.StringWriter sw = new StringWriter();
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(XYChartData.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.marshal(chartData, sw);
		} catch (JAXBException e) {
			LOG.error("EXCEPTION: JAXBException in ChartRenderer",e);
		}
	    return sw.toString();
	}
}
