package org.hpccsystems.dashboard.services.impl;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.entity.HpccConnection;
import org.hpccsystems.dashboard.chart.entity.Join;
import org.hpccsystems.dashboard.chart.entity.Measure;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.entity.XYModel;
import org.hpccsystems.dashboard.chart.tree.entity.Level;
import org.hpccsystems.dashboard.chart.tree.entity.LevelElement;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.chart.tree.entity.TreeFilter;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.FileMeta;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.soap.HpccSoap;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zkoss.zkplus.spring.SpringUtil;

import ws_sql.ws.hpccsystems.ArrayOfEspException;
import ws_sql.ws.hpccsystems.ExecuteSQLRequest;
import ws_sql.ws.hpccsystems.Ws_sqlLocator;
import ws_sql.ws.hpccsystems.Ws_sqlServiceSoap;
import wsdfu.ws.hpccsystems.DFUFileViewRequest;
import wsdfu.ws.hpccsystems.DFUFileViewResponse;
import wsdfu.ws.hpccsystems.DFULogicalFile;
import wsdfu.ws.hpccsystems.DFUQueryRequest;
import wsdfu.ws.hpccsystems.DFUQueryResponse;
import wsdfu.ws.hpccsystems.WsDfuLocator;
import wsdfu.ws.hpccsystems.WsDfuServiceSoap;
import wsworkunits.ws.hpccsystems.QuerySetAlias;
import wsworkunits.ws.hpccsystems.WUQuerySetDetailsResponse;
import wsworkunits.ws.hpccsystems.WUQuerysetDetails;
import wsworkunits.ws.hpccsystems.WsWorkunitsLocator;
import wsworkunits.ws.hpccsystems.WsWorkunitsServiceSoap;

public class HPCCServiceImpl implements HPCCService {

    private static final Log LOG = LogFactory.getLog(HPCCServiceImpl.class);
    
    private HPCCQueryService hpccQueryService;
    
    @Autowired
    public void setHpccQueryService(HPCCQueryService hpccQueryService) {
		this.hpccQueryService = hpccQueryService;
	}

	private static final String SELECT = "select ";
    private static final String WHERE = "where";
    private static final String WHERE_WITH_SPACES = " where ";
    
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    
    private static final String UNAUTHORIZED = "Unauthorized";  

    private static final String DFU_ENDPOINT = "/WsDfu?ver_=1.2";

    private String executeSQL(HpccConnection hpccConnection, String sql) throws ServiceException, ArrayOfEspException,
            RemoteException {
        
        String resultString = null;
        Ws_sqlLocator locator = new Ws_sqlLocator();
        StringBuilder endpoint = new StringBuilder();
        if (hpccConnection.getIsSSL()) {
            endpoint.append(HTTPS);
        } else {
            endpoint.append(HTTP);
        }
        endpoint.append(hpccConnection.getHostIp());
        endpoint.append(":");
        endpoint.append(hpccConnection.getWssqlPort());
        endpoint.append("/ws_sql?ver_=1");

        locator.setWs_sqlServiceSoapAddress(endpoint.toString());
        locator.setWs_sqlServiceSoap_userName(hpccConnection.getUsername());
        locator.setWs_sqlServiceSoap_password(hpccConnection.getPassword());

        ExecuteSQLRequest req = new ExecuteSQLRequest();
        req.setSqlText(sql);
        req.setTargetCluster("thor");
        req.setIncludeResults(true);

        Ws_sqlServiceSoap soap = locator.getws_sqlServiceSoap();
        resultString = soap.executeSQL(req).getResult();

        if (resultString != null && resultString.length() > 0) {
            resultString = StringUtils.substringBetween(resultString, "<Dataset", "</Dataset>");
            resultString = "<Dataset" + resultString + "</Dataset>";
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Result String: Afetr removing schema tag " + resultString);
        }

        return resultString;

    }

