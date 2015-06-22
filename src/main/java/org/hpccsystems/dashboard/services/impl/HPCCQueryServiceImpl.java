package org.hpccsystems.dashboard.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.AdvancedFilter;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.HpccConnection;
import org.hpccsystems.dashboard.chart.entity.InputParam;
import org.hpccsystems.dashboard.chart.entity.Measure;
import org.hpccsystems.dashboard.chart.entity.ScoredSearchData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.TitleColumn;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.entity.XYModel;
import org.hpccsystems.dashboard.chart.tree.entity.Level;
import org.hpccsystems.dashboard.chart.tree.entity.LevelElement;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.chart.tree.entity.TreeFilter;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.mysql.jdbc.StringUtils;

public class HPCCQueryServiceImpl implements HPCCQueryService {

    private static final Log LOG = LogFactory.getLog(HPCCQueryServiceImpl.class);
    
    
    private final String AUTHORIZATION= "Authorization";
    private final String BASIC= "Basic ";
    private final String INTEGER = "integer";
    
    private final String BOTH = "Both";
    private final String INPUT = "Input";
    private final String OUTPUT = "Output";
    private final String MEASURE_COLUMN ="measure_column";
    private final String ATTRIBUTE_COLUMN ="attribute_column";
    private final String GROUPBY_COLUMN ="groupby_column";
    private final String ROW_NO =".Row.0.";
    
