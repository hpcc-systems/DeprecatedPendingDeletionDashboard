package org.hpccsystems.dashboard.chart.utils;

import java.io.IOException;
import java.math.BigDecimal;
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
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.cluster.ClusterData;
import org.hpccsystems.dashboard.chart.cluster.ClusterJSON;
import org.hpccsystems.dashboard.chart.cluster.ClusterLink;
import org.hpccsystems.dashboard.chart.cluster.ClusterNode;
import org.hpccsystems.dashboard.chart.cluster.Relation;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.entity.InputParam;
import org.hpccsystems.dashboard.chart.entity.Measure;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.entity.XYGroup;
import org.hpccsystems.dashboard.chart.entity.XYModel;
import org.hpccsystems.dashboard.chart.entity.YColumn;
import org.hpccsystems.dashboard.chart.gauge.GaugeChartData;
import org.hpccsystems.dashboard.chart.gauge.GaugeElement;
import org.hpccsystems.dashboard.chart.gauge.GaugeJSON;
import org.hpccsystems.dashboard.chart.tree.entity.Level;
import org.hpccsystems.dashboard.chart.tree.entity.LevelElement;
import org.hpccsystems.dashboard.chart.tree.entity.Node;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.chart.tree.entity.TreeFilter;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.exception.HpccConnectionException;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.impl.HPCCServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.util.Clients;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ChartRenderer {
    
    private static final  Log LOG = LogFactory.getLog(ChartRenderer.class);
    
    private static final String X_AXIS_LABEL = "xAxisLabel";
    private static final String PRIMARY_Y_AXIS_LABEL = "primaryYAxisLabel";
    private static final String SECONDARY_Y_AXIS_LABEL = "secondaryYAxisLabel";
   
    private static final String IS_ENABLED = "isEnabled";
    private static final String CHART_TYPES = "chartTypes";
    private static final String AXES = "axes";
	private static final String ROTATE_AXIS= "rotateAxis";
	
	private static final String Y_MIN = "yMin";
	private static final String Y_MAX = "yMax";
	private static final String Y2_MIN = "y2Min";
	private static final String Y2_MAX = "y2Max";
	private static final String Y_THRESHOLD = "yThreshold";
	private static final String Y2_THRESHOLD = "y2Threshold";
    
    private HPCCService hpccService;
    private ChartService chartService;
    
    @Autowired
    public void setChartService(ChartService chartService) {
        this.chartService = chartService;
    }
    
    @Autowired
    public void setHpccService(HPCCService hpccService) {
        this.hpccService = hpccService;
    }
    
    
    /**
     * Constructs the JSON Object required to draw D3 graph and places the constructed JSON in Portlet Object
     * @param chartData
     *     Chart Data that contains details to draw chart
     * @param chartType
     * @param portlet
     *     Portlet Object for which chartData is to be generated. 
     *     The JSON data constructed will be available in this portlet.
     * 
     * @return
     *  Generates JSON into passed Portlet object    
     * @throws IOException 
     * @throws SAXException 
     * @throws ParserConfigurationException 
     * @throws ServiceException 
     * @throws HpccConnectionException 
     * @throws XPathExpressionException 
     */
    public void constructChartJSON(XYChartData chartData, Portlet portlet,
            Boolean isEditWindow) throws  HpccConnectionException, XPathExpressionException  {

        final JsonObject header = new JsonObject();
        ChartDetails chartInfo=chartService.getCharts().get(portlet.getChartType());
        
        header.addProperty(ROTATE_AXIS, new Boolean(chartData.getIsAxisrotated()));
        
        header.addProperty(X_AXIS_LABEL, chartData.getAttribute().getDisplayName());
        String[] yLabels = chartData.getyAxisLabels();
        header.addProperty(PRIMARY_Y_AXIS_LABEL, yLabels[0]);
        header.addProperty(SECONDARY_Y_AXIS_LABEL, yLabels[1]);
        
        if(isEditWindow) {
            header.addProperty(Constants.PORTLET_ID, "e_" + portlet.getId());
        } else {
            header.addProperty(Constants.PORTLET_ID, "p_" + portlet.getId());
        }
        
        if(LOG.isDebugEnabled()){
            LOG.debug("Constructing chart \n Is chart has filters - " + chartData.getIsFiltered());
        }
        
        StringBuilder filterDescription = new StringBuilder();
        if(chartData.getIsFiltered()) {
            filterDescription.append(" WHERE ");
        }
        
        Iterator<Filter> filterIterator = chartData.getFilters().iterator(); 
        while (filterIterator.hasNext()) {
            Filter filter = (Filter) filterIterator.next();
            if(LOG.isDebugEnabled()) {
                LOG.debug("Filter -> " + filter);
            }
            
            header.addProperty("isFiltered", true);
            if(chartData.getIsFiltered() &&
                    Constants.DATA_TYPE_STRING.equals(filter.getType())) {
                filterDescription.append(filter.getColumn());
                filterDescription.append(" IS ");
                
                Iterator<String> iterator = filter.getValues().iterator();
                while(iterator.hasNext()){
                    filterDescription.append(iterator.next());
                    if(iterator.hasNext()){
                        filterDescription.append(", ");
                    }
                }

            } else if (chartData.getIsFiltered() && 
                    Constants.DATA_TYPE_NUMERIC.equals(filter.getType())) {
                filterDescription.append(filter.getColumn());
                filterDescription.append(" BETWEEN " + filter.getStartValue());
                filterDescription.append(" & " + filter.getEndValue());
                
            }else if(chartData.getIsFiltered() 
                    &&(Constants.CURRENT_DATE_NUMERIC.equals(filter.getType())
                            || Constants.CURRENT_DATE_STRING.equals(filter.getType()))){                
                SimpleDateFormat formatter = new SimpleDateFormat(filter.getCurrentDateFormat());
                String stringDate = formatter.format(new Date());
                filterDescription.append(filter.getColumn()).append("=")
                .append(stringDate);
            }
            
            if(filterIterator.hasNext()){
                filterDescription.append(" AND "); 
            }
        }
        
        if(LOG.isDebugEnabled()){
            LOG.debug("Drawing chart");
            LOG.debug("Chart Type - " + portlet.getChartType());
        }    
        
        Iterator<XYModel> iterator =null;    
        try    {
            List<XYModel> list = null;
            if(chartData.isGrouped()) {
                list = refactorResult(hpccService.getChartData(chartData), chartData);
            }else {
                list = hpccService.getChartData(chartData);
            }
            iterator = list.iterator();    
        }catch(ParserConfigurationException | SAXException
                | IOException | ServiceException | HpccConnectionException e)    {
            LOG.error(Constants.EXCEPTION, e);
            throw new HpccConnectionException("Unable to fetch chart data from Hpcc");
        }

        JsonArray xValues = new JsonArray();
        JsonArray rows = new JsonArray();
        JsonArray row = new JsonArray();
        JsonObject geoRows = new JsonObject();
        List<YColumn> yColumnNames;
        
        //Deciding TimeSeries - Initializing to not a Time series 
        JsonObject timeseries = new JsonObject();
        timeseries.addProperty(IS_ENABLED, false);
        header.add("timeseries", timeseries);
                
        if(Constants.CATEGORY_PIE== chartInfo.getCategory()) {
            row = new JsonArray();
            JsonObject chartTypes = new JsonObject();
            
            if (iterator != null) {
                while (iterator.hasNext()) {
                    final XYModel bar = iterator.next();

                    row.add(new JsonPrimitive((BigDecimal) bar.getyAxisValues().get(0)));

                    xValues.add(new JsonPrimitive(bar.getxAxisValues().get(0).toString()));
                    
                    if (Constants.PIE_CHART.equals(chartInfo.getName())) {
                        chartTypes.addProperty(bar.getxAxisValues().get(0).toString(), "pie");
                    } else if(Constants.DONUT_CHART.equals(chartInfo.getName())) {
                        chartTypes.addProperty(bar.getxAxisValues().get(0).toString(), "donut");
                    }
                    
                    header.add(CHART_TYPES, chartTypes);
                }
            }
            
            //Adding this to avoid C3 error
            header.add(AXES, new JsonObject());
            
            rows.add(xValues);
            rows.add(row);
       
        } else if(Constants.CATEGORY_USGEO== chartInfo.getCategory()){
        	if (iterator != null) {
                while (iterator.hasNext()) {
                    final XYModel bar = iterator.next();
                    geoRows.add(bar.getxAxisValues().get(0).toString().toUpperCase(), 
                            new JsonPrimitive((BigDecimal) bar.getyAxisValues().get(0)));
                }
            }
        } else {
            //Deciding Time series
            String dateFormat = chartData.getAttribute().getD3DateFormat();
            if(dateFormat != null){
                timeseries.addProperty(IS_ENABLED, true);
                timeseries.addProperty("format", chartData.getAttribute().getD3DateFormat());
                timeseries.addProperty("displayFormat", chartData.getAttribute().getD3DateFormat());
                
                //Adding Header Row
                //Adding X Column Name - Only for time series chart
                row.add(new JsonPrimitive(chartData.getAttribute().getColumn()));
            }
            if (chartData.isGrouped()) {
                yColumnNames = new ArrayList<YColumn>();
                for (String colName : chartData.getGroup().getyColumnNames()) {
                    yColumnNames.add(new YColumn(colName));
                    row.add(new JsonPrimitive(colName));
                }
            } else {
                yColumnNames = new ArrayList<YColumn>();
                for (Measure measure : chartData.getMeasures()) {
                    if (measure.getDisplayYColumnName() == null) {
                        if(LOG.isDebugEnabled()) {
                            LOG.debug("Aggregate function -- " + measure.getAggregateFunction());
                        }
                        if(measure.getAggregateFunction() != null) {
                            row.add(new JsonPrimitive(measure.getColumn() + "_"    + measure.getAggregateFunction()));
                            yColumnNames.add(new YColumn(measure.getColumn() + "_"+ measure.getAggregateFunction(), 
                                    measure.isSecondary()));
                        } else {
                            row.add(new JsonPrimitive(measure.getColumn()));
                            yColumnNames.add(new YColumn(measure.getColumn(), measure.isSecondary()));
                        }
                    } else {
                        row.add(new JsonPrimitive(measure.getDisplayYColumnName()));
                        yColumnNames.add( new YColumn(measure.getDisplayYColumnName(), measure.isSecondary()));
                    }
                }
            }
            rows.add(row);
            
            if (iterator != null) {
                while (iterator.hasNext()) {
                    final XYModel bar = iterator.next();
                    //New Row
                    row = new JsonArray();

                    // X values - Only for Timeseries charts
                    if (timeseries.get(IS_ENABLED).getAsBoolean()) {
                        row.add(new JsonPrimitive(bar.getxAxisValues().get(0)
                                .toString()));
                    }

                    // Y values
                    for (Object object : bar.getyAxisValues()) {
                        row.add(new JsonPrimitive((BigDecimal) object));
                    }

                    rows.add(row);

                    xValues.add(new JsonPrimitive(bar.getxAxisValues().get(0).toString()));                   
                }
                String primaryType = "";
                String secondaryType = "";
                if (Constants.BAR_CHART.equals(chartInfo.getName())) {
                    primaryType = "bar";
                    secondaryType = "spline";
                } else if (Constants.LINE_CHART.equals(chartInfo.getName())) {
                    primaryType = "line";
                    secondaryType = "bar";
                }
                
                JsonObject chartTypes = new JsonObject();
                JsonObject axes = new JsonObject();
                for (YColumn yColumn : yColumnNames) {
                    chartTypes.addProperty(yColumn.getName(), yColumn.isSecondary() ? secondaryType : primaryType);
                    axes.addProperty(yColumn.getName(), yColumn.isSecondary() ? "y2" : "y");
                    
                }
                header.add(CHART_TYPES, chartTypes);
                header.add(AXES, axes);
            }
        }
        header.addProperty("filterDescription", filterDescription.toString());

        header.add("dataRows", rows);
        header.add("xCategoryLabels", xValues);
        header.add("states", geoRows);
        
        header.addProperty(Y_MIN, chartData.getyAxisMinVal());
        header.addProperty(Y_MAX, chartData.getyAxisMaxVal());
        header.addProperty(Y2_MIN, chartData.getY2AxisMinVal());
        header.addProperty(Y2_MAX, chartData.getY2AxisMaxVal());
        header.addProperty(Y_THRESHOLD, chartData.getyThresholdVal());
        header.addProperty(Y2_THRESHOLD, chartData.getY2ThresholdVal());

        String data = header.toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("data -->" +data);
        }

        portlet.setChartDataJSON(data);
    }
    
    private List<XYModel> refactorResult(List<XYModel> input,
            XYChartData chartData) throws HpccConnectionException,
            ServiceException, ParserConfigurationException, SAXException,
            IOException {
        List<XYModel> result = new ArrayList<XYModel>();
        List<String> xLabels = null;
        List<String> groupedList = null;
        if(chartData.getIsQuery()){
            xLabels = getAttributeDistinctValues(chartData.getAttribute().getColumn(),input,0);  
            groupedList = getAttributeDistinctValues(chartData.getGroupAttribute().getColumn(),input,1);
        }else{
            xLabels = hpccService.getDistinctValues(
                chartData.getAttribute().getColumn(),   
                chartData.getAttribute().getFileName(),
                chartData, true);
        
            groupedList = hpccService.getDistinctValues(
                chartData.getGroupAttribute().getColumn(), 
                chartData.getGroupAttribute().getFileName(),
                chartData, true); 
        }
                
        //Constructing Group Object
        XYGroup group = new XYGroup();
        List<String> newCols = new ArrayList<String>();
        newCols.add(chartData.getAttribute().getColumn());
        group.setxColumnNames(newCols);
        group.setyColumnNames(groupedList);
        chartData.setGroup(group);
        
        for (String xValue : xLabels) {
            XYModel newRow = new XYModel();
            
            //Setting X Value
            List<Object> xValues = new ArrayList<Object>();
            xValues.add(xValue);
            newRow.setxAxisValues(xValues);
            
            List<Object> yValues = new ArrayList<Object>();
            //Initializing Empty Y Value Map
            Map<String,Object> yValuesMap = new LinkedHashMap<String, Object>();
            for (String groupedColumn : groupedList) {
                yValuesMap.put(groupedColumn, new BigDecimal(0));
            }
            
            // Replacing apt Y Values to the Map
            for (XYModel xyModel : input) {
                for (String groupedColumn : groupedList) {
                    if(xyModel.getxAxisValues().get(0).equals(xValue) &&
                            xyModel.getxAxisValues().get(1).equals(groupedColumn)){
                        yValuesMap.put(groupedColumn, new BigDecimal(xyModel.getyAxisValues().get(0).toString()));
                    }
                }
            }
            
            //Transform Y Map to List
            for (Entry<String, Object> entry : yValuesMap.entrySet()) {
                yValues.add(entry.getValue());
            }
            
            newRow.setxAxisValues(xValues);
            newRow.setyAxisValues(yValues);
            result.add(newRow);
        }
        
        return result;
    }

    /**
     * @param columnName
     * @param input
     * @param columnIndex
     * @return List<String>
     */
    private List<String> getAttributeDistinctValues(String columnName,List<XYModel> input,int columnIndex) {
        
        Set<String> valueSet = new LinkedHashSet<String>();
        for (XYModel xyModel : input) {
            valueSet.add(xyModel.getxAxisValues().get(columnIndex).toString());
        }
        List<String> valueList = new ArrayList<String>();
        valueList.addAll(valueSet);
        return valueList;
    }

    /**
     * Must Construct JSON before invoking this method
     * 
     * Draws D3 chart onto the 'divToDraw' of the specified type
     * Must construct JSON before calling this function
     * @param divToDraw
     * @param portlet
     * @param chartType
     */
    public void drawChart(String divToDraw, Portlet portlet) throws Exception {

        if( portlet.getChartDataJSON() == null) {
            Clients.showNotification(Labels.getLabel("noDataAvailable"), true);
        }    
        String chartJson = null;
        if(Constants.RELEVANT_CONFIG != chartService.getCharts().get(portlet.getChartType()).getCategory()){
            chartJson = StringEscapeUtils.escapeJavaScript(portlet.getChartDataJSON());
        }else{
            chartJson = portlet.getChartDataJSON();
        }
        ChartDetails chartDetails = chartService.getCharts().get(portlet.getChartType());
        
        //Forming java script
        StringBuilder jsBuilder = new StringBuilder();
        
        //Importing Styles
        if(chartDetails.getConfiguration().getDependentCssURL() != null) {
            for (String path : chartDetails.getConfiguration().getDependentCssURL()) {
                jsBuilder.append("jq('head').append('<link rel=\"stylesheet\" type=\"text/css\" href=\"")
                        .append(path)
                        .append("\" />');");
            }
        }
        
        if(chartDetails.getConfiguration().getGooglePackages() != null) {
            jsBuilder.append("function oneMethod() {");
            
            jsBuilder.append("jq.when(");
            
            jsBuilder.append("jq.getScript('")
                .append(chartDetails.getConfiguration().getJsURL())
                .append("'),");
            
            
            jsBuilder.append("$.Deferred(function( deferred ){")
                    .append("$( deferred.resolve );")
                    .append("})")
                    .append(").done(function(){")
                    .append(chartDetails.getConfiguration().getFunctionName())
                  .append("('" + divToDraw +  "','"+ chartJson +"')")
            .append("});");
            
            jsBuilder.append("}");
            
            jsBuilder.append("google.load('visualization', '1', {'packages': [");
            Iterator<String> iterator = chartDetails.getConfiguration().getGooglePackages().iterator();
            
            while (iterator.hasNext()) {
                jsBuilder.append("'");
                jsBuilder.append(iterator.next());
                jsBuilder.append("'");
                
                if(iterator.hasNext()) {
                    jsBuilder.append(",");
                }
            }
            
            jsBuilder.append("],'callback': oneMethod});");
        } else {
            jsBuilder.append("jq.when(");
            if(chartDetails.getConfiguration().getDependentJsURL() != null 
                    && !chartDetails.getConfiguration().getDependentJsURL().isEmpty()) {
                for (String path : chartDetails.getConfiguration().getDependentJsURL()) {
                    jsBuilder.append("jq.getScript('")
                        .append(path)
                        .append("'),");
                }
            }
            
            jsBuilder.append("jq.getScript('")
                .append(chartDetails.getConfiguration().getJsURL())
                .append("'),");
            
            jsBuilder.append("$.Deferred(function( deferred ){")
                .append("$( deferred.resolve );")
                .append("})")
                .append(").done(function(){")
                .append(chartDetails.getConfiguration().getFunctionName())
                .append("('" + divToDraw +  "','"+ chartJson +"')")
                .append("});");
        }
        
        if(LOG.isDebugEnabled()) {
            LOG.debug(jsBuilder.toString());
        }
        
        Clients.evalJavaScript(jsBuilder.toString());
    }
    
    
    /**
     * @param treeData
     * @param levelId
     *  The level for which child elements has to be constructed. Level id corresponds to list index of levels 
     * @param filters
     * @return
     *  Json  format
     *  {
            "name": "Name",
            "children": [
                {
                    "name": "Child 1",
                    "_children": [],
                    "imageSrc": "image url",
                    "level": 2,
                    "filters": [
                        [
                            "Value 1",
                            "Value 2"
                        ],
                        [
                            "Value 1"
                        ]
                    ]
                }
            ]
        }
     * @throws HpccConnectionException 
     * @throws RemoteException 
     * 
     */
    public String constructChildren(TreeData treeData, Node currentNode) throws RemoteException, HpccConnectionException {
        Node rootNode = new Node(currentNode.getName());
        rootNode.setImageSrc(currentNode.getImageSrc());
        int childLevel = currentNode.getLevel() + 1;
        
        List<Node> chiildren = new ArrayList<Node>();
        
        List<TreeFilter> treeFilters = new ArrayList<TreeFilter>();
        
        Iterator<List<String>> filterIterator = currentNode.getFilters().iterator();
        List<String> nodeValues;
        Iterator<Level> levelIterator = treeData.getLevels().iterator();
        Level level;
        
        Iterator<String> valueIterator;
        String value;
        Iterator<LevelElement> elementIterator;
        LevelElement element;
        
        while (filterIterator.hasNext() && levelIterator.hasNext()) {
            nodeValues = filterIterator.next();
            level = levelIterator.next();
            
            valueIterator = nodeValues.iterator();
            elementIterator = level.getElements().iterator();
            while (elementIterator.hasNext() && valueIterator.hasNext()) {
                element = (LevelElement) elementIterator.next();
                if(element.getIsColumn()) {
                    value = (String) valueIterator.next();
                    
                    treeFilters.add(new TreeFilter(
                            element.getFileName(), 
                            element.getName(), 
                            element.getDataType(),
                            value));
                }
            }
        }
        
        //Retriving next level children
        level = treeData.getLevels().get(childLevel);
        if(hpccService == null){
        	hpccService = new HPCCServiceImpl();
        }
        List<List<String>> childValues = hpccService.getRootValues(treeData, level,treeFilters);
        
        chiildren = new ArrayList<Node>();
        StringBuilder stringBuilder;
        Node node;
        
        List<List<String>> filters;
			for (List<String> elementValues : childValues) {
				stringBuilder = new StringBuilder();
				valueIterator = elementValues.iterator();
				
				Iterator<LevelElement> levelElementIterator = level.getElements().iterator();
				
				while (levelElementIterator.hasNext() && valueIterator.hasNext()) {
					LevelElement levelElement = (LevelElement) levelElementIterator.next();
					if (levelElement.getIsColumn()) {
						stringBuilder.append(valueIterator.next());
					} else {
						stringBuilder.append(levelElement.getName());
					}
				}

				node = new Node(stringBuilder.toString());
				node.setImageSrc(level.getImgSrc());
				node.setLevel(childLevel);
				// Checking for next next level nodes
				if (treeData.getLevels().size() > childLevel + 1) {
					node.setDummyChildren();

					filters = new ArrayList<List<String>>();
					filters.addAll(currentNode.getFilters());
					filters.add(elementValues);
					node.setFilters(filters);
				}

				chiildren.add(node);
			}
        
        rootNode.setChildren(chiildren);
        
        if(LOG.isDebugEnabled()) {
			LOG.debug("JSON -->" + new Gson().toJson(rootNode));
        }
        
        return new Gson().toJson(rootNode);
    }
    
    
	/**
	 * Constructs JSON to draw tree and sets it to the passed Portlet object
	 * @param treeData
	 * @param portlet
	 * @param treeId
	 * @return String
	 * @throws RemoteException
	 * @throws HpccConnectionException
	 */
	public void constructTreeJSON(TreeData treeData, Portlet portlet,
			String treeId) throws RemoteException, HpccConnectionException {

		final StringBuilder stringBuilder = new StringBuilder();
		final List<List<String>> filters = new ArrayList<List<String>>();
		final List<String> filter = new ArrayList<String>();
		final List<Level> levels = treeData.getLevels();
		for (LevelElement element : levels.get(0).getElements()) {
			if (element.getIsColumn()) {
				stringBuilder.append(treeData.getRootValueMap().get(
						element.getFileName() + "." + element.getName()));
				filter.add(treeData.getRootValueMap().get(
						element.getFileName() + "." + element.getName()));
			} else {
				stringBuilder.append(element.getName());
			}
		}
		filters.add(filter);
		Node rootNode = new Node(stringBuilder.toString());
		rootNode.setFilters(filters);
		rootNode.setLevel(0);
		rootNode.setImageSrc(levels.get(0).getImgSrc());
		String childJson = constructChildren(treeData, rootNode);
		JsonObject jObj = new JsonObject();
		jObj.addProperty("chart_id", treeId);
		jObj.add("chart_data", new JsonParser().parse(childJson));
		portlet.setChartDataJSON( new Gson().toJson(jObj));
	}
	
	public void constructGaugeJSON(GaugeChartData chartData, Portlet portlet, boolean isEditing) throws HpccConnectionException, XPathExpressionException {
	    GaugeJSON gaugeJSON = new GaugeJSON();
	    gaugeJSON.setValueLabel(chartData.getAttribute().getColumn());
	    
	    if(isEditing) {
	       gaugeJSON.setPortletId("edit" + portlet.getId());
	    } else {
	        gaugeJSON.setPortletId(Constants.PORTLET_ID + portlet.getId());
	    }
	    
	    XYChartData xyChartData = new XYChartData(chartData);
	    xyChartData.setAttribute(chartData.getAttribute());
	    xyChartData.getMeasures().add(chartData.getValue());
	    if(chartData.getTotal() != null) {
	        xyChartData.getMeasures().add(chartData.getTotal());
	    }
	    try {
            List<XYModel> list = hpccService.getChartData(xyChartData);
            
            GaugeElement element;
            List<GaugeElement> elements = new ArrayList<GaugeElement>();
            float value, total;
            Iterator<Object> iterator;
            for (XYModel xyModel : list) {
                element = new GaugeElement();
                element.setName(xyModel.getxAxisValues().iterator().next().toString());
                
                if(chartData.getIsTotalRequired()) {
                    iterator = xyModel.getyAxisValues().iterator();
                    value = Float.valueOf(iterator.next().toString());
                    total = Float.valueOf(iterator.next().toString());
                    
                    element.setPercent(100* value/ total);
                } else {
                    element.setPercent(
                            Float.valueOf(
                                    xyModel.getyAxisValues().iterator().next().toString()));
                }
                
                elements.add(element);
            }
            
            gaugeJSON.setData(elements);
            
            if(LOG.isDebugEnabled()) {
                LOG.debug(new Gson().toJson(gaugeJSON));
            }
            
            portlet.setChartDataJSON(new Gson().toJson(gaugeJSON));
            
        }catch(ParserConfigurationException | SAXException
                | IOException | ServiceException | HpccConnectionException e)    {
            LOG.error(Constants.EXCEPTION, e);
            throw new HpccConnectionException("Unable to fetch chart data from Hpcc");
        }
	}
	
	public void constructClusterJSON(ClusterData chartData, Portlet portlet) 
	        throws HpccConnectionException {
	    ClusterJSON json = new ClusterJSON();
	    
	    if(LOG.isDebugEnabled()) {
	        LOG.debug("Creating JSON.. Relation - " + chartData.getRelation().getChildren());
	    }
	    
	    Set<Attribute> attributes = new LinkedHashSet<Attribute>();
	    attributes.add(chartData.getId());
	    attributes.add(chartData.getRelation());
	    attributes.add(chartData.getCategory());
	    attributes.addAll(chartData.getDetails());
	    
	    TableData tableData = new TableData(chartData);
	    tableData.getAttributes().addAll(attributes);
	    
	    //Set to hold distinct types
	    Set<String> categories = new HashSet<String>();
	    Set<String> linkCategories = new HashSet<String>();
	    
	    try {
            Map<String, List<Attribute>> result = hpccService.fetchTableData(tableData);
            int numOfNodes = result.entrySet().iterator().next().getValue().size();
            
            //Map of Node index to the unique identifier column 
            Map<String,Integer> nodeIndexMap = new HashMap<String, Integer>(); 
            
            
            //Creating Nodes
            List<ClusterNode> nodes = new ArrayList<ClusterNode>();
            ClusterNode node;
            List<String> details;
            String id, type;
            for (int i = 0; i < numOfNodes; i++) {
                id = result.get(chartData.getId().getColumn()).get(i).getColumn();
                nodeIndexMap.put(id, i);
                
                node = new ClusterNode();
                
                type = result.get(chartData.getCategory().getColumn())
                        .get(i)
                        .getColumn();
                node.setType(type);
                categories.add(type);
               
                details = new ArrayList<String>();
                for (Attribute attribute : chartData.getDetails()) {
                   details.add(
                           result.get(attribute.getColumn())
                               .get(i)
                               .getColumn());
                }
                node.setDetail(details);
                
                Attribute identifier = result.get(chartData.getRelation().getColumn())
				                        .get(i);
                
                LOG.debug("Identifier - " + identifier);
                
                List<Relation> relations = new ArrayList<Relation>();
                if(identifier.isNested()){
                    Iterator<Attribute> relationIds = identifier.getChildren().get(0).getChildren().iterator();
                    Iterator<Attribute> relationTypes = identifier.getChildren().get(1).getChildren().iterator();
                    
                    while (relationIds.hasNext() && relationTypes.hasNext()) {
                        relations.add(new Relation(
                                relationIds.next().getColumn(), 
                                relationTypes.next().getColumn()));
                    }
                } else {
                    String[] ids = identifier.getColumn().split(",");
                    for (int j = 0; j < ids.length; j++) {
                        relations.add(new Relation(ids[j]));
                    }
                }
                node.setConnectedNodes(relations);
                nodes.add(node);
            }
            //Creating Links
            List<ClusterLink> links = new ArrayList<ClusterLink>();
            ClusterLink link;
            int i = 0;
            for (ClusterNode clusterNode : nodes) {
                if(clusterNode.getConnectedNodes() != null &&
                        !clusterNode.getConnectedNodes().isEmpty()) {
                    for (Relation identifier : clusterNode.getConnectedNodes()) {
                        //Creating links only when both source & target exists
                        if(nodeIndexMap.get(identifier.getId()) != null) {
                            link = new ClusterLink();
                            link.setSource(i);
                            link.setTarget(nodeIndexMap.get(identifier.getId()));
                            link.setType(identifier.getRelation());
                            
                            linkCategories.add(identifier.getRelation());
                            
                            links.add(link);
                        }
                    }                    
                    
                }
                
                i++;
            }
            
            json.setNodes(nodes);
            json.setLinks(links);
            json.setTypes(new ArrayList<String>(categories));
            json.setLinkTypes(new ArrayList<String>(linkCategories));
            
            if(LOG.isDebugEnabled()) {
                LOG.debug(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(json));
            }
            
            portlet.setChartDataJSON(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(json));
            
        }catch(IOException | HpccConnectionException e)    {
            LOG.error(Constants.EXCEPTION, e);
            throw new HpccConnectionException("Unable to fetch chart data from Hpcc");
        }
	    
	    
	}
}
