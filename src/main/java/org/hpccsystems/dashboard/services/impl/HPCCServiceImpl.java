package org.hpccsystems.dashboard.services.impl;

import java.io.StringReader;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.XYModel;
import org.hpccsystems.dashboard.services.HPCCService;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import ws_sql.ws.hpccsystems.ExecuteSQLRequest;
import ws_sql.ws.hpccsystems.ExecuteSQLResponse;
import ws_sql.ws.hpccsystems.Ws_sqlLocator;
import ws_sql.ws.hpccsystems.Ws_sqlServiceSoap;
import wsdfu.ws.hpccsystems.DFUInfoRequest;
import wsdfu.ws.hpccsystems.DFUInfoResponse;
import wsdfu.ws.hpccsystems.WsDfuLocator;
import wsdfu.ws.hpccsystems.WsDfuServiceSoap;

public class HPCCServiceImpl implements HPCCService{
	
	private static final  Log LOG = LogFactory.getLog(HPCCServiceImpl.class); 
	
	final static String WS_SQL_ENDPOINT = ":18009/ws_sql?ver_=1";
	final static String DFU_ENDPOINT = ":18010/WsDfu?ver_=1.2";
	 /**
	  * getColumnSchema() is used to retrieve the ColumnSchema details from HPCC systems 
	  * to pass column data details to Edit Chart page to generate the D3 charts. 
	 * @param sql
	 * @param userName
	 * @param password
	 * @param url
	 * @return Map<String,String>
	 */
	
	public Map<String,String> getColumnSchema(final String Sql,final String userName,final String password,final String url)
	{
		final HashMap<String, String> schemaMap=new HashMap<String, String>();
		
		String[] rowObj=null,columnObj=null;
		try 
		{
			final WsDfuLocator locator = new WsDfuLocator();
			locator.setWsDfuServiceSoap_userName(userName);
			locator.setWsDfuServiceSoap_password(password);
			locator.setWsDfuServiceSoapAddress(url + DFU_ENDPOINT);
			
			final WsDfuServiceSoap soap = locator.getWsDfuServiceSoap();
			final DFUInfoRequest req = new DFUInfoRequest();
			req.setName(Sql);
			req.setCluster("mythor");
			final DFUInfoResponse result = soap.DFUInfo(req);	
			//The below properties needs to be set in later phase of the project 
			if (LOG.isDebugEnabled()) {
				LOG.debug(Constants.COLUMN_NAME+userName);
				LOG.debug(Constants.EDIT_URL+url);
				LOG.debug(Constants.EDIT_SQL+Sql);
			}
			//Two types of column schema results been parsed here to 
			// get column name and datatype
			if(result.getFileDetail()!=null)
			{
				final StringBuilder resultString =new StringBuilder(result.getFileDetail().getEcl());
				if(resultString.indexOf("{")!=-1)
				{
					resultString.replace(resultString.length()-3, resultString.length(), "").replace(0, 1, "");
					rowObj=resultString.toString().trim().split(",");
				}
				else
				{
					resultString.replace(resultString.indexOf("RECORD"), resultString.indexOf("RECORD")+6,"");
					resultString.replace(resultString.indexOf("END"), resultString.indexOf("END")+4,"");
					rowObj=resultString.toString().split(";");
				}
				for(String rowString:rowObj)
				{
					rowString=rowString.trim();
					if(rowString!=null && rowString.length()>0)
					{
						columnObj=rowString.split(" ");
						if(columnObj!=null && columnObj.length>1){
						schemaMap.put(columnObj[1], columnObj[0]);
						}
					}
				}
			}
			else{
				throw new Exception(Constants.ERROR_RETRIEVE_COLUMNS);
			}
		} catch (ServiceException e) {
			LOG.error("ServiceException", e);
			schemaMap.put(Constants.ERROR,Constants.ERROR_HPCC_SERVER);
		} catch (RemoteException e) {
			LOG.error("RemoteException", e);
			schemaMap.put(Constants.ERROR,Constants.ERROR_HPCC_SERVER);
		}
		catch (Exception e) {
			LOG.error("Exception", e);
			schemaMap.put(Constants.ERROR,e.getMessage());
		}
		if(LOG.isDebugEnabled()){
			LOG.debug(schemaMap);
		}	
		return schemaMap;
	}
	