        static {
        // To bypass SSL Handshake exception
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String string, SSLSession session) {
                return true;
            }
        });
    }

    
        @Override
        public Set<Field> getColumns(String queryName,
                HpccConnection hpccConnection, boolean isGenericQuery,
                String inputParamQuery) throws IOException, ParserConfigurationException, SAXException, URISyntaxException, HpccConnectionException {
            if(isGenericQuery){
                return getGenericQuerySchema(queryName,hpccConnection,inputParamQuery).getFields();
            }else{
                return getNongenericQueryColumns(hpccConnection,queryName);             
            }           
        }
        
    /**
     * Hits Hpcc to get output columns of Query
     * @param hpccConnection
     * @param queryName
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws URISyntaxException
     */
    private Set<Field> getNongenericQueryColumns(HpccConnection hpccConnection,
            String queryName) throws IOException, ParserConfigurationException,
            SAXException, URISyntaxException {
        
            Set<Field> fields = new LinkedHashSet<Field>();         
            StringBuilder urlBuilder = new StringBuilder();
            if (hpccConnection.getIsSSL()) {
                urlBuilder.append(Constants.HTTPS);
            } else {
                urlBuilder.append(Constants.HTTP);
            }
            urlBuilder.append(hpccConnection.getHostIp());
            urlBuilder.append(":");
            urlBuilder.append(hpccConnection.getWsEclPort());
            urlBuilder.append("/WsEcl/definitions/query/");
            urlBuilder.append(hpccConnection.getClusterType());
            urlBuilder.append("/");
            urlBuilder.append(queryName);
            urlBuilder.append("/main/");
            urlBuilder.append(queryName);
            urlBuilder.append(".xsd");

            if (LOG.isDebugEnabled()) {
                LOG.debug("URL -> " + urlBuilder.toString());
            }
            
            
            URL url = new URL(urlBuilder.toString());
            URLConnection urlConnection = url.openConnection();
            String authString = hpccConnection.getUsername() + ":" + hpccConnection.getPassword();
            String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
            urlConnection.setRequestProperty(AUTHORIZATION, BASIC + authStringEnc);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(urlConnection.getInputStream());
            doc.getDocumentElement().normalize();

            NamedNodeMap importAttrs = doc.getElementsByTagName("xsd:import").item(0).getAttributes();
            String schmaLocation = importAttrs.getNamedItem("schemaLocation").getTextContent();

            URI uri = new URI(urlBuilder.toString());
            url = uri.resolve(schmaLocation).toURL();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Schema URL -> " + url);
            }

            urlConnection = url.openConnection();
            urlConnection.setRequestProperty(AUTHORIZATION, BASIC + authStringEnc);

            doc = dBuilder.parse(urlConnection.getInputStream());
            doc.getDocumentElement().normalize();

            NodeList fieldList = doc.getElementsByTagName("xs:element").item(1).getChildNodes().item(1).getChildNodes()
                    .item(1).getChildNodes();
            parseResultString(fields,fieldList);
            
            return fields;
        }
    
    @Override
    public QuerySchema getQuerySchema(String queryName, HpccConnection hpccConnection,boolean isGenericQuery,String inputParamQuery) throws Exception {
        Set<Field> fields = new LinkedHashSet<Field>();
        QuerySchema queryData = null;
        try {
            if(isGenericQuery){
                queryData = getGenericQuerySchema(queryName,hpccConnection,inputParamQuery);
            }else{
                queryData =  new QuerySchema();
                fields =  getNongenericQueryColumns(hpccConnection,queryName); 
                queryData.setFields(fields);
                Set<String> inputParams = getInputParameters(queryName,hpccConnection,isGenericQuery,inputParamQuery);
                queryData.setInputParams(getInputParamDistinctValues(queryName,
                        inputParams, hpccConnection, isGenericQuery,
                        inputParamQuery));              
                }

        } catch (DOMException | SAXException | IOException | ParserConfigurationException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("queryData" + queryData);
        }

        return queryData;
    }
    
    
    private QuerySchema getGenericQuerySchema(String queryName, HpccConnection hpccConnection,String inputParamQuery) throws HpccConnectionException {
        QuerySchema querySchema = new QuerySchema();
        Set<Field> fields = new LinkedHashSet<Field>();
        Map<String,Set<String>> inputParams = new LinkedHashMap<String, Set<String>>();
        querySchema.setFields(fields);
        querySchema.setInputParams(inputParams);
        
        try {
            StringBuilder urlBuilder = new StringBuilder();
            
            if (hpccConnection.getIsSSL()) {
                urlBuilder.append(Constants.HTTPS);
            } else {
                urlBuilder.append(Constants.HTTP);
            }
            urlBuilder.append(hpccConnection.getHostIp())
                    .append(":")
                    .append(hpccConnection.getWsEclPort())
                    .append("/WsEcl/submit/query/")
                    .append(hpccConnection.getClusterType())
                    .append("/")
                    .append(inputParamQuery)
                    .append("/xml?")
                    .append("queryname=")
                    .append(queryName);
            
            URL url = new URL(urlBuilder.toString());
            URLConnection urlConnection = url.openConnection();
            String authString = hpccConnection.getUsername() + ":"
                    + hpccConnection.getPassword();
            String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
            urlConnection.setRequestProperty(AUTHORIZATION, BASIC + authStringEnc);

            if (LOG.isDebugEnabled()) {
                LOG.debug("URL ->" + url);
            }
            
            final InputStream respone = urlConnection.getInputStream();
            if(respone != null) {
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                final DocumentBuilder db = dbf.newDocumentBuilder();
                final Document doc = db.parse(respone);
                XPathFactory xPathFactory = XPathFactory.newInstance();
                XPath xPath = xPathFactory.newXPath();
                
                NodeList rows = (NodeList) xPath.evaluate("/" + inputParamQuery + "Response/Result/Dataset/Row", doc, XPathConstants.NODESET);
                
                
                String param_type;
                String field_name;
                String field_type;
                for (int i = 0; i < rows.getLength(); i++) {
                    Node row = rows.item(i); 
                    
                    field_name = ((Node) xPath.evaluate("field_name", row, XPathConstants.NODE)).getTextContent();
                    
                    param_type =  ((Node) xPath.evaluate("parameter_type", row, XPathConstants.NODE)).getTextContent();
                    switch (param_type) {
                    case INPUT:
                        inputParams.put(field_name, extractValues(xPath,row));
                        break;
                        
                    case OUTPUT:
                        field_type= ((Node) xPath.evaluate("field_type", row, XPathConstants.NODE)).getTextContent().trim();
                        fields.add(new Field(field_name, 
                                ("Measure".equals(field_type))?"unsigned":field_type));
                        break;
                        
                    case BOTH:
                        inputParams.put(field_name, extractValues(xPath,row));
                        field_type = ((Node) xPath.evaluate("field_type", row, XPathConstants.NODE)).getTextContent().trim();
                        fields.add(new Field(field_name, 
                                ("Measure".equals(field_type))?"unsigned":field_type));
                        break;

                    default:
                        break;
                    }
                }
            }            
            
        } catch (NumberFormatException e) {
            throw e;
        } catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
            LOG.error(Constants.EXCEPTION, e);
            if (e.getMessage().contains("Unauthorized")) {
                throw new HpccConnectionException("401 Unauthorized");
            } else {
                throw new HpccConnectionException(e.getMessage());
            }
        }
        LOG.debug("Generic querySchema ---->{}"+ querySchema);
        return querySchema;
    }
    
    private Set<String> extractValues(XPath xPath, Node row){
        Set<String> values = new LinkedHashSet<String>();
        try {
            NodeList valueNodes =  (NodeList) xPath.evaluate("field_value/Row/value", row, XPathConstants.NODESET);     
            for (int i = 0; i < valueNodes.getLength(); i++) {
                values.add(valueNodes.item(i).getTextContent());
            }
        } catch (XPathExpressionException e) {
            LOG.error(Constants.EXCEPTION,e);
        }
        return values;
    }


    /**Parses the result Xml string to get output columns of an query
     * @param fields
     * @param fieldList
     */
    private void parseResultString(Set<Field> fields,NodeList fieldList) {
        
        Set<String> xsdNumericTypes = new HashSet<String>();
        String[] array = { "byte", "decimal", "int", "integer", "long", "negativeInteger", "nonNegativeInteger",
                "nonPositiveInteger", "positiveInteger", "short", "unsignedLong", "unsignedInt", "unsignedShort",
                "unsignedByte" };
        xsdNumericTypes.addAll(Arrays.asList(array));
        
        String tagName;
        String type;
        Field field;
        Node typeNode;
        
        for (int i = 0; i < fieldList.getLength(); i++) {
            if (fieldList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                field = new Field();
                tagName = fieldList.item(i).getAttributes().getNamedItem("name").getTextContent();
                if("fpos".equalsIgnoreCase(tagName)) {
                    continue;
                }
                field.setColumnName(tagName);
                typeNode = fieldList.item(i).getAttributes().getNamedItem("type");
                if(typeNode != null) {
                    type = typeNode.getTextContent();
                    if (type.lastIndexOf(':') > 0
                            && xsdNumericTypes.contains(type.substring(type.lastIndexOf(':') + 1))) {
                        field.setDataType(INTEGER);
                    } else if (xsdNumericTypes.contains(type)) {
                        field.setDataType(INTEGER);
                    } else {
                        field.setDataType(type);
                    }
                } else {
                    field.setDataType(Constants.DATA_TYPE_DATASET_STRING);
                    
                    Set<Field> childFields = new LinkedHashSet<Field>();
                    
                    parseResultString(childFields, 
                    fieldList.item(i).getChildNodes().item(1).getChildNodes().item(1)
                        .getChildNodes().item(1).getChildNodes().item(1)
                        .getChildNodes().item(1).getChildNodes());
                    
                    field.setChildren(new ArrayList<Field>(childFields));
                }
                fields.add(field);
            }
        }       
    }

    @Override
    public List<XYModel> getChartData(XYChartData chartData,
            List<TitleColumn> titleColumns) throws HpccConnectionException,
            NumberFormatException, XPathExpressionException {
        List<XYModel> dataList = null;
        
       try {
           if(chartData.isGenericQuery()){
               return getGenericQueryData(chartData,titleColumns);
           }else{
               StringBuilder urlBuilder = new StringBuilder();
               if (chartData.getHpccConnection().getIsSSL()) {
                   urlBuilder.append(Constants.HTTPS);
               } else {
                   urlBuilder.append(Constants.HTTP);
               }
               urlBuilder.append(chartData.getHpccConnection().getHostIp())
                       .append(":")
                       .append(chartData.getHpccConnection().getWsEclPort())
                       .append("/WsEcl/submit/query/")
                       .append(chartData.getHpccConnection().getClusterType())
                       .append("/")
                       .append(chartData.getFiles().iterator().next())
                       .append("/xml?");


               //Input parameter may not have any value selected
               //or it has single value selected for a single input parameter               
                if (chartData.isGrouped() && chartData.getMeasures().get(0).getAggregateFunction() != null) {
                            Map<String,Map<String,List<Object>>> groupedData =  getGroupedChartData(urlBuilder,chartData,titleColumns);
                            if(!Constants.NONE.equals(chartData.getMeasures().get(0).getAggregateFunction())){
                                return withAggregateGroupedData(groupedData,chartData.getMeasures().get(0).getAggregateFunction());
                            }else{
                                return withOutAggregateGroupedData(groupedData);  
                            }
               }else{
                   dataList = getNonGenericQueryData(urlBuilder,chartData,titleColumns);
                   LOG.debug("dataList -->"+dataList);
                   return doAggregation(dataList, chartData);
               }               
           }
          
           
       } catch (NumberFormatException e) {
           throw e;
       } catch (SAXException | IOException | ParserConfigurationException e) {
           LOG.error(Constants.EXCEPTION, e);
           if (e.getMessage().contains("Unauthorized")) {
               throw new HpccConnectionException("401 Unauthorized");
           } else {
               throw new HpccConnectionException(e.getMessage());
           }
       }
      
    
   }    
    

    /**
     * Gets chart data for generic queries
     * @param chartData
     * @return List<XYModel>
     * @throws HpccConnectionException 
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws XPathExpressionException 
     */
    private List<XYModel> getGenericQueryData(XYChartData chartData,List<TitleColumn> titleColumns)
            throws HpccConnectionException, IOException, 
            ParserConfigurationException, SAXException, XPathExpressionException {
       
       String requestName = getGenericQueryRequestName(chartData);
       String urlStr = null;
       List<XYModel> dataList;
       
       if(requestName != null){
           urlStr = constructQueryURLWithReqName(requestName,chartData);
       }else{
           urlStr = constructQueryURLWithoutReqName(chartData);
       }
        
         URL url = new URL(urlStr);
         URLConnection urlConnection = url.openConnection();
         String authString = chartData.getHpccConnection().getUsername() + ":"
                 + chartData.getHpccConnection().getPassword();
         String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
         urlConnection.setRequestProperty(AUTHORIZATION, BASIC+ authStringEnc);

         final InputStream respone = urlConnection.getInputStream();        
         
        if (respone != null) {
            if (chartData.isGrouped() && chartData.getMeasures().get(0).getAggregateFunction() != null) {
                
                    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    final DocumentBuilder db = dbf.newDocumentBuilder();
                    final Document doc = db.parse(respone);

                    final NodeList nodeList = doc.getElementsByTagName("Row");
                    Map<String,Map<String,List<Object>>> groupedRowData  = groupData(nodeList,chartData,titleColumns);
                    if(!Constants.NONE.equals(chartData.getMeasures().get(0).getAggregateFunction())){
                        return withAggregateGroupedData(groupedRowData,chartData.getMeasures().get(0).getAggregateFunction());
                    }else{
                        return withOutAggregateGroupedData(groupedRowData);  
                    }
               
            }else{
                dataList = parseHpccData(respone,chartData,titleColumns);  
            }
         } else {
             throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
         }
         if(LOG.isDebugEnabled()){
             LOG.debug("getGenericQueryData() dataList -->"+dataList);
         }
        return dataList;
    }
    
    private String constructQueryURLWithoutReqName(XYChartData chartData) throws UnsupportedEncodingException {
            
         StringBuilder urlBuilder = new StringBuilder();
         if (chartData.getHpccConnection().getIsSSL()) {
             urlBuilder.append(Constants.HTTPS);
         } else {
             urlBuilder.append(Constants.HTTP);
         }
         urlBuilder.append(chartData.getHpccConnection().getHostIp())
                 .append(":")
                 .append(chartData.getHpccConnection().getWsEclPort())
                 .append("/WsEcl/submit/query/")
                 .append(chartData.getHpccConnection().getClusterType())
                 .append("/")
                 .append(chartData.getFiles().iterator().next())
                 .append("/xml?");        
         
          urlBuilder.append(MEASURE_COLUMN).append("=");
          Iterator<Measure> measureItr = chartData.getMeasures().iterator();
          while(measureItr.hasNext()){
              urlBuilder.append(measureItr.next().getColumn());
              if (measureItr.hasNext()) {
                  urlBuilder.append(",");
              }
          }
          
         urlBuilder.append("&").append(ATTRIBUTE_COLUMN).append("=")
                 .append(chartData.getAttribute().getColumn());
         
          if(chartData.isGrouped()){
              urlBuilder.append("&");
              urlBuilder.append(GROUPBY_COLUMN).append("=").append(chartData.getGroupAttribute().getColumn());
          }
          if (chartData.getInputParams() != null ) {
              Iterator<InputParam> iterator = chartData.getInputParams().iterator();
              urlBuilder.append("&");
              while (iterator.hasNext()) {
                  InputParam inputParam = iterator.next();
                  if(!StringUtils.isNullOrEmpty(inputParam.getValue())){
                    urlBuilder
                            .append(inputParam.getName())
                            .append("=")
                            .append(URLEncoder.encode(inputParam.getValue(),
                                    Constants.CHAR_CODE));
                      if (iterator.hasNext()) {
                          urlBuilder.append("&");
                      }
                  }
              }
          }          

          if(LOG.isDebugEnabled()){
             LOG.debug("getGenericQueryData() URL -->"+urlBuilder);
          }        
        return urlBuilder.toString();
    }

    private String constructQueryURLWithReqName(String requestName,XYChartData chartData) throws UnsupportedEncodingException {
        StringBuilder requestbuilder = new StringBuilder(requestName);
        requestbuilder.append(ROW_NO);
            
         StringBuilder urlBuilder = new StringBuilder();
         if (chartData.getHpccConnection().getIsSSL()) {
             urlBuilder.append(Constants.HTTPS);
         } else {
             urlBuilder.append(Constants.HTTP);
         }
         urlBuilder.append(chartData.getHpccConnection().getHostIp())
                 .append(":")
                 .append(chartData.getHpccConnection().getWsEclPort())
                 .append("/WsEcl/submit/query/")
                 .append(chartData.getHpccConnection().getClusterType())
                 .append("/")
                 .append(chartData.getFiles().iterator().next())
                 .append("/xml?");        
         
          urlBuilder.append(requestbuilder).append(MEASURE_COLUMN).append("=");
          Iterator<Measure> measureItr = chartData.getMeasures().iterator();
          while(measureItr.hasNext()){
              urlBuilder.append(measureItr.next().getColumn());
              if (measureItr.hasNext()) {
                  urlBuilder.append(",");
              }
          }
          
         urlBuilder.append("&").append(requestbuilder).append(ATTRIBUTE_COLUMN).append("=")
                 .append(chartData.getAttribute().getColumn());
         
          if(chartData.isGrouped()){
              urlBuilder.append("&").append(requestbuilder);
              urlBuilder.append(GROUPBY_COLUMN).append("=").append(chartData.getGroupAttribute().getColumn());
          }
          if (chartData.getInputParams() != null ) {
              Iterator<InputParam> iterator = chartData.getInputParams().iterator();
              urlBuilder.append("&");
              while (iterator.hasNext()) {
                  InputParam inputParam = iterator.next();
                  if(!StringUtils.isNullOrEmpty(inputParam.getValue())){
                    urlBuilder
                            .append(requestbuilder)
                            .append(inputParam.getName())
                            .append("=")
                            .append(URLEncoder.encode(inputParam.getValue(),
                                    Constants.CHAR_CODE));
                      if (iterator.hasNext()) {
                          urlBuilder.append("&");
                      }
                  }
              }
          }          

          if(LOG.isDebugEnabled()){
             LOG.debug("getGenericQueryData() URL -->"+urlBuilder);
          }        
        return urlBuilder.toString();
    }

    private String getGenericQueryRequestName(ChartData chartData) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
        StringBuilder urlBuilder = new StringBuilder();
       if (chartData.getHpccConnection().getIsSSL()) {
           urlBuilder.append(Constants.HTTPS);
       } else {
           urlBuilder.append(Constants.HTTP);
       }
       urlBuilder.append(chartData.getHpccConnection().getHostIp())
               .append(":")
               .append(chartData.getHpccConnection().getWsEclPort())
               .append("/WsEcl/example/request/query/")
               .append(chartData.getHpccConnection().getClusterType())
               .append("/")
               .append(chartData.getFiles().iterator().next())
               .append("?display");   
       
       if(LOG.isDebugEnabled()){
           LOG.debug("getGenericQueryRequestName() URL -->"+urlBuilder);
        }        
       
        URL url = new URL(urlBuilder.toString());
        URLConnection urlConnection = url.openConnection();
        String authString = chartData.getHpccConnection().getUsername() + ":"
                + chartData.getHpccConnection().getPassword();
        String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
        urlConnection.setRequestProperty(AUTHORIZATION, BASIC+ authStringEnc);


        final InputStream response= urlConnection.getInputStream();
        
       String requestName = parseGenericQueryRequest(chartData.getFiles().iterator().next(),response);
        LOG.debug("requestName -->"+requestName);
        return requestName;
    }

    private String parseGenericQueryRequest(String query, InputStream response)
            throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {
        String reqName = null;
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document doc = db.parse(response);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        
        NodeList rows = (NodeList) xPath.evaluate("/" + query + "Request", doc, XPathConstants.NODESET);
        
        NodeList list = ((Node)rows.item(0)).getChildNodes();
        if(list.item(0) != null){
            reqName = list.item(0).getNodeName();
        }        
        LOG.debug("Request Row name ->" + reqName); 
        
        return reqName;
    }

    private List<XYModel> withOutAggregateGroupedData(Map<String, Map<String, List<Object>>> groupedData) {
        List<XYModel> dataList = new ArrayList<XYModel>();
        List<Object> xvalueList = null;
        List<Object> yvalueList = null;
        Object xValue = null;
        XYModel xyModel = null;
        for(Entry<String, Map<String, List<Object>>> entry :groupedData.entrySet()){
            xValue =  entry.getKey();
           
            for(Entry<String, List<Object>> group : entry.getValue().entrySet()){
                xvalueList = new ArrayList<Object>();
                xvalueList.add(xValue);
                xvalueList.add(group.getKey());
                yvalueList = new ArrayList<Object>();
                for (Object object : group.getValue()) {
                    yvalueList.add(object);
               }
                xyModel = new XYModel();
                xyModel.setxAxisValues(xvalueList);
                xyModel.setyAxisValues(yvalueList);
                dataList.add(xyModel);
            }
             
        }
        LOG.debug("Aggregated dataList -->"+dataList);
       return dataList;
    }
    /**
     * Aggregates chart data while grouping more attributes/x-columns with aggregate function
     * @param groupedData
     * @param aggregateFn
     * @return List<XYModel>
     */
    private List<XYModel> withAggregateGroupedData(Map<String, Map<String, List<Object>>> groupedData,String aggregateFn) {
        
         List<XYModel> dataList = new ArrayList<XYModel>();
         List<Object> xvalueList = null;
         List<BigDecimal> yvalueList = null;
         List<Object> aggregatedYvalue = null;
         Object xValue = null;
         XYModel xyModel = null;
         for(Entry<String, Map<String, List<Object>>> entry :groupedData.entrySet()){
             xValue =  entry.getKey();
            
             for(Entry<String, List<Object>> group : entry.getValue().entrySet()){
                 xvalueList = new ArrayList<Object>();
                 xvalueList.add(xValue);
                 xvalueList.add(group.getKey());
                 yvalueList = new ArrayList<BigDecimal>();
                 for (Object object : group.getValue()) {
                     yvalueList.add((BigDecimal)object);
                }
                 aggregatedYvalue = new ArrayList<Object>();
                 aggregatedYvalue.add(doAggregation(yvalueList, aggregateFn));
                 xyModel = new XYModel();
                 xyModel.setxAxisValues(xvalueList);
                 xyModel.setyAxisValues(aggregatedYvalue);
                 dataList.add(xyModel);
             }
              
         }
         LOG.debug("Aggregated dataList -->"+dataList);
        return dataList;
    }

    /**
     * Groups chart data when chart has aggregate function and more x columns
     * @param urlBuilder
     * @param chartData
     * @return Map<String,Map<String,List<Object>>>
     * @throws HpccConnectionException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private Map<String, Map<String, List<Object>>> getGroupedChartData(
            StringBuilder urlBuilder, XYChartData chartData,
            List<TitleColumn> titleColumns) throws HpccConnectionException,
            IOException,
            ParserConfigurationException, SAXException {
        
         Map<String,Map<String,List<Object>>> groupedRowData = null;
            // Has Input parameter set
            if (chartData.getInputParams() != null ) {
                Iterator<InputParam> iterator = chartData.getInputParams().iterator();
                while (iterator.hasNext()) {
                    InputParam param =  iterator.next();
                    if(!StringUtils.isNullOrEmpty(param.getValue())){
                        urlBuilder.append(param.getName()).append("=").append(URLEncoder.encode(param.getValue(),Constants.CHAR_CODE));
                        if (iterator.hasNext()) {
                            urlBuilder.append("&");
                        }
                    }
                }
            }
            URL url = new URL(urlBuilder.toString());
            URLConnection urlConnection = url.openConnection();
            String authString = chartData.getHpccConnection().getUsername() + ":"
                    + chartData.getHpccConnection().getPassword();
            String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
            urlConnection.setRequestProperty(AUTHORIZATION, BASIC+ authStringEnc);

            if (LOG.isDebugEnabled()) {
                LOG.debug("URL ->" + url);
            }

        final InputStream respone = urlConnection.getInputStream();
      
        if (respone != null) {
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            final DocumentBuilder db = dbf.newDocumentBuilder();
            final Document doc = db.parse(respone);

            final NodeList nodeList = doc.getElementsByTagName("Row");
            groupedRowData  = groupData(nodeList,chartData,titleColumns);
            
        } else {
            throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("groupedRowData -->"+groupedRowData);
        }
        return groupedRowData;    
    }

    private Map<String, Map<String, List<Object>>> groupData(NodeList nodeList,
            XYChartData chartData, List<TitleColumn> titleColumns) {

        Map<String,Map<String,List<Object>>> groupedRowData = new LinkedHashMap<String, Map<String,List<Object>>>();
        
        Attribute firstxColumnName = null;
        Attribute groupedxColumnName = null;
        Node fstNode = null;
        Element fstElmnt = null, lstNmElmnt = null;
        NodeList lstNmElmntLst = null;
        Element secondLstNmElmnt = null;
        NodeList secondLstNmElmntLst = null;

       
        Map<String, List<Object>> childMap = null;
        List<Object> yValues = null;
        for (int s = 0; s < nodeList.getLength(); s++) {
            fstNode = nodeList.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                fstElmnt = (Element) fstNode;
                //getting first x-column value
                firstxColumnName = chartData.getAttribute();
                    lstNmElmntLst = fstElmnt.getElementsByTagName(firstxColumnName.getColumn());
                    lstNmElmnt = (Element) lstNmElmntLst.item(0);
                    if (lstNmElmnt != null) {
                        if(groupedRowData.get(lstNmElmnt.getTextContent()) != null){
                             childMap = groupedRowData.get(lstNmElmnt.getTextContent());                                 
                        }else{
                            groupedRowData.put(lstNmElmnt.getTextContent(), new TreeMap<String, List<Object>>());
                            childMap = groupedRowData.get(lstNmElmnt.getTextContent());
                        }
                        
                    } else {
                        groupedRowData.put("", new TreeMap<String, List<Object>>());
                        childMap = groupedRowData.get("");
                    }   
                    //getting second x-column value
                    groupedxColumnName = chartData.getGroupAttribute();
                     secondLstNmElmntLst = fstElmnt.getElementsByTagName(groupedxColumnName.getColumn());
                     secondLstNmElmnt = (Element) secondLstNmElmntLst.item(0);
                     if (secondLstNmElmnt != null) {
                        if( childMap.get(secondLstNmElmnt.getTextContent()) != null){
                            yValues = childMap.get(secondLstNmElmnt.getTextContent());
                        }else{
                            childMap.put(secondLstNmElmnt.getTextContent(),new ArrayList<Object>());
                            yValues = childMap.get(secondLstNmElmnt.getTextContent());
                        } 
                        
                     } else {
                         childMap.put("", new ArrayList<Object>());
                         yValues = childMap.get("");
                     }
                   //getting Y-column value
                     for (Measure measure : chartData.getMeasures()) {
                         lstNmElmntLst = fstElmnt.getElementsByTagName(measure.getColumn());
                         lstNmElmnt = (Element) lstNmElmntLst.item(0);
                         if (lstNmElmnt != null) {
                             yValues.add(new BigDecimal(lstNmElmnt.getTextContent()));
                         } else {
                             yValues.add(new BigDecimal(0));
                         }
                     }
                     
                     //processing title columns.Taking first row value from the Hpcc response 
                     //when the title columns are part of output columns
                     if(s == 0 && titleColumns != null){
                         parseTitle(titleColumns,fstElmnt);
                     }
            }
        }
        return groupedRowData;
    }

    private List<XYModel> doAggregation(List<XYModel> source, XYChartData chartData) {
        for (Measure measure : chartData.getMeasures()) {
            if(Constants.NONE.equals(measure.getAggregateFunction())) {
                return source;
            }
        }

        // Grouping the duplicates
        Map<String, Map<String, List<BigDecimal>>> group = new TreeMap<String, Map<String, List<BigDecimal>>>();
        Set<String> xValues = new HashSet<String>();

        String xValue;
        String yName;
        BigDecimal yValue;
        Iterator<Object> valueIterator;
        Iterator<Measure> measureIterator;
        List<BigDecimal> yValues;
        Map<String, List<BigDecimal>> yMap;
        for (XYModel xyModel : source) {
            xValue = xyModel.getxAxisValues().get(0).toString();
            if (xValues.add(xValue)) {
                // New xValue
                valueIterator = xyModel.getyAxisValues().iterator();
                measureIterator = chartData.getMeasures().iterator();

                yMap = new HashMap<String, List<BigDecimal>>();
                while (valueIterator.hasNext() && measureIterator.hasNext()) {
                    yName = measureIterator.next().getColumn();
                    yValue = (BigDecimal) valueIterator.next();

                    yValues = new ArrayList<BigDecimal>();
                    yValues.add(yValue);
                    yMap.put(yName, yValues);
                }

                group.put(xValue, yMap);
            } else {
                // Found a duplicate xValue
                valueIterator = xyModel.getyAxisValues().iterator();
                measureIterator = chartData.getMeasures().iterator();

                yMap = group.get(xValue);
                while (valueIterator.hasNext() && measureIterator.hasNext()) {
                    yName = measureIterator.next().getColumn();
                    yValue = (BigDecimal) valueIterator.next();

                    yValues = yMap.get(yName);
                    yValues.add(yValue);
                }

            }
        }

        List<XYModel> result = new ArrayList<XYModel>();
        XYModel xyModel;
        List<Object> xVals;
        List<Object> yVals;
        for (Entry<String, Map<String, List<BigDecimal>>> entry : group.entrySet()) {
            xyModel = new XYModel();
            xVals = new ArrayList<Object>();
            xVals.add(entry.getKey());

            yVals = new ArrayList<Object>();
            for (Measure measure : chartData.getMeasures()) {
                yVals.add(doAggregation(entry.getValue().get(measure.getColumn()), measure.getAggregateFunction()));
                
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Before Aggregation = " + entry.getValue().get(measure.getColumn()));
                    LOG.debug("Aggregated Value = " + doAggregation(entry.getValue().get(measure.getColumn()), measure.getAggregateFunction()));
                }
            }

            xyModel.setxAxisValues(xVals);
            xyModel.setyAxisValues(yVals);

            result.add(xyModel);
        }

        return result;

    }

    /**
     * @param dataList
     * @param aggregation
     * @return BigDecimal
     */
    private BigDecimal doAggregation(List<BigDecimal> dataList, String aggregation) {

        BigDecimal aggregatedValue = null;

        if (Constants.SUM.equalsIgnoreCase(aggregation)) {
            Iterator<BigDecimal> iterator = dataList.iterator();
            aggregatedValue = new BigDecimal(0);
            while (iterator.hasNext()) {
                aggregatedValue = aggregatedValue.add(iterator.next());
            }
        } else if (Constants.MINIMUM.equalsIgnoreCase(aggregation)) {
            TreeSet<BigDecimal> dataSet = new TreeSet<BigDecimal>();
            dataSet.addAll(dataList);
            aggregatedValue = dataSet.first();
        } else if (Constants.MAXIMUM.equalsIgnoreCase(aggregation)) {
            TreeSet<BigDecimal> dataSet = new TreeSet<BigDecimal>();
            dataSet.addAll(dataList);
            aggregatedValue = dataSet.last();
        } else if (Constants.COUNT.equalsIgnoreCase(aggregation)) {
            aggregatedValue = new BigDecimal(dataList.size());
        } else if (Constants.AVERAGE.equalsIgnoreCase(aggregation)) {
            Iterator<BigDecimal> iterator = dataList.iterator();
            BigDecimal aggregatedSum = new BigDecimal(0);
            while (iterator.hasNext()) {
                aggregatedSum = aggregatedSum.add(iterator.next());
            }
            aggregatedValue = aggregatedSum.divide(new BigDecimal(dataList.size()), BigDecimal.ROUND_HALF_EVEN);
        }
        return aggregatedValue;
    }   

    /**
     * @param respone
     * @return List<XYModel> 
     * @throws ParserConfigurationException 
     * @throws IOException  
     * @throws SAXException 
     */
    private List<XYModel> parseHpccData(InputStream respone,
            XYChartData chartData,List<TitleColumn> titleColumns) throws ParserConfigurationException,
            SAXException, IOException {
        
        final List<XYModel> dataList = new ArrayList<XYModel>();
        XYModel dataObj = null;
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document doc = db.parse(respone);

        Node fstNode = null;
        Element fstElmnt = null, lstNmElmnt = null;
        NodeList lstNmElmntLst = null;

        List<Object> valueList = null;

        final NodeList nodeList = doc.getElementsByTagName("Row");
        Boolean isThresholdSet = false;
        for (int s = 0; s < nodeList.getLength(); s++) {
            fstNode = nodeList.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                dataObj = new XYModel();

                fstElmnt = (Element) fstNode;
                valueList = new ArrayList<Object>();
                //processing Attributes
                Attribute xColumnName = chartData.getAttribute();
                    lstNmElmntLst = fstElmnt.getElementsByTagName(xColumnName.getColumn());
                    lstNmElmnt = (Element) lstNmElmntLst.item(0);
                    
                    if (lstNmElmnt != null) {
                        valueList.add(lstNmElmnt.getTextContent());
                    } else {
                        valueList.add("");
                    }
                dataObj.setxAxisValues(valueList);
                
                //processing Measures
                valueList = new ArrayList<Object>();
                for (Measure measure : chartData.getMeasures()) {
                    lstNmElmntLst = fstElmnt.getElementsByTagName(measure.getColumn());
                    lstNmElmnt = (Element) lstNmElmntLst.item(0);
                  
                    if (lstNmElmnt != null) {
                        valueList.add(new BigDecimal(lstNmElmnt.getTextContent()));
                    } else {
                        valueList.add(new BigDecimal(0));
                    }
                }
                
                dataObj.setyAxisValues(valueList);
                dataList.add(dataObj);
               
                //Threshold
                if(s == 0 && !isThresholdSet && chartData.getDynamicYThresholdEnabled() && chartData.getThreshold() != null) {
                    parseSecondaryAxis(isThresholdSet,chartData,fstElmnt);
                }
                
                //processing title columns.Taking first row value from the Hpcc response 
                //when the title columns are part of output columns
                if(s == 0 && titleColumns != null){
                    parseTitle(titleColumns,fstElmnt);
                }
               
            }
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("dataList ->" + dataList);            
        }
        
        return dataList;
    }
    private void parseTitle(List<TitleColumn> titleColumns, Element fstElmnt) {
        Element lstNmElmnt = null;
        NodeList lstNmElmntLst = null;
        for (TitleColumn titleColumn : titleColumns) {
            lstNmElmntLst = fstElmnt.getElementsByTagName(titleColumn.getName());
            lstNmElmnt = (Element) lstNmElmntLst.item(0);
          
            if (lstNmElmnt != null) {
                titleColumn.setValue(lstNmElmnt.getTextContent());
            }
        }        
    }

    private void parseSecondaryAxis(Boolean isThresholdSet,XYChartData chartData, Element fstElmnt) {
        Element lstNmElmnt = null;
        NodeList lstNmElmntLst = null;
        Measure threshold = chartData.getThreshold();
        lstNmElmntLst = fstElmnt.getElementsByTagName(threshold.getColumn() + "_low");                
        lstNmElmnt = (Element) lstNmElmntLst.item(0);
        if (lstNmElmnt != null) {
            if(threshold.isSecondary()) {
                chartData.setY2ThresholdValMin(Double.valueOf(lstNmElmnt.getTextContent()));
            } else {
                chartData.setyThresholdValMin(Double.valueOf(lstNmElmnt.getTextContent()));
            }
        }
        
        lstNmElmntLst = fstElmnt.getElementsByTagName(threshold.getColumn() + "_high");                
        lstNmElmnt = (Element) lstNmElmntLst.item(0);
        if (lstNmElmnt != null) {
            if(threshold.isSecondary()) {
                chartData.setY2ThresholdVaMaxl(Double.valueOf(lstNmElmnt.getTextContent()));
            } else {
                chartData.setyThresholdValMax(Double.valueOf(lstNmElmnt.getTextContent()));
            }
        }
        
        isThresholdSet = true;
    
    }

    /**
     * Fetches chart data when no input parameter/Only one input parameter is specified
     * @param urlBuilder
     * @param chartData
     * @return List<XYModel>
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws HpccConnectionException
     */
    private List<XYModel> getNonGenericQueryData(StringBuilder urlBuilder,
            XYChartData chartData, List<TitleColumn> titleColumns)
            throws IOException, ParserConfigurationException, SAXException,
            HpccConnectionException {
        
        List<XYModel> dataList = null;
      //list holds selected input parameter name
        List<String> selectedInputParams = new ArrayList<String>();
        // Don't have any input parameters or has only one Input parameter set
        if (chartData.getInputParams() != null && chartData.getInputParams().size() == 1) {         
            
            Iterator<InputParam> iterator = chartData.getInputParams().iterator();
            while (iterator.hasNext()) {
                InputParam param = iterator.next();
                if(!StringUtils.isNullOrEmpty(param.getValue())){
                    selectedInputParams.add(param.getName());
                    urlBuilder.append(param.getName()).append("=").append(URLEncoder.encode(param.getValue(),Constants.CHAR_CODE));
    
                    if (iterator.hasNext()) {
                        urlBuilder.append("&");
                    }
                }
            }                     
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("selectedInputParams -->"+selectedInputParams);
        }        
       
        URL url = new URL(urlBuilder.toString());
        URLConnection urlConnection = url.openConnection();
        String authString = chartData.getHpccConnection().getUsername() + ":"
                + chartData.getHpccConnection().getPassword();
        String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
        urlConnection.setRequestProperty(AUTHORIZATION, BASIC+ authStringEnc);

        if (LOG.isDebugEnabled()) {
            LOG.debug("getNoParamChartData - URL ->" + url);
        }

        final InputStream respone = urlConnection.getInputStream();
        
        if (respone != null) {
            dataList = parseHpccData(respone,chartData,titleColumns);            
        } else {
            throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
        }
        return dataList;
    }

    @Override
    public Set<String> getInputParameters(String queryName,
            HpccConnection hpccConnection, boolean isGenericQuery,
            String inputParamQuery) throws Exception {
        
        Set<String> params = new LinkedHashSet<String>();
        try {
            if(isGenericQuery){
                
            }else{
                
                StringBuilder urlBuilder = new StringBuilder();
                if (hpccConnection.getIsSSL()) {
                    urlBuilder.append(Constants.HTTPS);
                } else {
                    urlBuilder.append(Constants.HTTP);
                }
                urlBuilder.append(hpccConnection.getHostIp());
                urlBuilder.append(":");
                urlBuilder.append(hpccConnection.getWsEclPort());
                urlBuilder.append("/WsEcl/example/request/query/");
                urlBuilder.append(hpccConnection.getClusterType());
                urlBuilder.append("/");
                urlBuilder.append(queryName);
                urlBuilder.append("?display");
               
                LOG.debug("Input parameters Req  URL-->"+urlBuilder);
                
                URL url = new URL(urlBuilder.toString());           
                URLConnection urlConnection = url.openConnection();
                String authString = hpccConnection.getUsername() + ":" + hpccConnection.getPassword();
                String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
                urlConnection.setRequestProperty(AUTHORIZATION, BASIC + authStringEnc);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                Document doc = dBuilder.parse(urlConnection.getInputStream());
                doc.getDocumentElement().normalize();

                Node row = doc.getElementsByTagName(queryName + "Request").item(0);
                NodeList nodeList = row.getChildNodes();

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {  
                        params.add(node.getNodeName());
                    }
                }
                
            }
            
        } catch (DOMException | SAXException | IOException | ParserConfigurationException e) {
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        }
        
        LOG.debug("params -->"+params);

        return params;
    }
    