    public Set<Field> getColumns(final String sql, final HpccConnection hpccConnection)
            throws HpccConnectionException, ParserConfigurationException, SAXException, IOException {
        Set<Field> fields = new LinkedHashSet<Field>();
        
        HpccSoap hpccSoap = new HpccSoap(hpccConnection);
        String resultXML = hpccSoap.getColumnSchema(sql);
        
        try {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            final InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(resultXML));
            final Document doc = db.parse(inStream);
            
            doc.getDocumentElement().normalize();
            
            NodeList nodes = doc.getElementsByTagName("Field");
            Node node;
            Field field;
            for (int i = 0; i < nodes.getLength(); i++) {
                node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    field = new Field(
                            node.getAttributes().getNamedItem("name").getTextContent(), 
                            node.getAttributes().getNamedItem("ecltype").getTextContent());
                    fields.add(field);
                }
            }
            
        } catch (ParserConfigurationException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        } catch (SAXException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        } catch (IOException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        }
        
        return fields;
    }

    /**
     * getChartData() is used the retrieve the ChartData details and render the
     * D3 charts.
     * 
     * @param chartParamsMap
     * @return List<BarChart>
     * @throws ParserConfigurationException
     *             ,HpccConnectionException
     * @throws SAXException
     * @throws IOException
     *             , ServiceException
     * 
     */
    public List<XYModel> getChartData(XYChartData chartData) throws HpccConnectionException,
            ParserConfigurationException, SAXException, IOException, ServiceException, XPathExpressionException {
        //To handle roxie queries
        if(chartData.getIsQuery()) {
            return hpccQueryService.getChartData(chartData);
        }
        
        final List<XYModel> dataList = new ArrayList<XYModel>();
        XYModel dataObj = null;
        try {
            final String queryTxt = constructQuery(chartData);

            if (LOG.isDebugEnabled()) {
                LOG.debug("WS_SQL Query ->" + queryTxt);
            }

            final String resultString = executeSQL(chartData.getHpccConnection(), queryTxt);
            if (resultString != null && resultString.length() > 0) {

                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                final DocumentBuilder db = dbf.newDocumentBuilder();
                final InputSource inStream = new InputSource();
                inStream.setCharacterStream(new StringReader(resultString));
                final Document doc = db.parse(inStream);
                
                XPath xPath = XPathFactory.newInstance().newXPath();
                
                //Retrieve Attribute
                XPathExpression expression = xPath.compile("/Dataset/Row/" + chartData.getAttribute().getColumn());
                NodeList nodes = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
                List<Object> valueList;
                for (int i = 0; i < nodes.getLength(); i++) {
                    dataObj = new XYModel();
                    valueList = new ArrayList<Object>();
                    valueList.add(String.valueOf(nodes.item(i).getTextContent()));
                    dataObj.setxAxisValues(valueList);
                    dataList.add(dataObj);
                }
                
                //Retrieve Group attribute
                if (chartData.isGrouped()) {
                    expression = xPath.compile("/Dataset/Row/" + chartData.getGroupAttribute().getColumn());
                    nodes = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
                    for (int i = 0; i < nodes.getLength(); i++) {
                        dataObj = dataList.get(i);
                        dataObj.getxAxisValues().add(String.valueOf(nodes.item(i).getTextContent()));
                    }
                }
                
                //Measures
                int outCount = chartData.isGrouped() ? 3 : 2;
                for (Measure measure : chartData.getMeasures()) {
                    expression = xPath.compile("/Dataset/Row/" + 
                                (Constants.NONE.equals(measure.getAggregateFunction())?
                                        measure.getColumn():
                                            measure.getAggregateFunction() + "out" + outCount));
                    nodes = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
                    for (int i = 0; i < nodes.getLength(); i++) {
                        dataObj = dataList.get(i);
                        if(dataObj.getyAxisValues() != null) {
                            dataObj.getyAxisValues().add(new BigDecimal(nodes.item(i).getTextContent()));
                        } else {
                            valueList = new ArrayList<Object>();
                            valueList.add(new BigDecimal(nodes.item(i).getTextContent()));
                            dataObj.setyAxisValues(valueList);
                        }
                    }
                    
                    outCount++;
                }
            } else {
                throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
            }
        } catch (ServiceException | XPathExpressionException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        } catch (RemoteException e) {
            LOG.error(Constants.EXCEPTION, e);

            if (e.getMessage().contains(UNAUTHORIZED)) {
                throw new HpccConnectionException("401 Unauthorized");
            }

            throw e;
        }
        return dataList;
    }

    @Override
    public List<String> getDistinctValues(String fieldName, String dataSetName, ChartData chartData, Boolean applyFilter)
            throws HpccConnectionException, RemoteException {
        List<String> filterDataList = new ArrayList<String>();
        try {
            final StringBuilder queryTxt = new StringBuilder(SELECT);
            queryTxt.append(dataSetName);
            queryTxt.append(".");
            queryTxt.append(fieldName);
            queryTxt.append(" from ");

            if (applyFilter) {

                if (chartData.getIsFiltered()
                        && DashboardUtil.checkForMultipleDatasetFilter(chartData.getFilters(), dataSetName)) {

                    for (String fileName : chartData.getFiles()) {
                        queryTxt.append(fileName);
                        queryTxt.append(",");
                    }
                    queryTxt.deleteCharAt(queryTxt.length() - 1);
                    queryTxt.append(WHERE_WITH_SPACES);
                    
                    for (Join join : chartData.getJoins()) {
                        queryTxt.append(join.getSql());
                        queryTxt.append(" and ");
                    }
                    StringBuilder whereClause = new StringBuilder(constructWhereClause(chartData));
                    queryTxt.append(whereClause.replace(whereClause.indexOf(WHERE), whereClause.indexOf(WHERE) + 6,
                            ""));
                } else {
                    queryTxt.append(dataSetName);
                    queryTxt.append(constructWhereClause(chartData));
                }
            } else {
                queryTxt.append(dataSetName);
            }

            queryTxt.append(" group by ");
            queryTxt.append(dataSetName);
            queryTxt.append(".");
            queryTxt.append(fieldName);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Query for Distinct values -> " + queryTxt.toString());
            }

            final String resultString = executeSQL(chartData.getHpccConnection(), queryTxt.toString());
            if (resultString != null && resultString.length() > 0) {
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                final DocumentBuilder db = dbf.newDocumentBuilder();
                final InputSource inStream = new InputSource();
                inStream.setCharacterStream(new StringReader(resultString));
                final Document doc = db.parse(inStream);

                doc.getDocumentElement().normalize();

                final NodeList nodeList = doc.getElementsByTagName("Row");

                Node nodeItem;
                Element element;
                for (int count = 0; count < nodeList.getLength(); count++) {
                    nodeItem = nodeList.item(count);
                    if (nodeItem.getNodeType() == Node.ELEMENT_NODE) {
                        element = (Element) nodeItem;
                        filterDataList.add(element.getElementsByTagName(fieldName).item(0).getTextContent());
                    }

                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("filterDataList -->" + filterDataList);
                }
            } else {
                throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
            }
        } catch (RemoteException e) {
            if (e.getMessage().contains(UNAUTHORIZED)) {
                throw new HpccConnectionException("401 Unauthorized");
            }
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        } catch (ServiceException | ParserConfigurationException | SAXException | IOException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw new HpccConnectionException(ex.getMessage());
        }
        return filterDataList;
    }

    @Override
    public Map<Integer, BigDecimal> getMinMax(String fieldName, String dataSetName, ChartData chartData,
            Boolean applyFilter) throws HpccConnectionException, RemoteException {
        Map<Integer, BigDecimal> resultMap = new HashMap<Integer, BigDecimal>();

        try {
            // It is required to specify minimum value first in the query as
            // result XML element names are dependent on the order
            final StringBuilder queryTxt = new StringBuilder("select min(").append(dataSetName).append(".")
                    .append(fieldName).append("), max(").append(dataSetName).append(".").append(fieldName)
                    .append(") from ");

            if (applyFilter) {

                if (chartData.getIsFiltered()
                        && DashboardUtil.checkForMultipleDatasetFilter(chartData.getFilters(), dataSetName)) {

                    for (String fileName : chartData.getFiles()) {
                        queryTxt.append(fileName);
                        queryTxt.append(",");
                    }
                    queryTxt.deleteCharAt(queryTxt.length() - 1);
                    queryTxt.append(WHERE_WITH_SPACES);
                    for (Join join : chartData.getJoins()) {
                        queryTxt.append(join.getSql());
                        queryTxt.append(" and ");
                    }
                    StringBuilder whereClause = new StringBuilder(constructWhereClause(chartData));
                    queryTxt.append(whereClause.replace(whereClause.indexOf(WHERE), whereClause.indexOf(WHERE) + 6,
                            ""));
                } else {
                    queryTxt.append(dataSetName);
                    queryTxt.append(constructWhereClause(chartData));
                }
            } else {
                queryTxt.append(dataSetName);
            }

            final String resultString = executeSQL(chartData.getHpccConnection(), queryTxt.toString());

            if (LOG.isDebugEnabled()) {
                LOG.debug("queryTxt in fetchFilterMinMax() -->" + queryTxt);
            }
            if (resultString != null && resultString.length() > 0) {
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                final DocumentBuilder db = dbf.newDocumentBuilder();
                final InputSource inStream = new InputSource();
                inStream.setCharacterStream(new StringReader(resultString));
                final Document doc = db.parse(inStream);

                doc.getDocumentElement().normalize();

                final NodeList nodeList = doc.getElementsByTagName("Row");

                resultMap.put(Constants.FILTER_MINIMUM, new BigDecimal(nodeList.item(0).getChildNodes().item(0)
                        .getTextContent()));

                resultMap.put(Constants.FILTER_MAXIMUM, new BigDecimal(nodeList.item(0).getChildNodes().item(1)
                        .getTextContent()));
            } else {
                throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
            }

        } catch (RemoteException e) {
            if (e.getMessage().contains(UNAUTHORIZED)) {
                throw new HpccConnectionException("401 Unauthorized");
            }
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        } catch (ServiceException | ParserConfigurationException | SAXException | IOException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw new HpccConnectionException(ex.getMessage());
        }
        return resultMap;
    }

    /**
     * Constructs a where clause only when ChartData is Filtered
     * 
     * @param chartData
     * @return
     */
    private String constructWhereClause(ChartData chartData) {
        StringBuilder queryTxt = new StringBuilder();

        if (chartData.getIsFiltered()) {
            queryTxt.append(WHERE_WITH_SPACES);

            Iterator<Filter> iterator = chartData.getFilters().iterator();
            while (iterator.hasNext()) {
                Filter filter = iterator.next();

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Contructing where clause " + filter.toString());
                }

                queryTxt.append("(");

                if (Constants.DATA_TYPE_STRING.equals(filter.getType())) {
                    queryTxt.append(filter.getFileName());
                    queryTxt.append(".");
                    queryTxt.append(filter.getColumn());
                    queryTxt.append(" in ");
                    queryTxt.append(" (");

                    for (int i = 1; i <= filter.getValues().size(); i++) {

                        queryTxt.append(" '").append(filter.getValues().get(i - 1)).append("'");

                        if (i < filter.getValues().size()) {
                            queryTxt.append(",");
                        }
                    }

                    queryTxt.append(" )");
                } else if (Constants.DATA_TYPE_NUMERIC.equals(filter.getType())) {
                    queryTxt.append(filter.getFileName());
                    queryTxt.append(".");
                    queryTxt.append(filter.getColumn());
                    queryTxt.append(" >= ");
                    queryTxt.append(filter.getStartValue().setScale(0, RoundingMode.DOWN));
                    queryTxt.append(" and ");
                    queryTxt.append(filter.getFileName());
                    queryTxt.append(".");
                    queryTxt.append(filter.getColumn());
                    queryTxt.append(" <= ");
                    queryTxt.append(filter.getEndValue().setScale(0, RoundingMode.CEILING));
                }else if(Constants.CURRENT_DATE_STRING.equals(filter.getType())){
                    SimpleDateFormat formatter = new SimpleDateFormat(filter.getCurrentDateFormat());
                    String stringDate = formatter.format(new Date());
                    queryTxt.append(filter.getFileName());
                    queryTxt.append(".");
                    queryTxt.append(filter.getColumn());
                    queryTxt.append(" = ").append("'")
                    .append(stringDate).append("'");                    
                } else if(Constants.CURRENT_DATE_NUMERIC.equals(filter.getType())){
                    SimpleDateFormat formatter = new SimpleDateFormat(filter.getCurrentDateFormat());
                    String stringDate = formatter.format(new Date());
                    queryTxt.append(filter.getFileName());
                    queryTxt.append(".");
                    queryTxt.append(filter.getColumn());
                    queryTxt.append(" = ")
                    .append(stringDate);                    
                }

                queryTxt.append(")");

                if (iterator.hasNext()) {
                    queryTxt.append(" AND ");
                }
            }
        }
        return queryTxt.toString();
    }

    /**
     * Method to generate query for Hpcc
     * 
     * @param chartData
     *            -bar,line,pie chart
     * @return StringBuilder
     * 
     */
    private String constructQuery(XYChartData chartData) {
        StringBuilder queryTxt = new StringBuilder(SELECT);
        boolean isJoinEnabled = false;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Building Query");
            }

            queryTxt.append(chartData.getAttribute().getFileName());
            queryTxt.append(".");
            queryTxt.append(chartData.getAttribute().getColumn());
            queryTxt.append(",");
            
            if (chartData.isGrouped()) {
                queryTxt.append(chartData.getGroupAttribute().getFileName());
                queryTxt.append(".");
                queryTxt.append(chartData.getGroupAttribute().getColumn());
                queryTxt.append(",");
            }

            for (Measure measure : chartData.getMeasures()) {
                // Logic assumes when a 'NONE' aggregate is present no other
                // aggregations are present
                if (Constants.NONE.equals(measure.getAggregateFunction())) {
                    queryTxt.append(measure.getFileName());
                    queryTxt.append(".");
                    queryTxt.append(measure.getColumn());
                    queryTxt.append(",");
                } else {
                    queryTxt.append(measure.getAggregateFunction());
                    queryTxt.append("(");
                    queryTxt.append(measure.getFileName());
                    queryTxt.append(".");
                    queryTxt.append(measure.getColumn());
                    queryTxt.append("),");
                }
            }
            // Deleting last comma
            queryTxt.deleteCharAt(queryTxt.length() - 1);
            queryTxt.append(" from ");

            for (String fileName : chartData.getFiles()) {
                queryTxt.append(fileName);
                queryTxt.append(",");
            }
            queryTxt.deleteCharAt(queryTxt.length() - 1);

            if (chartData.getJoins() != null && !chartData.getJoins().isEmpty()) {
                queryTxt.append(WHERE_WITH_SPACES);
                for (Join join : chartData.getJoins()) {
                    queryTxt.append(join.getSql());
                    queryTxt.append(" and ");
                }
                isJoinEnabled = true;
            }

            if (isJoinEnabled) {
                if (chartData.getIsFiltered()) {
                    StringBuilder whereClause = new StringBuilder(constructWhereClause(chartData));
                    queryTxt.append(whereClause.replace(whereClause.indexOf(WHERE), whereClause.indexOf(WHERE) + 6,
                            ""));
                } else {
                    queryTxt.replace(queryTxt.lastIndexOf("and"), queryTxt.lastIndexOf("and") + 3, "");
                }
            } else {
                queryTxt.append(constructWhereClause(chartData));
            }

            // Skipping group by when no aggregations are present
            if (!Constants.NONE.equals(chartData.getMeasures().iterator().next().getAggregateFunction())) {
                queryTxt.append(" group by ");

                queryTxt.append(chartData.getAttribute().getFileName());
                queryTxt.append(".");
                queryTxt.append(chartData.getAttribute().getColumn());
                
                if(chartData.isGrouped()){
                    queryTxt.append(",");
                    queryTxt.append(chartData.getGroupAttribute().getFileName());
                    queryTxt.append(".");
                    queryTxt.append(chartData.getGroupAttribute().getColumn());
                }
            } else {
                // skipping order by when group by clause already exists
                queryTxt.append(" order by ");

                queryTxt.append(chartData.getAttribute().getFileName());
                queryTxt.append(".");
                queryTxt.append(chartData.getAttribute().getColumn());
                
                if(chartData.isGrouped()){
                    queryTxt.append(",");
                    queryTxt.append(chartData.getGroupAttribute().getFileName());
                    queryTxt.append(".");
                    queryTxt.append(chartData.getGroupAttribute().getColumn());
                }
            }

        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION, e);
        }
        return queryTxt.toString();
    }

    /**
     * Method to generate query for Hpcc
     * 
     * @param TableData
     *            -table widget
     * @return StringBuilder
     * 
     */
    private String constructQuery(TableData tableData) {
        StringBuilder queryTxt = new StringBuilder(SELECT);
        boolean isJoinEnabled = false;
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Building Query for table widget");
                LOG.debug("isFiltered -> " + tableData.getIsFiltered());
            }

            for (Attribute attribute : tableData.getAttributes()) {
            	//Measure columns are added to query
            	if(attribute.getAggregateFunction() != null ){
            		// appending aggregate function other than 'NONE'
					if(!Constants.NONE.equals(attribute.getAggregateFunction())){
						queryTxt.append(attribute.getAggregateFunction());
	                    queryTxt.append("(");
	                    queryTxt.append(attribute.getFileName());
	                    queryTxt.append(".");
	                    queryTxt.append(attribute.getColumn());
	                    queryTxt.append("),");
					}else{
						queryTxt.append(attribute.getFileName());
						queryTxt.append(".");
						queryTxt.append(attribute.getColumn());
						queryTxt.append(",");
					}
				}else{
					//Attribute columns are added to query
					queryTxt.append(attribute.getFileName());
					queryTxt.append(".");
					queryTxt.append(attribute.getColumn());
					queryTxt.append(",");
				}
            	               
            }
            // Deleting last comma
            queryTxt.deleteCharAt(queryTxt.length() - 1);
            queryTxt.append(" from ");

            for (String fileName : tableData.getFiles()) {
                queryTxt.append(fileName);
                queryTxt.append(",");
            }
            queryTxt.deleteCharAt(queryTxt.length() - 1);

            if (tableData.getJoins() != null && !tableData.getJoins().isEmpty()) {
                queryTxt.append(WHERE_WITH_SPACES);
                for (Join join : tableData.getJoins()) {
                    queryTxt.append(join.getSql());
                    queryTxt.append(" and ");
                }
                isJoinEnabled = true;
            }
            if (isJoinEnabled) {
                if (tableData.getIsFiltered()) {
                    StringBuilder whereClause = new StringBuilder(constructWhereClause(tableData));
                    queryTxt.append(whereClause.replace(whereClause.indexOf(WHERE), whereClause.indexOf(WHERE) + 6,
                            ""));
                } else {
                    queryTxt.replace(queryTxt.lastIndexOf("and"), queryTxt.lastIndexOf("and") + 3, "");
                }
            } else {
                queryTxt.append(constructWhereClause(tableData));
            }

        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION, e);
        }
        return queryTxt.toString();
    }

    /**
     * fetchTableData() is used to retrieve the Column values from HPCC systems
     * to construct Table Widget.
     * 
     * @param data
     * @return
     * @throws HpccConnectionException
     * @throws RemoteException
     * @throws Exception
     */
    public Map<String, List<Attribute>> fetchTableData(TableData tableData) throws HpccConnectionException,
            RemoteException {
        //To handle roxie queries
        if(tableData.getIsQuery()) {
            HPCCQueryService hpccQueryService = (HPCCQueryService) SpringUtil.getBean("hpccQueryService");
            return hpccQueryService.fetchTableData(tableData);
        }
        
        Map<String, List<Attribute>> tableDataMap = new LinkedHashMap<String, List<Attribute>>();

        try {
            final String queryTxt = constructQuery(tableData);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Logical file : WS_SQL Query table widget->" + queryTxt);
            }

            final String resultString = executeSQL(tableData.getHpccConnection(), queryTxt);

            if (resultString != null && resultString.length() > 0) {
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                final DocumentBuilder db = dbf.newDocumentBuilder();
                final InputSource inStream = new InputSource();
                inStream.setCharacterStream(new StringReader(resultString));
                final Document doc = db.parse(inStream);
                Node fstNode = null;
                Element fstElmnt = null, lstNmElmnt = null;
                NodeList lstNmElmntLst = null;
                List<Attribute> columnListvalue = null;
                for (Attribute columnName : tableData.getAttributes()) {
                    columnListvalue = new ArrayList<Attribute>();
                    tableDataMap.put(columnName.getColumn(), columnListvalue);
                }
                final NodeList nodeList = doc.getElementsByTagName("Row");
                if (nodeList != null) {
                    String str;
                    for (int count = 0; count < nodeList.getLength(); count++) {
                        fstNode = nodeList.item(count);
                        if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                            fstElmnt = (Element) fstNode;
                            for (Attribute data : tableData.getAttributes()) {
                                lstNmElmntLst = fstElmnt.getElementsByTagName(data.getColumn());
                                lstNmElmnt = (Element) lstNmElmntLst.item(0);
                                if(lstNmElmnt != null){
	                                // Rounding off Numeric values
	                                if (DashboardUtil.checkRealValue(tableData
	                                        .getFields()
	                                        .get(data.getFileName())
	                                        .get(tableData.getFields().get(data.getFileName())
	                                                .indexOf(new Field(data.getColumn(), null))).getDataType())) {
	                                    str = new BigDecimal(lstNmElmnt.getTextContent()).setScale(2,
	                                            RoundingMode.HALF_EVEN).toPlainString();
	                                } else {
	                                    str = lstNmElmnt.getTextContent();
	                                }
	                                                              
                                }else{
                                	str="";
                                }
                                columnListvalue = tableDataMap.get(data.getColumn());	
                                columnListvalue.add(new Attribute(str));
                            }
                        }
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("tableDataMap -->" + tableDataMap);
                }
            } else {
                throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
            }
        } catch (RemoteException e) {
            if (e.getMessage().contains(UNAUTHORIZED)) {
                throw new HpccConnectionException("401 Unauthorized");
            }
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        } catch (ServiceException | ParserConfigurationException | SAXException | IOException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw new HpccConnectionException(ex.getMessage());
        }
        return tableDataMap;
    }

    public List<FileMeta> getFileList(String scope, HpccConnection hpccConnection) throws ServiceException,
            RemoteException {

        List<FileMeta> results = new ArrayList<FileMeta>();
        try {
            WsDfuLocator locator = new WsDfuLocator();
            locator.setWsDfuServiceSoap_userName(hpccConnection.getUsername());
            locator.setWsDfuServiceSoap_password(hpccConnection.getPassword());

            StringBuilder soapAddress = new StringBuilder();
            soapAddress.append(HTTPS).append(hpccConnection.getHostIp()).append(Constants.COLON)
                    .append(hpccConnection.getEspPort()).append(DFU_ENDPOINT);

            if (hpccConnection.getIsSSL()) {
                locator.setWsDfuServiceSoapAddress(soapAddress.toString());
            } else {
                soapAddress.replace(0, 5, "http");
                locator.setWsDfuServiceSoapAddress(soapAddress.toString());
            }
            WsDfuServiceSoap soap = locator.getWsDfuServiceSoap();
            DFUFileViewRequest req = new DFUFileViewRequest();
            req.setScope(scope);
            DFUFileViewResponse result = soap.DFUFileView(req);
            DFULogicalFile[] resultsArray = result.getDFULogicalFiles();
            
            if(LOG.isDebugEnabled()) {
                LOG.debug("Web Service Results -->" + result);
                LOG.debug("Total Number of Files: " + resultsArray.length);
            }
            
            FileMeta fileMeta;
            for (int i = 0; i < resultsArray.length; i++) {
                fileMeta = new FileMeta();
                if (resultsArray[i].getIsDirectory()) {
                    fileMeta.setIsDirectory(true);
                    fileMeta.setFileName(resultsArray[i].getDirectory());
                    if (scope.length() > 0) {
                        fileMeta.setScope(scope + "::" + resultsArray[i].getDirectory());
                    } else {
                        fileMeta.setScope(resultsArray[i].getDirectory());
                    }
                } else {
                    fileMeta.setIsDirectory(false);
                    fileMeta.setFileName(resultsArray[i].getName());
                }
                results.add(fileMeta);
            }
        } catch (ServiceException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        } catch (RemoteException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        }
        return results;
    }

    public List<FileMeta> getRoxieFileList(HpccConnection hpccConnection) throws HpccConnectionException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("HpccConnection in getRoxieFileList() -->" + hpccConnection);
        }

        WsDfuLocator locator = new WsDfuLocator();
        StringBuilder endpoint = new StringBuilder();
        if (hpccConnection.getIsSSL()) {
            endpoint.append(HTTPS);
        } else {
            endpoint.append(HTTP);
        }
        endpoint.append(hpccConnection.getHostIp());
        endpoint.append(":");
        endpoint.append(hpccConnection.getEspPort());
        endpoint.append(DFU_ENDPOINT);

        locator.setWsDfuServiceSoapAddress(endpoint.toString());
        locator.setWsDfuServiceSoap_userName(hpccConnection.getUsername());
        locator.setWsDfuServiceSoap_password(hpccConnection.getPassword());

        DFUQueryRequest req = new DFUQueryRequest();
        //req.setClusterName(hpccConnection.getClusterName());
        req.setFileType("Logical Files Only");
        DFUQueryResponse result;
        try {

            WsDfuServiceSoap soap = locator.getWsDfuServiceSoap();
            result = soap.DFUQuery(req);
        } catch (RemoteException | ServiceException e) {
            LOG.error("ERROR", e);
            throw new HpccConnectionException(e.getMessage());
        }

        List<FileMeta> results = new ArrayList<FileMeta>();
        DFULogicalFile[] resultsArray = result.getDFULogicalFiles();
        FileMeta fileMeta;
        for (DFULogicalFile dfuLogicalFile : resultsArray) {
            fileMeta = new FileMeta();
            fileMeta.setFileName(dfuLogicalFile.getName());
            fileMeta.setIsDirectory(false);
            results.add(fileMeta);
        }

        return results;
    }

    public List<FileMeta> getQueries(HpccConnection hpccConnection,int category) throws HpccConnectionException {

        WsWorkunitsLocator locator = new WsWorkunitsLocator();

        StringBuilder url = new StringBuilder();
        if (hpccConnection.getIsSSL()) {
            url.append(HTTPS);
        } else {
            url.append(HTTP);
        }
        url.append(hpccConnection.getHostIp());
        url.append(":");
        url.append(hpccConnection.getEspPort());
        url.append("/WsWorkunits?ver_=1.5");

        locator.setWsWorkunitsServiceSoap_address(url.toString());
        locator.setWsWorkunitsServiceSoap_userName(hpccConnection.getUsername());
        locator.setWsWorkunitsServiceSoap_password(hpccConnection.getPassword());

        try {
            WsWorkunitsServiceSoap soap;
            soap = locator.getWsWorkunitsServiceSoap();
            WUQuerysetDetails req = new WUQuerysetDetails();
            req.setQuerySetName(hpccConnection.getClusterType());
            req.setClusterName(hpccConnection.getClusterType());

            WUQuerySetDetailsResponse result = soap.WUQuerysetDetails(req);
            QuerySetAlias[] resultsArray = result.getQuerysetAliases();

            List<FileMeta> results = new ArrayList<FileMeta>();
            FileMeta fileMeta;
            //For Scored-Search-table need queries starts with 'score'
            if(Constants.CATEGORY_ADVANCED_TABLE == category){
            	for (QuerySetAlias querySetAlias : resultsArray) {  
            		if(querySetAlias.getName().startsWith("score")){
	                    fileMeta = new FileMeta();
	                    fileMeta.setFileName(querySetAlias.getName());
	                    fileMeta.setIsDirectory(false);
	                    results.add(fileMeta);
            		}
                }            	
            }else{
            	for (QuerySetAlias querySetAlias : resultsArray) {            	
                    fileMeta = new FileMeta();
                    fileMeta.setFileName(querySetAlias.getName());
                    fileMeta.setIsDirectory(false);
                    results.add(fileMeta);
                }
            	
            }
            

            return results;
        } catch (RemoteException | ServiceException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw new HpccConnectionException(e.getMessage());
        }

    }
    
    @Override
    public List<List<String>> getRootValues(TreeData treeData, Level level, List<TreeFilter> treeFilters)
            throws HpccConnectionException, RemoteException {

        final List<List<String>> resultList = new ArrayList<List<String>>();
        if(treeData.getIsQuery()){
        	if(hpccQueryService == null){
        		hpccQueryService = new HPCCQueryServiceImpl();
        	}
        	return hpccQueryService.getRootValues(treeData,level,treeFilters);        	
        }else{

	        try {
	            final String comma = ", ";
	
	            final StringBuilder queryTxt = new StringBuilder(SELECT);
	
	            Set<String> files = new HashSet<String>();
	
	            Iterator<LevelElement> iterator = level.getElements().iterator();
	            while (iterator.hasNext()) {
	                LevelElement element = (LevelElement) iterator.next();
	                if (element.getIsColumn()) {
	                    files.add(element.getFileName());
	
	                    queryTxt.append(element.getFileName());
	                    queryTxt.append(".");
	                    queryTxt.append(element.getName());
	                } else {
	                    if (queryTxt.lastIndexOf(comma) == (queryTxt.length() - 2)) {
	                        queryTxt.delete(queryTxt.lastIndexOf(comma), queryTxt.length() - 1);
	                    }
	                }
	                if (iterator.hasNext()) {
	                    queryTxt.append(comma);
	                }
	            }
	
	            queryTxt.append(" from ");
	
	            StringBuilder whereClause = new StringBuilder(constructWhereClause(treeData));
	
	            if (treeData.getFiles().size() > 1) {
	                Iterator<String> stringIterator = treeData.getFiles().iterator();
	                while (stringIterator.hasNext()) {
	                    queryTxt.append((String) stringIterator.next());
	
	                    if (stringIterator.hasNext()) {
	                        queryTxt.append(",");
	                    }
	                }
	                queryTxt.append(WHERE_WITH_SPACES);
	                Iterator<Join> joinIterator = treeData.getJoins().iterator();
	                while (joinIterator.hasNext()) {
	                    Join join = (Join) joinIterator.next();
	                    queryTxt.append(join.getSql());
	
	                    if (joinIterator.hasNext()) {
	                        queryTxt.append(" and ");
	                    }
	                }
	
	                if (whereClause.length() > 6) {
	                    queryTxt.append(whereClause.replace(whereClause.indexOf(WHERE), whereClause.indexOf(WHERE) + 6,
	                            ""));
	                }
	            } else {
	                // Assuming only one file exists to query
	                queryTxt.append(files.iterator().next());
	                queryTxt.append(whereClause);
	            }
	            // Apply Filters passed in argument
	            if (treeFilters != null) {
	                if (queryTxt.indexOf(WHERE) > -1) {
	                    queryTxt.append(" and ");
	                } else {
	                    queryTxt.append(WHERE_WITH_SPACES);
	                }
	
	                Iterator<TreeFilter> filterIterator = treeFilters.iterator();
	                while (filterIterator.hasNext()) {
	                    TreeFilter treeFilter = (TreeFilter) filterIterator.next();
	                    queryTxt.append(treeFilter.getFileName());
	                    queryTxt.append(".");
	                    queryTxt.append(treeFilter.getColumnName());
	                    queryTxt.append("=");
	                    if (Constants.DATA_TYPE_STRING.equals(treeFilter.getDataType())) {
	                        queryTxt.append("'");
	                    }
	                    queryTxt.append(treeFilter.getValue());
	                    if (Constants.DATA_TYPE_STRING.equals(treeFilter.getDataType())) {
	                        queryTxt.append("'");
	                    }
	
	                    if (filterIterator.hasNext()) {
	                        queryTxt.append(" and ");
	                    }
	                }
	            }
	
	            // Group by to induce distinct values
	            queryTxt.append(" group by ");
	            iterator = level.getElements().iterator();
	            while (iterator.hasNext()) {
	                LevelElement element = (LevelElement) iterator.next();
	                if (element.getIsColumn()) {
	                    files.add(element.getFileName());
	
	                    queryTxt.append(element.getFileName());
	                    queryTxt.append(".");
	                    queryTxt.append(element.getName());
	                } else {
	                    if (queryTxt.lastIndexOf(comma) == (queryTxt.length() - 2)) {
	                        queryTxt.delete(queryTxt.lastIndexOf(comma), queryTxt.length() - 1);
	                    }
	                }
	                if (iterator.hasNext()) {
	                    queryTxt.append(comma);
	                }
	            }
	
	            if (LOG.isDebugEnabled()) {
	                LOG.debug("Query for Root values -> " + queryTxt.toString());
	            }
	
	            final String resultString = executeSQL(treeData.getHpccConnection(), queryTxt.toString());
	
	            if (LOG.isDebugEnabled()) {
	                LOG.debug("Result String length : " + resultString == null ? resultString : resultString.length());
	            }
	            if (resultString != null && resultString.length() > 0) {
	                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	                final DocumentBuilder db = dbf.newDocumentBuilder();
	                final InputSource inStream = new InputSource();
	                inStream.setCharacterStream(new StringReader(resultString));
	                final Document doc = db.parse(inStream);
	
	                doc.getDocumentElement().normalize();
	
	                final NodeList nodeList = doc.getElementsByTagName("Row");
	
	                Node nodeItem;
	                Element element;
	                List<String> values;
	                for (int count = 0; count < nodeList.getLength(); count++) {
	                    nodeItem = nodeList.item(count);
	                    if (nodeItem.getNodeType() == Node.ELEMENT_NODE) {
	                        element = (Element) nodeItem;
	                        values = new ArrayList<String>();
	                        for (LevelElement levelElement : level.getElements()) {
	                            if (levelElement.getIsColumn()) {
	                                values.add(element.getElementsByTagName(levelElement.getName()).item(0)
	                                        .getTextContent());
	                            }
	                        }
	                        resultList.add(values);
	                    }
	
	                }
	
	                if (LOG.isDebugEnabled()) {
	                    LOG.debug("filterDataList -->" + resultList);
	                }
	            } else {
	                throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
	            }
	        } catch (RemoteException e) {
            if (e.getMessage().contains(UNAUTHORIZED)) {
                throw new HpccConnectionException("401 Unauthorized");
            }
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        } catch (ServiceException | ParserConfigurationException | SAXException | IOException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw new HpccConnectionException(ex.getMessage());
        }
        }
        return resultList;
    }

    @Override
    public List<String> getClusters(HpccConnection hpccConnection) throws HpccConnectionException {
        HpccSoap hpccSoap = new HpccSoap(hpccConnection);
        return hpccSoap.getClusters();
    }
}