	 /**
	  * getChartData() is used the retrieve the ChartData details and render the D3 charts.
	 * @param chartParamsMap
	 * @return List<BarChart>
	 * 
	 */
	
	public List<XYModel> getChartData(XYChartData chartData)
	{
		final List<XYModel> dataList=new ArrayList<XYModel>();
		XYModel dataObj=null;
		try {
			final Ws_sqlLocator locator = new Ws_sqlLocator();
			locator.setWs_sqlServiceSoap_userName(chartData.getUserName());
			locator.setWs_sqlServiceSoap_password(chartData.getPassword());
			locator.setWs_sqlServiceSoapAddress(chartData.getURL() + WS_SQL_ENDPOINT);
			
			final Ws_sqlServiceSoap soap = locator.getws_sqlServiceSoap();
			final ExecuteSQLRequest req = new ExecuteSQLRequest();
			final String queryTxt = constructQuery(chartData);
			req.setSqlText(queryTxt.toString());
			req.setTargetCluster("thor");
			final ExecuteSQLResponse result = soap.executeSQL(req);
			final String resultString = result.getResult();
			if(LOG.isDebugEnabled())
				LOG.debug("Result String: " + resultString);
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			final DocumentBuilder db = dbf.newDocumentBuilder();
			final InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(resultString));
			final Document doc = db.parse(inStream);
			Node fstNode=null;Element fstElmnt=null,lstNmElmnt=null;NodeList lstNmElmntLst=null,lstNm=null;
			String nodeValue=null;
			final NodeList nodeList = doc.getElementsByTagName("Row");
				for (int s = 0; s < nodeList.getLength(); s++) {
					 fstNode = nodeList.item(s);
					  if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
						  dataObj=new XYModel();
					    fstElmnt = (Element) fstNode;
					    lstNmElmntLst = fstElmnt.getElementsByTagName(chartData.getXColumnName());
					    lstNmElmnt = (Element) lstNmElmntLst.item(0);
					    lstNm = lstNmElmnt.getChildNodes();
					    nodeValue = ((Node) lstNm.item(0)).getNodeValue();
					    dataObj.setxAxisVal(nodeValue);
					    lstNmElmntLst = fstElmnt.getElementsByTagName(chartData.getYColumnName());
					    lstNmElmnt = (Element) lstNmElmntLst.item(0);
					    lstNm = lstNmElmnt.getChildNodes();
					    nodeValue = ((Node) lstNm.item(0)).getNodeValue();
					    dataObj.setyAxisVal(new BigDecimal(nodeValue));
					    dataList.add(dataObj);
					  }
				}
		} catch (ServiceException e) {
			LOG.error("ServiceException", e);
		} catch (RemoteException e) {
			LOG.error("RemoteException", e);
		}
		catch (Exception e) {
			LOG.error("Exception", e);
		}
		return dataList;
	}
	

	/* 
	 * Method to get values for filter column
	 *
	 */
	public List<String> fetchFilterData(XYChartData chartData) throws Exception{
		final StringBuilder queryTxt=new StringBuilder("select ");
		
		final Ws_sqlLocator locator = new Ws_sqlLocator();
		locator.setWs_sqlServiceSoap_userName(chartData.getUserName());
		locator.setWs_sqlServiceSoap_password(chartData.getPassword());
		locator.setWs_sqlServiceSoapAddress(chartData.getURL() + WS_SQL_ENDPOINT);
		
		final Ws_sqlServiceSoap soap = locator.getws_sqlServiceSoap();
		final ExecuteSQLRequest req = new ExecuteSQLRequest();
		queryTxt.append(chartData.getFilter().getColumn());
		queryTxt.append(" from ");
		queryTxt.append(chartData.getFileName());
		queryTxt.append(" limit 7");
		req.setSqlText(queryTxt.toString());
		req.setTargetCluster("thor");
		final ExecuteSQLResponse result = soap.executeSQL(req);
		final String resultString = result.getResult();
		if(LOG.isDebugEnabled()){
			LOG.debug("Hitting URL for filter - " + locator.getws_sqlServiceSoapAddress());
			LOG.debug("Result String: " + resultString);
		}
		
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		final DocumentBuilder db = dbf.newDocumentBuilder();
		final InputSource inStream = new InputSource();
		inStream.setCharacterStream(new StringReader(resultString));
		final Document doc = db.parse(inStream);
		Node fstNode=null;Element fstElmnt=null,lstNmElmnt=null;NodeList lstNmElmntLst=null,lstNm=null;
		String nodeValue=null;
		List<String> filterDataList = new ArrayList<String>();
		final NodeList nodeList = doc.getElementsByTagName("Row");
		if(nodeList != null)
		{
		for (int count = 0; count < nodeList.getLength(); count++) {
			 fstNode = nodeList.item(count);
			  if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
			    fstElmnt = (Element) fstNode;
			    lstNmElmntLst = fstElmnt.getElementsByTagName(chartData.getFilter().getColumn());
			    lstNmElmnt = (Element) lstNmElmntLst.item(0);
			    lstNm = lstNmElmnt.getChildNodes();
			    nodeValue = ((Node) lstNm.item(0)).getNodeValue();
			    filterDataList.add(nodeValue);
			  }
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug(("filterDataList -->" + filterDataList));
			}
		}
		return filterDataList;
	}

	
	/**
	 * Method to generate query for Hpcc
	 * @param chartParamsMap
	 * @return StringBuilder
	 * 
	 */
	private String constructQuery(XYChartData chartData)
	{
		
		StringBuilder queryTxt=new StringBuilder("select ");
		
		if(!chartData.getIsFiltered())
		{
			//Query without filters
			queryTxt.append(chartData.getXColumnName());
			queryTxt.append(",");
			queryTxt.append(chartData.getYColumnName());
			queryTxt.append(" from ");
			queryTxt.append(chartData.getFileName());
			//queryTxt.append(" limit 7");
		} else if( chartData.getIsFiltered() &&
				Constants.STRING_DATA.equals(chartData.getFilter().getType())) {
			//Query with String filters
			queryTxt.append(chartData.getXColumnName());
			queryTxt.append(",");
			queryTxt.append(chartData.getYColumnName());
			queryTxt.append(" from ");
			queryTxt.append(chartData.getFileName());
			queryTxt.append(" where ");
			queryTxt.append(chartData.getFilter().getColumn());
			queryTxt.append(" in ");
			queryTxt.append(" (");
			
			for(int i=1;i<= chartData.getFilter().getValues().size(); i++){
				
				queryTxt.append(" '").append( chartData.getFilter().getValues().get(i-1)).append("'");
				
				if(i<chartData.getFilter().getValues().size()){
					queryTxt.append(",");
				}	
			}
			
			queryTxt.append(" )");
			//queryTxt.append(" limit 7");
			
		} else if( chartData.getIsFiltered() &&
				Constants.NUMERIC_DATA.equals(chartData.getFilter().getType())) {
			//Query with Numeric filter
			queryTxt.append(chartData.getXColumnName());
			queryTxt.append(",");
			queryTxt.append(chartData.getYColumnName());
			queryTxt.append(" from ");
			queryTxt.append(chartData.getFileName());
			queryTxt.append(" where ");
			queryTxt.append(chartData.getFilter().getColumn());
			queryTxt.append(" > ");
			queryTxt.append(chartData.getFilter().getStartValue());
			queryTxt.append(" and ");
			queryTxt.append(chartData.getFilter().getColumn());
			queryTxt.append(" < ");
			queryTxt.append(chartData.getFilter().getEndValue());
			//queryTxt.append(" limit 7");
		}
		return queryTxt.toString();
	}

}