public HashMap<String, HashMap<String, List<Attribute>>> fetchScoredSearchData(ScoredSearchData searchData) throws HpccConnectionException,
    RemoteException {
    
    /*Map for holding the data for table widget with key as result set name and value as another map <with key as 
    column name and values as list of values for that column.*/
    HashMap<String, HashMap<String, List<Attribute>>> resultDataMap = new LinkedHashMap<String, HashMap<String, List<Attribute>>>();
    
    HashMap<String, List<Attribute>> tableDataMap = null;
    final String RECORDS_TO_RETURN = "recordstoreturn";
    try {
    StringBuilder urlBuilder = new StringBuilder();
    if (searchData.getHpccConnection().getIsSSL()) {
        urlBuilder.append(Constants.HTTPS);
    } else {
        urlBuilder.append(Constants.HTTP);
    }
    urlBuilder.append(searchData.getHpccConnection().getHostIp())
    .append(":")
    .append(searchData.getHpccConnection().getWsEclPort())
    .append("/WsEcl/submit/query/")
    .append(searchData.getHpccConnection().getClusterType())
    .append("/")
    .append(searchData.getFiles().iterator().next()).append("/xml?");   
    
     //Append the Filters
    List<AdvancedFilter> filterList = searchData.getAdvancedFilters();
    AdvancedFilter filter=null;
    boolean isNoOfRecordsGiven = false;
    if (filterList != null) {
        Iterator<AdvancedFilter> iterator = filterList.iterator();
        while (iterator.hasNext()) {
            filter = iterator.next();
            if(RECORDS_TO_RETURN.equals(filter.getColumnName())){
                isNoOfRecordsGiven = true;
            }
            if((filter.getOpeartorValue()!=null && filter.getOpeartorValue().trim().length() >0) || (filter.getModifierValue()!=null && filter.getModifierValue().trim().length() >0 ) ){
                urlBuilder.append(filter.getColumnName()).append("=");
            }
            if(filter.getOpeartorValue()!=null && filter.getOpeartorValue().trim().length() >0 ){
                urlBuilder.append("=".equals(filter.getOperator())?"":" "+URLEncoder.encode(filter.getOperator(),Constants.CHAR_CODE))
                .append(URLEncoder.encode(filter.getOpeartorValue(),Constants.CHAR_CODE));
            }
            if(filter.getModifierValue()!=null && filter.getModifierValue().trim().length() >0 ){
                urlBuilder.append("=".equals(filter.getModifier())?"":" "+URLEncoder.encode(filter.getModifier(),Constants.CHAR_CODE))
                .append(URLEncoder.encode(filter.getModifierValue(),Constants.CHAR_CODE));
            }
            if((filter.getOpeartorValue()!=null && filter.getOpeartorValue().trim().length() >0) || (filter.getModifierValue()!=null && filter.getModifierValue().trim().length() >0 ) ){
                urlBuilder.append("&");
            }
        }
    }
    //Append the groupby columns 
    List<String> groupByList = searchData.getGroupbyColumns();
    String grpByColName=null;
    
    if (groupByList != null) {
        Iterator<String> iterator = groupByList.iterator();
        while (iterator.hasNext()) {
            grpByColName = iterator.next();
            urlBuilder.append(grpByColName).append("=").append(URLEncoder.encode("&",Constants.CHAR_CODE));
            urlBuilder.append("&");
        }
    }
    //Append the aggregate function
    if(searchData.getAggregateFunction() !=null && searchData.getAggregateFunction().trim().length() >0){
        urlBuilder.append("scorecombine").append("=").append(URLEncoder.encode(searchData.getAggregateFunction(),Constants.CHAR_CODE));
        urlBuilder.append("&");
    }
    
    if(!isNoOfRecordsGiven){
        urlBuilder.append(RECORDS_TO_RETURN).append("=").append(URLEncoder.encode("100",Constants.CHAR_CODE));
    }
    
    URL url = new URL(urlBuilder.toString());
    URLConnection urlConnection = url.openConnection();
    String authString = searchData.getHpccConnection().getUsername() + ":"
            + searchData.getHpccConnection().getPassword();
    String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
    urlConnection.setRequestProperty(AUTHORIZATION, BASIC + authStringEnc);

    if (LOG.isDebugEnabled()) {
        LOG.debug("Scored search URL ->" + url);
    }
    

    final InputStream respone = urlConnection.getInputStream();

    if (respone != null) {
        Node dataSetNode = null,rowNode=null,columnNode=null;
        Element fstElmnt = null, lstNmElmnt = null;
        NodeList lstNmElmntLst = null;
        List<Attribute> columnListvalue = null;
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document doc = db.parse(respone);
         
        
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        
        XPathExpression expr = xPath.compile("/" + searchData.getFiles().iterator().next() + "Response/Result/Dataset");
        final NodeList datasetNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        NodeList rowNodes = null,columnNodes=null;;
        
        if (datasetNodes != null) {
            String str;
            for (int count = 0; count < datasetNodes.getLength(); count++) {
                dataSetNode = datasetNodes.item(count);
                rowNodes=dataSetNode.getChildNodes();
                tableDataMap = new LinkedHashMap<String, List<Attribute>>();
                
                //Get the output column names for each result set
                
                if(rowNodes!=null && rowNodes.getLength()>0)
                {
                    rowNode = rowNodes.item(0);
                    columnNodes=rowNode.getChildNodes();
                    for (int count1 = 0; count1 < columnNodes.getLength(); count1++) {
                        columnNode = columnNodes.item(count1);
                        columnListvalue = new ArrayList<Attribute>();
                        tableDataMap.put(columnNode.getNodeName(), columnListvalue);
                    }
                }
                for (int count2 = 0; count2 < rowNodes.getLength(); count2++) {
                    rowNode = rowNodes.item(count2);
                    fstElmnt = (Element) rowNode;
                    for (Entry<String, List<Attribute>> data : tableDataMap.entrySet()) {
                        Attribute value = new Attribute();
                        lstNmElmntLst = fstElmnt.getElementsByTagName(data.getKey());
                        lstNmElmnt = (Element) lstNmElmntLst.item(0);
    
                        if (lstNmElmnt != null) {
                            str = lstNmElmnt.getTextContent();
                        } else {
                            str = "";
                        }
                        value.setColumn(str);
                        data.getValue().add(value);
                    }
              }
              if(!tableDataMap.isEmpty()){
                resultDataMap.put(((Element)dataSetNode).getAttribute("name"), tableDataMap);
              }
          }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("fetchScoredSearchTableData -->" + resultDataMap);
        }
    } else {
        throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
    }
} catch (RemoteException e) {
    if (e.getMessage().contains("Unauthorized")) {
        throw new HpccConnectionException("401 Unauthorized");
    }
    LOG.error(Constants.EXCEPTION, e);
    throw e;
} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
    LOG.error(Constants.EXCEPTION, ex);
    throw new HpccConnectionException(ex.getMessage());
}
return resultDataMap;
}

    @Override
    public Map<String, List<Attribute>> fetchTableData(TableData tableData, List<TitleColumn> titleColumns) throws HpccConnectionException,
            RemoteException {
        Map<String, List<Attribute>> tableDataMap = new LinkedHashMap<String, List<Attribute>>();

        try {
            if(tableData.isGenericQuery()){
                   return fetchGenericTableData(tableDataMap,tableData,titleColumns);
             }else{
                StringBuilder urlBuilder = new StringBuilder();
                if (tableData.getHpccConnection().getIsSSL()) {
                    urlBuilder.append(Constants.HTTPS);
                } else {
                    urlBuilder.append(Constants.HTTP);
                }
                urlBuilder.append(tableData.getHpccConnection().getHostIp())
                .append(":")
                .append(tableData.getHpccConnection().getWsEclPort())
                .append("/WsEcl/submit/query/")
                .append(tableData.getHpccConnection().getClusterType())
                .append("/")
                .append(tableData.getFiles().iterator().next()).append("/xml?");    
                
    
                if (tableData.getInputParams() != null) {
                    
                    Iterator<InputParam> iterator = tableData.getInputParams().iterator();
                    while (iterator.hasNext()) {
                        InputParam param = iterator.next();
                        if(!StringUtils.isNullOrEmpty(param.getValue())){
                            urlBuilder.append(param.getName()).append("=").append(URLEncoder.encode(param.getValue(),Constants.CHAR_CODE));               
        
                            if (iterator.hasNext()) {
                                urlBuilder.append("&");
                            }
                        }
                    }
                }
                
    
                URL url = new URL(urlBuilder.toString());
                URLConnection urlConnection = url.openConnection();
                String authString = tableData.getHpccConnection().getUsername() + ":"
                        + tableData.getHpccConnection().getPassword();
                String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
                urlConnection.setRequestProperty(AUTHORIZATION, BASIC + authStringEnc);
    
                if (LOG.isDebugEnabled()) {
                    LOG.debug("URL ->" + url);
                }
                
    
                final InputStream respone = urlConnection.getInputStream();
    
                if (respone != null) {
                    Node fstNode = null;
                    Element fstElmnt = null, lstNmElmnt = null;
                    NodeList lstNmElmntLst = null;
                    List<Attribute> columnListvalue = null;
                    for (Attribute columnName : tableData.getAttributes()) {
                        columnListvalue = new ArrayList<Attribute>();
                        tableDataMap.put(columnName.getColumn(), columnListvalue);
                    }
                    
                    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    final DocumentBuilder db = dbf.newDocumentBuilder();
                    final Document doc = db.parse(respone);
                    XPathFactory xPathFactory = XPathFactory.newInstance();
                    XPath xPath = xPathFactory.newXPath();
                    
                    XPathExpression expr = xPath.compile("/" + tableData.getFiles().iterator().next() + "Response/Result/Dataset/Row");
                    
                    final NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                    
                    if (nodeList != null) {
                        String str;
                        for (int count = 0; count < nodeList.getLength(); count++) {
                            fstNode = nodeList.item(count);
                            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                                fstElmnt = (Element) fstNode;
                                //gets data for table fields 
                                for (Attribute data : tableData.getAttributes()) {
                                    Attribute value = new Attribute();
                                    
                                    if(data.isNested()) {
                                        List<Attribute> childValues = new ArrayList<Attribute>();
                                        for (Attribute childAttribute : data.getChildren()) {
                                            Attribute childValue = new Attribute();
                                            lstNmElmntLst = fstElmnt.getElementsByTagName(childAttribute.getColumn());
                                            
                                            List<Attribute> innerAttributes = new ArrayList<Attribute>();
                                            for (int i = 0; i < lstNmElmntLst.getLength(); i++) {
                                                if(lstNmElmntLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                                    Element element = (Element) lstNmElmntLst.item(i);
                                                    Attribute innerAttribute = new Attribute();
                                                    if (element != null) {
                                                        str = element.getTextContent();
                                                    } else {
                                                        str = "";
                                                    }
                                                    innerAttribute.setColumn(str);
                                                    innerAttributes.add(innerAttribute);
                                                }
                                            }
                                            childValue.setChildren(innerAttributes);
                                            childValues.add(childValue);
                                        }
                                        value.setChildren(childValues);
                                    }
                                    lstNmElmntLst = fstElmnt.getElementsByTagName(data.getColumn());
                                    lstNmElmnt = (Element) lstNmElmntLst.item(0);
    
                                    if (lstNmElmnt != null) {
                                        // Rounding off Numeric values
                                        if (tableData.getFields() != null && DashboardUtil.checkRealValue(tableData
                                                .getFields()
                                                .get(data.getFileName())
                                                .get(tableData.getFields().get(data.getFileName())
                                                        .indexOf(new Field(data.getColumn(), null))).getDataType())) {
                                            str = new BigDecimal(lstNmElmnt.getTextContent()).setScale(2,
                                                    RoundingMode.HALF_EVEN).toPlainString();
                                        } else {
                                            str = lstNmElmnt.getTextContent();
                                        }
                                    } else {
                                        if (tableData.getFields() != null && DashboardUtil.checkRealValue(tableData
                                                .getFields()
                                                .get(data.getFileName())
                                                .get(tableData.getFields().get(data.getFileName())
                                                        .indexOf(new Field(data.getColumn(), null))).getDataType())) {
                                            str = new BigDecimal(0).setScale(2, RoundingMode.HALF_EVEN).toPlainString();
                                        } else {
                                            str = "";
                                        }
    
                                    }
                                    columnListvalue = tableDataMap.get(data.getColumn());
                                    value.setColumn(str);
                                    columnListvalue.add(value);
                                }
                                
                              //processing title columns.Taking first row value from the Hpcc response 
                                //when the title columns are part of output columns
                                if(count == 0 && titleColumns != null){
                                    for (TitleColumn titleColumn : titleColumns) {
                                        lstNmElmntLst = fstElmnt.getElementsByTagName(titleColumn.getName());
                                        lstNmElmnt = (Element) lstNmElmntLst.item(0);
                                      
                                        if (lstNmElmnt != null) {
                                            titleColumn.setValue(lstNmElmnt.getTextContent());
                                        }
                                    }
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
         }
        } catch (RemoteException e) {
            if (e.getMessage().contains("Unauthorized")) {
                throw new HpccConnectionException("401 Unauthorized");
            }
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw new HpccConnectionException(ex.getMessage());
        }
        return tableDataMap;
    }
    
    private Map<String, List<Attribute>> fetchGenericTableData(
            Map<String, List<Attribute>> tableDataMap, TableData tableData,List<TitleColumn> titleColumns)
            throws XPathExpressionException, IOException,
            ParserConfigurationException, SAXException, HpccConnectionException {
         
         String requestName = getGenericQueryRequestName(tableData);
         StringBuilder requestbuilder = null;
           
         StringBuilder urlBuilder = new StringBuilder();
         if (tableData.getHpccConnection().getIsSSL()) {
             urlBuilder.append(Constants.HTTPS);
         } else {
             urlBuilder.append(Constants.HTTP);
         }
         urlBuilder.append(tableData.getHpccConnection().getHostIp())
         .append(":")
         .append(tableData.getHpccConnection().getWsEclPort())
         .append("/WsEcl/submit/query/")
         .append(tableData.getHpccConnection().getClusterType())
         .append("/")
         .append(tableData.getFiles().iterator().next()).append("/xml?");    
         
         if(requestName != null){
             requestbuilder = new StringBuilder(requestName);
             requestbuilder.append(ROW_NO);
             urlBuilder.append(addInputparamToURL(requestbuilder,tableData.getInputParams()));
         }else{
             urlBuilder.append(addInputparamToURL(tableData.getInputParams()));
         }

         URL url = new URL(urlBuilder.toString());
         URLConnection urlConnection = url.openConnection();
         String authString = tableData.getHpccConnection().getUsername() + ":"
                 + tableData.getHpccConnection().getPassword();
         String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
         urlConnection.setRequestProperty(AUTHORIZATION, BASIC + authStringEnc);

         if (LOG.isDebugEnabled()) {
             LOG.debug("fetchGenericTableData() URL ->" + url);
         }
         

         final InputStream respone = urlConnection.getInputStream();

         if (respone != null) {
             Node fstNode = null;
             Element fstElmnt = null, lstNmElmnt = null;
             NodeList lstNmElmntLst = null;
             List<Attribute> columnListvalue = null;
             for (Attribute columnName : tableData.getAttributes()) {
                 columnListvalue = new ArrayList<Attribute>();
                 tableDataMap.put(columnName.getColumn(), columnListvalue);
             }
             
             final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             final DocumentBuilder db = dbf.newDocumentBuilder();
             final Document doc = db.parse(respone);
             XPathFactory xPathFactory = XPathFactory.newInstance();
             XPath xPath = xPathFactory.newXPath();
             
             XPathExpression expr = xPath.compile("/" + tableData.getFiles().iterator().next() + "Response/Result/Dataset/Row");
             
             final NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
             
             if (nodeList != null) {
                 String str;
                 for (int count = 0; count < nodeList.getLength(); count++) {
                     fstNode = nodeList.item(count);
                     if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                         fstElmnt = (Element) fstNode;
                         for (Attribute data : tableData.getAttributes()) {
                             Attribute value = new Attribute();
                             
                             if(data.isNested()) {
                                 List<Attribute> childValues = new ArrayList<Attribute>();
                                 for (Attribute childAttribute : data.getChildren()) {
                                     Attribute childValue = new Attribute();
                                     lstNmElmntLst = fstElmnt.getElementsByTagName(childAttribute.getColumn());
                                     
                                     List<Attribute> innerAttributes = new ArrayList<Attribute>();
                                     for (int i = 0; i < lstNmElmntLst.getLength(); i++) {
                                         if(lstNmElmntLst.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                             Element element = (Element) lstNmElmntLst.item(i);
                                             Attribute innerAttribute = new Attribute();
                                             if (element != null) {
                                                 str = element.getTextContent();
                                             } else {
                                                 str = "";
                                             }
                                             innerAttribute.setColumn(str);
                                             innerAttributes.add(innerAttribute);
                                         }
                                     }
                                     childValue.setChildren(innerAttributes);
                                     childValues.add(childValue);
                                 }
                                 value.setChildren(childValues);
                             }
                             lstNmElmntLst = fstElmnt.getElementsByTagName(data.getColumn());
                             lstNmElmnt = (Element) lstNmElmntLst.item(0);

                             if (lstNmElmnt != null) {
                                 // Rounding off Numeric values
                                 if (tableData.getFields() != null && DashboardUtil.checkRealValue(tableData
                                         .getFields()
                                         .get(data.getFileName())
                                         .get(tableData.getFields().get(data.getFileName())
                                                 .indexOf(new Field(data.getColumn(), null))).getDataType())) {
                                     str = new BigDecimal(lstNmElmnt.getTextContent()).setScale(2,
                                             RoundingMode.HALF_EVEN).toPlainString();
                                 } else {
                                     str = lstNmElmnt.getTextContent();
                                 }
                             } else {
                                 if (tableData.getFields() != null && DashboardUtil.checkRealValue(tableData
                                         .getFields()
                                         .get(data.getFileName())
                                         .get(tableData.getFields().get(data.getFileName())
                                                 .indexOf(new Field(data.getColumn(), null))).getDataType())) {
                                     str = new BigDecimal(0).setScale(2, RoundingMode.HALF_EVEN).toPlainString();
                                 } else {
                                     str = "";
                                 }

                             }
                             columnListvalue = tableDataMap.get(data.getColumn());
                             value.setColumn(str);
                             columnListvalue.add(value);
                         }
                         
                         //processing title columns.Taking first row value from the Hpcc response 
                         //when the title columns are part of output columns
                         if(count == 0 && titleColumns != null){
                             for (TitleColumn titleColumn : titleColumns) {
                                 lstNmElmntLst = fstElmnt.getElementsByTagName(titleColumn.getName());
                                 lstNmElmnt = (Element) lstNmElmntLst.item(0);
                               
                                 if (lstNmElmnt != null) {
                                     titleColumn.setValue(lstNmElmnt.getTextContent());
                                 }
                             }
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
         return tableDataMap;
    }

    private Object addInputparamToURL(List<InputParam> inputParams) throws UnsupportedEncodingException {        
        String urlStr= null;        
        if (inputParams != null) {
            StringBuilder urlBuilder = new StringBuilder();
            Iterator<InputParam> iterator = inputParams.iterator();
            while (iterator.hasNext()) {
                InputParam param = iterator.next();
                if(!StringUtils.isNullOrEmpty(param.getValue())){
                    urlBuilder.append(param.getName()).append("=").append(URLEncoder.encode(param.getValue(),Constants.CHAR_CODE));
                    if (iterator.hasNext()) {
                        urlBuilder.append("&");
                    }
                }
            }
            urlStr = urlBuilder.toString();
        }
        return urlStr;    
    }

    private String addInputparamToURL(StringBuilder requestbuilder,
            List<InputParam> inputParams) throws UnsupportedEncodingException {        
        String urlStr= null;        
        if (inputParams != null) {
            StringBuilder urlBuilder = new StringBuilder();
            Iterator<InputParam> iterator = inputParams.iterator();
            while (iterator.hasNext()) {
                InputParam param = iterator.next();
                if(!StringUtils.isNullOrEmpty(param.getValue())){
                    urlBuilder.append(requestbuilder).append(param.getName()).append("=").append(URLEncoder.encode(param.getValue(),Constants.CHAR_CODE));
                    if (iterator.hasNext()) {
                        urlBuilder.append("&");
                    }
                }
            }
            urlStr = urlBuilder.toString();
        }
        return urlStr;
    }

    @Override
    public Map<String, Set<String>> getInputParamDistinctValues(
            String queryName, Set<String> inputParams,
            HpccConnection hpccConnection,boolean isGenericQuery, String inputParamQuery) throws Exception {
            
            Map<String,Set<String>> inputParamValues = new LinkedHashMap<String, Set<String>>();    
                
        try {
                if(isGenericQuery){
                    
                }else{
                    // creating set for each input parameter
                    for (String param : inputParams) {
                        inputParamValues.put(param, new TreeSet<String>());
                    }
                    getDistinctValuesbyParsing(queryName, hpccConnection,inputParamValues);
                }
            
            }catch( ParserConfigurationException | SAXException | IOException e){
                LOG.error(Constants.EXCEPTION,e);
                throw e;            
            }
            if(LOG.isDebugEnabled()){
                LOG.debug("inputParamValues -->"+inputParamValues);
            }
            return inputParamValues;
        }

    private void getDistinctValuesbyParsing(String queryName,HpccConnection hpccConnection,
            Map<String, Set<String>> inputParamValues) throws IOException, ParserConfigurationException, SAXException {
         StringBuilder urlBuilder = new StringBuilder();
         if (hpccConnection.getIsSSL()) {
             urlBuilder.append(Constants.HTTPS);
         } else {
             urlBuilder.append(Constants.HTTP);
         }
         urlBuilder.append(hpccConnection.getHostIp())
                 .append(":")
                 .append(hpccConnection.getWsEclPort())
                 .append("/WsEcl/submit/query/")
                 .append(hpccConnection.getClusterType())
                 .append("/")
                 .append(queryName)
                 .append("/xml?");            

         URL url = new URL(urlBuilder.toString());
         URLConnection urlConnection = url.openConnection();
         String authString = hpccConnection.getUsername() + ":"
                 + hpccConnection.getPassword();
         String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
         urlConnection.setRequestProperty(AUTHORIZATION, BASIC + authStringEnc);

         if (LOG.isDebugEnabled()) {
             LOG.debug("URL ->" + url);
         }

         final InputStream respone = urlConnection.getInputStream();

         if (respone != null) {
             final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             final DocumentBuilder db = dbf.newDocumentBuilder();
             final Document doc = db.parse(respone);

             Node fstNode = null;
             Element fstElmnt = null, lstNmElmnt = null;
             NodeList lstNmElmntLst = null;


             final NodeList nodeList = doc.getElementsByTagName("Row");
             for (int s = 0; s < nodeList.getLength(); s++) {
                 fstNode = nodeList.item(s);
                 if (fstNode.getNodeType() == Node.ELEMENT_NODE) {

                     fstElmnt = (Element) fstNode;
                     for (Entry<String,Set<String>> entry: inputParamValues.entrySet()) {
                         lstNmElmntLst = fstElmnt.getElementsByTagName(org.apache.commons.lang.StringUtils.removeEndIgnoreCase(entry.getKey(), "in"));
                         lstNmElmnt = (Element) lstNmElmntLst.item(0);
                         if (lstNmElmnt != null) {
                             entry.getValue().add(lstNmElmnt.getTextContent());
                         }
                     }
                 }
             }
         }
        
    }

    @Override
    public List<List<String>> getRootValues(TreeData treeData, Level level,
            List<TreeFilter> treeFilters) throws HpccConnectionException, RemoteException {
        
        List<List<String>> valueList = null;
         String query = level.getElements().get(0).getFileName();
        try {
            StringBuilder urlBuilder = new StringBuilder();
            if (treeData.getHpccConnection().getIsSSL()) {
                urlBuilder.append(Constants.HTTPS);
            } else {
                urlBuilder.append(Constants.HTTP);
            }
            urlBuilder.append(treeData.getHpccConnection().getHostIp())
                    .append(":")
                    .append(treeData.getHpccConnection().getWsEclPort())
                    .append("/WsEcl/submit/query/")
                    .append(treeData.getHpccConnection().getClusterType())
                    //Since a level can use only one query, taking first LevelElement's query
                    .append("/").append(query)
                    .append("/xml?");
            
            if(treeFilters != null){               
            
                Iterator<TreeFilter> filterIterator = treeFilters.iterator();
                while (filterIterator.hasNext()) {
                    TreeFilter treeFilter = (TreeFilter) filterIterator.next();
                    urlBuilder
                            .append(treeFilter.getColumnName())
                            .append("=")
                            .append(URLEncoder.encode(treeFilter.getValue(),Constants.CHAR_CODE));
                    if (filterIterator.hasNext()) {
                        urlBuilder.append("&");
                    }
                }
            }
            
            URL url = new URL(urlBuilder.toString());
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("getRootValues() URL ->" + url);
            }
            
            URLConnection urlConnection = url.openConnection();
            String authString = treeData.getHpccConnection().getUsername()
                    + ":" + treeData.getHpccConnection().getPassword();
            String authStringEnc = new String(Base64.encodeBase64(authString.getBytes()));
            urlConnection.setRequestProperty(AUTHORIZATION, BASIC+ authStringEnc);

            final InputStream response = urlConnection.getInputStream();
            
            if (response != null) {
                valueList =  constructChildValueList(response,level);
            } else {
                throw new HpccConnectionException(Constants.UNABLE_TO_FETCH_DATA);
            }
            
        } catch (RemoteException e) {
            if (e.getMessage().contains("Unauthorized")) {
                throw new HpccConnectionException("401 Unauthorized");
            }
            LOG.error(Constants.EXCEPTION, e);
            throw e;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw new HpccConnectionException(ex.getMessage());
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("valueList --->"+valueList);
        }
        return valueList;
    }
    
    /**
     * Parse the hpcc response to construct list with child node values 
     * @param response
     * @param level
     * @return List<List<String>>
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private List<List<String>> constructChildValueList(InputStream response,
            Level level) throws ParserConfigurationException, SAXException,
            IOException {

        List<List<String>> valueList = new ArrayList<List<String>>();       
        
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document doc = db.parse(response);
        
        Node fstNode = null;
        Element fstElmnt = null, lstNmElmnt = null;
        NodeList lstNmElmntLst = null;
        List<String> oneNodeValue = null;
        LevelElement element = null;

        final NodeList nodeList = doc.getElementsByTagName("Row");
        for (int s = 0; s < nodeList.getLength(); s++) {
            fstNode = nodeList.item(s);
            if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
                fstElmnt = (Element) fstNode;
                oneNodeValue = new ArrayList<String>();

                Iterator<LevelElement> iterator = level.getElements().iterator();
                while (iterator.hasNext()) {
                    element = (LevelElement) iterator.next();
                    if (element.getIsColumn()) {
                        lstNmElmntLst = fstElmnt.getElementsByTagName(element.getName());
                        lstNmElmnt = (Element) lstNmElmntLst.item(0);
                        if (lstNmElmnt != null) {
                            if (!oneNodeValue.contains(lstNmElmnt.getTextContent())) {
                                oneNodeValue.add(lstNmElmnt.getTextContent());
                            }
                        }
                    }
                }
                if (!valueList.contains(oneNodeValue)) {
                    valueList.add(oneNodeValue);
                }
            }
        }
        return valueList;
    }

    

}
