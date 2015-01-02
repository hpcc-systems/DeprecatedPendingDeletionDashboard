package org.hpccsystems.dashboard.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.ChartdataJSON;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.NumericFilter;
import org.hpccsystems.dashboard.entity.widget.StringFilter;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import ws_sql.ws.hpccsystems.ExecuteSQLRequest;
import ws_sql.ws.hpccsystems.Ws_sqlLocator;
import ws_sql.ws.hpccsystems.Ws_sqlServiceSoap;



@Service("wssqlService")
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public  class WSSQLServiceImpl implements WSSQLService{
	private static final Logger LOGGER =LoggerFactory.getLogger(WSSQLServiceImpl.class);
	
    private static final String SELECT = "select ";
    private static final String WHERE_WITH_SPACES = " where ";
    
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    
    private static final String UNAUTHORIZED = "Unauthorized";  
	
	private String executeSQL(HPCCConnection hpccConnection, String sql)
			throws Exception {

		String resultString = null;
		Ws_sqlLocator locator = new Ws_sqlLocator();
		StringBuilder endpoint = new StringBuilder();
		if (hpccConnection.isHttps) {
			endpoint.append(HTTPS);
		} else {
			endpoint.append(HTTP);
		}
		
		// TO DO add the IP and  port number in HIPIE  and then implement
		endpoint.append("216.19.105.2:18009"); 
		endpoint.append("/ws_sql?ver_=1");

		if(LOGGER.isDebugEnabled()) {
		    LOGGER.debug("WS SQL End point - {}", endpoint.toString());
		    LOGGER.debug("Connection name - {}, Password - {}", hpccConnection.getUserName(), hpccConnection.getPwd());
		}
		
		locator.setWs_sqlServiceSoapAddress(endpoint.toString());
		locator.setWs_sqlServiceSoap_userName(hpccConnection.getUserName());
		//TODO Find out the way to get unencryped password from HIPIE
		locator.setWs_sqlServiceSoap_password("Lexis123!");

		ExecuteSQLRequest req = new ExecuteSQLRequest();
		req.setSqlText(sql);
		req.setTargetCluster("thor");
		req.setIncludeResults(true);

		Ws_sqlServiceSoap soap = locator.getws_sqlServiceSoap();
		resultString = soap.executeSQL(req).getResult();

		if (resultString != null && resultString.length() > 0) {
			resultString = StringUtils.substringBetween(resultString,
					"<Dataset", "</Dataset>");
			resultString = "<Dataset" + resultString + "</Dataset>";
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Result String: Afetr removing schema tag "
					+ resultString);
		}

		return resultString;

	}

	/* Sample response xml
	 * 
	 * <Dataset name='WsSQLResult'> <Row><productcode>S10_1678</productcode><quantityinstock>7933</quantityinstock><buyprice>48.81</buyprice></Row></Dataset>
	 * 
	 */
	private static ChartdataJSON parseChartdataResponse(List<String> columns, String responseXML){
        ChartdataJSON dataJSON = null;
    	if (responseXML != null && responseXML.length() > 0) {
    		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    		try {
				XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(responseXML));
				List <List<Object>> dataList=new ArrayList<List<Object>>();List<Object> dataRowList=null;
				while(xmlEventReader.hasNext()){
					XMLEvent xmlEvent = xmlEventReader.nextEvent();
					if (xmlEvent.isStartElement()){
	                       StartElement startElement = xmlEvent.asStartElement();
	                       if(startElement.getName().getLocalPart().equals("Row")){
	                    	   dataRowList=new ArrayList<Object>();
	                       }
	                       else if(!startElement.getName().getLocalPart().equals("Dataset")){
	                           xmlEvent = xmlEventReader.nextEvent();
	                           if(xmlEvent.isCharacters()){
	                        	   dataRowList.add(xmlEvent.asCharacters().getData());
	 	                           }else{
	 	                        	  dataRowList.add("0");
	 	                           }
	                          
	                       }
	                }
					if(xmlEvent.isEndElement()){
                       EndElement endElement = xmlEvent.asEndElement();
                       if(endElement.getName().getLocalPart().equals("Row")){
                    	   dataList.add(dataRowList);
                       }
	               }
				}
				dataJSON=new ChartdataJSON();
				dataJSON.setColumns(columns);
				dataJSON.setData(dataList);
				if(LOGGER.isDebugEnabled()){
				    LOGGER.info("Columns {}", columns);
				}
				
			} catch (XMLStreamException e) {
				LOGGER.error(Constants.EXCEPTION, e);
				//TODO Throw
			}
    	}
        return dataJSON;
    }
    
    @Override
    public List<String> getDistinctValues(Field field, HPCCConnection connection, String fileName, List<Filter> filters) throws Exception  {
    	 List<String> dataList = null;
         try {
             final StringBuilder queryTxt = new StringBuilder(SELECT);
             queryTxt.append(fileName);
             queryTxt.append(".");
             queryTxt.append(field.getColumn());
             queryTxt.append(" from ");
             queryTxt.append(fileName);

             if (filters != null && !filters.isEmpty()) {            	 
                queryTxt.append(constructWhereClause(filters ,fileName));
             }

             queryTxt.append(" group by ");
             queryTxt.append(fileName);
             queryTxt.append(".");
             queryTxt.append(field.getColumn());

             if (LOGGER.isDebugEnabled()) {
            	 LOGGER.debug("Query for Distinct values -> " + queryTxt.toString());
             }

             final String resultString = executeSQL(connection, queryTxt.toString());
             if (resultString != null && resultString.length() > 0) {
            	 XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            	 XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(resultString));
 				 dataList=new ArrayList<String>();
 				while(xmlEventReader.hasNext()){
 					XMLEvent xmlEvent = xmlEventReader.nextEvent();
 					if (xmlEvent.isStartElement()){
 	                       StartElement startElement = xmlEvent.asStartElement();
 	                       if(startElement.getName().getLocalPart().equals("Row") || startElement.getName().getLocalPart().equals("Dataset")){
 	                    	   continue;
 	                       }
 	                       else {
 	                           xmlEvent = xmlEventReader.nextEvent();
 	                           if(xmlEvent.isCharacters()){
 	                        	  dataList.add(xmlEvent.asCharacters().getData());
 	                           }else{
 	                        	  dataList.add("");
 	                           }
 	                          
 	                       }
 	                }
 				}
            	 
             }else{
            	 throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
             }
                 
         }  catch (RemoteException e) {
             if (e.getMessage().contains(UNAUTHORIZED)) {
                 throw new HpccConnectionException("401 Unauthorized");
             }
             LOGGER.error(Constants.EXCEPTION, e);
             throw e;
         } catch (ServiceException | ParserConfigurationException | SAXException | IOException ex) {
        	 LOGGER.error(Constants.EXCEPTION, ex);
             throw ex;
         }
         return dataList;
    }

    @Override
    public Map<String, BigDecimal> getMinMax(Field field, HPCCConnection connection, String fileName, List<Filter> filters) throws Exception {
    	Map<String, BigDecimal> resultMap = null;

          try {
        	  
              final StringBuilder queryTxt = new StringBuilder("select min(").append(fileName).append(".")
                      .append(field).append("), max(").append(fileName).append(".").append(field)
                      .append(") from ");

              if (!filters.isEmpty()) {
                      queryTxt.append(fileName);
                      queryTxt.append(constructWhereClause(filters ,fileName));
                  }
               else {
                  queryTxt.append(fileName);
              }

              final String resultString = executeSQL(connection, queryTxt.toString());

              if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("queryTxt in fetchFilterMinMax() -->" + queryTxt);
              }
              if (resultString != null && resultString.length() > 0) {
            	  
            	 XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            	 XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(resultString));
            	 resultMap=new HashMap<String, BigDecimal>();
  				while(xmlEventReader.hasNext()){
  					XMLEvent xmlEvent = xmlEventReader.nextEvent();
  					if (xmlEvent.isStartElement()){
  	                       StartElement startElement = xmlEvent.asStartElement();
  	                       if(startElement.getName().getLocalPart().equals("Row") || startElement.getName().getLocalPart().equals("Dataset")){
  	                    	   continue;
  	                       }
  	                       else if(startElement.getName().getLocalPart().equals("minout1")){
  	                           xmlEvent = xmlEventReader.nextEvent();
  	                         if(xmlEvent.isCharacters())
  	                        	resultMap.put("min", new BigDecimal((xmlEvent.asCharacters().getData())));
  	                        else
  	                        	resultMap.put("min",new BigDecimal(0));
  	                       }
  	                       else{
	                           xmlEvent = xmlEventReader.nextEvent();
							if (xmlEvent.isCharacters())
								resultMap.put("max", new BigDecimal(xmlEvent
										.asCharacters().getData()));
							else
								resultMap.put("max", new BigDecimal(0));
						}
  	                }
  				}
            	  
              } else {
            	  throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
              }

          } catch (RemoteException e) {
              if (e.getMessage().contains(UNAUTHORIZED)) {
                 throw new HpccConnectionException("401 Unauthorized");
              }
              LOGGER.error(Constants.EXCEPTION, e);
             throw e;
          } catch (ServiceException | ParserConfigurationException | SAXException | IOException ex) {
              LOGGER.error(Constants.EXCEPTION, ex);
             throw new HpccConnectionException();
          }
          return resultMap;
    }

    @Override
    public ChartdataJSON getChartdata(Widget widget, HPCCConnection connection) throws Exception {

        final String queryTxt = widget.generateSQL();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("wssql -->" + widget.getLogicalFile());
            LOGGER.debug("WS_SQL Query ->" + queryTxt);
        }

        final String resultString = executeSQL(connection, queryTxt);
        return parseChartdataResponse(widget.getColumns(), resultString);
    }
   
    private String constructWhereClause(List<Filter> filters, String fileName) {
        StringBuilder queryTxt = new StringBuilder();
        queryTxt.append(WHERE_WITH_SPACES);

            Iterator<Filter> iterator = filters.iterator();
            while (iterator.hasNext()) {
                Filter filter = iterator.next();
                if(!filter.hasValues()) {
                    continue;
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Contructing where clause " + filter.toString());
                }

                queryTxt.append("(");

                NumericFilter nFilter = null;
                StringFilter sfilter = null;
                
                if(filter instanceof StringFilter){
                    sfilter = (StringFilter)filter;
                    queryTxt.append(fileName);
                    queryTxt.append(".");
                    queryTxt.append(filter.getColumn());
                    queryTxt.append(" in ");
                    queryTxt.append(" (");
                    
                    sfilter.getValues().forEach(value -> {
                        queryTxt.append(" '").append(value).append("'");
                        queryTxt.append(",");
                    });
                    
                    queryTxt.deleteCharAt(queryTxt.length()-1);
                    queryTxt.append(" )");
                } else if(filter instanceof NumericFilter){
                    nFilter = (NumericFilter)filter;
                    queryTxt.append(fileName);
                    queryTxt.append(".");
                    queryTxt.append(nFilter.getColumn());
                    queryTxt.append(" >= ");
                    queryTxt.append(nFilter.getMinValue());
                    queryTxt.append(" and ");
                    queryTxt.append(fileName);
                    queryTxt.append(".");
                    queryTxt.append(filter.getColumn());
                    queryTxt.append(" <= ");
                    queryTxt.append(((NumericFilter) filter).getMinValue());
                }

                queryTxt.append(")");

                if (iterator.hasNext()) {
                    queryTxt.append(" AND ");
                }
            }
        
    
        return queryTxt.length() <= WHERE_WITH_SPACES.length() ? "" : queryTxt.toString();
    }
}
