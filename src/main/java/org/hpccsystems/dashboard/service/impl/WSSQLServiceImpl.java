package org.hpccsystems.dashboard.service.impl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import org.apache.commons.lang.StringUtils;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.ChartdataJSON;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.NumericFilter;
import org.hpccsystems.dashboard.entity.widget.StringFilter;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import ws_sql.ws.hpccsystems.ExecuteSQLRequest;
import ws_sql.ws.hpccsystems.Ws_sqlLocator;
import ws_sql.ws.hpccsystems.Ws_sqlServiceSoap;

import com.mysql.jdbc.Field;

public class WSSQLServiceImpl implements WSSQLService{
	  private static final Logger LOGGER =LoggerFactory
	            .getLogger(WSSQLServiceImpl.class);
    private static final String SELECT = "select ";
    private static final String WHERE = "where";
    private static final String WHERE_WITH_SPACES = " where ";
    
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    
    private static final String UNAUTHORIZED = "Unauthorized";  

    private static final String DFU_ENDPOINT = "/WsDfu?ver_=1.2";
	
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
		
		endpoint.append("https://216.105.19.2:18009"); 
		endpoint.append("/ws_sql?ver_=1");

		locator.setWs_sqlServiceSoapAddress(endpoint.toString());
		locator.setWs_sqlServiceSoap_userName(hpccConnection.getUserName());
		locator.setWs_sqlServiceSoap_password(hpccConnection.getPwd());

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

    private static ChartdataJSON parseChartdataResponse(List<String> columns, String responseXML){
        //TODO Implemented by Senthil
        return null;
    };
    
    @Override
    public List<String> getDistinctValues(Field field, HPCCConnection connection, String fileName, List<Filter> filters) throws Exception  {
    	 List<String> filterDataList = new ArrayList<String>();
         try {
             final StringBuilder queryTxt = new StringBuilder(SELECT);
             queryTxt.append(fileName);
             queryTxt.append(".");
             queryTxt.append(field);
             queryTxt.append(" from ");
             queryTxt.append(fileName);

             if (!filters.isEmpty()) {            	 
                queryTxt.append(constructWhereClause(filters ,fileName));
             }

             queryTxt.append(" group by ");
             queryTxt.append(fileName);
             queryTxt.append(".");
             queryTxt.append(field);

             if (LOGGER.isDebugEnabled()) {
            	 LOGGER.debug("Query for Distinct values -> " + queryTxt.toString());
             }

             final String resultString = executeSQL(connection, queryTxt.toString());
             if (resultString != null && resultString.length() > 0) {
            	  // TO DO implement parser
            	 
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
         }
         return filterDataList;
    }

    @Override
    public Map<String, Number> getMinMax(Field field, HPCCConnection connection, String fileName, List<Filter> filters) throws Exception {
    	Map<String, Number> resultMap = new HashMap<String, Number>();

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
            	  
                 // TO DO implement parser by SENTHIL
            	  
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
    public ChartdataJSON getChartdata(Widget widget, HPCCConnection connection) throws  Exception{
    	
            ChartdataJSON dataObj = null;
            final String queryTxt = widget.generateSQL();

            if (LOGGER.isDebugEnabled()) {
            	LOGGER.debug("WS_SQL Query ->" + queryTxt);
            }

            final String resultString = executeSQL(connection, queryTxt);
            return   parseChartdataResponse(widget.getColumns(), resultString);
    }
   
    private String constructWhereClause(List<Filter> filters, String fileName) {
        StringBuilder queryTxt = new StringBuilder();
        queryTxt.append(WHERE_WITH_SPACES);

            Iterator<Filter> iterator = filters.iterator();
            while (iterator.hasNext()) {
                Filter filter = iterator.next();

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

                    for (int i = 1; i <= sfilter.getValues().size(); i++) {

                        queryTxt.append(" '").append(sfilter.getValues().get(i - 1)).append("'");
                        queryTxt.append(",");
                    }
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
        
    
        return queryTxt.toString();
    }
}
