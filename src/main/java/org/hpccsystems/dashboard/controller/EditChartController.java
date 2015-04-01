package org.hpccsystems.dashboard.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.api.entity.ApiChartConfiguration;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.entity.Measure;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.DateFormatBox;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.entity.XYConfiguration;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.util.UiGenerator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;

/**
 * EditChartController class is used to handle the edit page of the Dashboard
 * project and controller class for edit_portlet.zul file.
 * 
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditChartController extends SelectorComposer<Component> {
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(EditChartController.class);

    private static final String TRUE = "true";
    private static final String FALSE = "false";
    
    @WireVariable
    private AuthenticationService authenticationService;
    @WireVariable
    private ChartRenderer chartRenderer;
    @WireVariable
    private HPCCService hpccService;
    @WireVariable
    private ChartService chartService;
    @WireVariable
    private HPCCQueryService hpccQueryService;

    @Wire
    private Tabbox measureTabbox;
    @Wire
    private Tabbox attributeTabbox;    
    @Wire
    private Listbox yAxisListbox;
    @Wire
    private Listbox y2AxisListbox;
    @Wire
    private Listbox xAxisListbox;
    @Wire
    private Listbox groupListbox;
    @Wire
    private Div chart;
    @Wire
    private Popup dateFormatPopup;
    @Wire
    private Include filterHolder;
    @Wire
    private Doublebox yAxisMinVal;
    @Wire
    private Doublebox yAxisMaxVal;
    @Wire
    private Doublebox y2AxisMinVal;
    @Wire
    private Doublebox y2AxisMaxVal;
    @Wire
    private Listbox y2MinMax;
    @Wire
    private Vbox measureContainer;
    @Wire
    private Button yAxisConfigBtn;
    @Wire
    private Popup minMaxPopup;
    @Wire
    private Checkbox secondAxisCheck;
    @Wire
    private Checkbox secondAxisHideCheck ;
    @Wire
    private Checkbox rotateAxis;
    @Wire
    private Listitem rotateAxisListItem;
    @Wire
    private Doublebox yAxisThresholdMin;
    @Wire
    private Doublebox yAxisThresholdMax;
    @Wire
    private Doublebox y2AxisThresholdMin;
    @Wire
    private Doublebox y2AxisThresholdMax;
    
    private DateFormatBox dateFormatBox;

    private XYChartData chartData;
    private Button doneButton;
    private Portlet portlet;
    private ChartDetails chartDetails;
    
    final Map<String, Object> parameters = new HashMap<String, Object>();
    
    private boolean queryDataSource;
    
    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        Execution execution = Executions.getCurrent();
        chartData = (XYChartData) execution.getAttribute(Constants.CHART_DATA);
        portlet = (Portlet) execution.getAttribute(Constants.PORTLET);
        doneButton = (Button) execution.getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
        setQueryDataSource(chartData.getIsQuery());
        
        return super.doBeforeCompose(page, parent, compInfo);
    }
    
    @Override
    public void doBeforeComposeChildren(Component comp) throws Exception {
        chartDetails = chartService.getCharts().get(portlet.getChartType());
        super.doBeforeComposeChildren(comp);
    }
    
    public boolean getShowGroupPanel() {
        if(chartDetails.getCategory() == Constants.CATEGORY_XY_CHART) {
            return true;
        } 
        return false;
    }
    
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        this.getSelf().addEventListener(Constants.DRAW_CHART_EVENT, drawChart);
        
        if(Constants.CATEGORY_XY_CHART == chartService.getCharts().get(portlet.getChartType()).getCategory()){
        	yAxisConfigBtn.setVisible(true);
        }
        
        ChartDetails chartInfo=chartService.getCharts().get(portlet.getChartType());
        
        if (Constants.BAR_CHART.equals(chartInfo.getName())) {
        	rotateAxisListItem.setVisible(true);
        	rotateAxis.setChecked(chartData.getIsAxisrotated());
        }
        //Date format for attributes
        dateFormatBox = new DateFormatBox(dateFormatPopup);
        dateFormatPopup.appendChild(dateFormatBox);
        dateFormatPopup.addEventListener("onDateformatAdded", addDateformatListener);
        if(chartData.getyAxisMaxVal() != null){
        	yAxisMaxVal.setValue(chartData.getyAxisMaxVal().doubleValue());
        }
        if(chartData.getyAxisMinVal() != null){
        	yAxisMinVal.setValue(chartData.getyAxisMinVal().doubleValue());
        }
        
        if(chartData.getyThresholdValMin() != null){
            yAxisThresholdMin.setValue(chartData.getyThresholdValMin());
        }
        if(chartData.getyThresholdValMax() != null){
            yAxisThresholdMax.setValue(chartData.getyThresholdValMax());
        }
        if(chartData.getY2ThresholdValMin() != null){
            y2AxisThresholdMin.setValue(chartData.getY2ThresholdValMin());
        }
        if(chartData.getY2ThresholdVaMaxl() != null){
            y2AxisThresholdMax.setValue(chartData.getY2ThresholdVaMaxl());
        }
        
        if(chartData.getHideY2Axis() != null && chartData.getHideY2Axis()){
            secondAxisHideCheck.setChecked(true);
        }
        //Setting params for filter include
        filterHolder.setDynamicProperty(Constants.BUSY_COMPONENT, chart);
        filterHolder.setDynamicProperty(Constants.PARENT, this.getSelf());
        if (chartData.getIsQuery()) {
            filterHolder.setSrc("layout/input_parameters.zul");
        } else {
            filterHolder.setSrc("layout/filter.zul");
        }
        
        Map<String, List<Field>> fieldMap = new LinkedHashMap<String, List<Field>>();
        // API chart config flow without chart
        if (authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)) {
            ApiChartConfiguration configuration = (ApiChartConfiguration) Executions.getCurrent().getAttribute(Constants.CIRCUIT_CONFIG);
            List<Field> fields = new ArrayList<Field>();
            fields.addAll(configuration.getFields());
            fieldMap.put(configuration.getDatasetName(), fields);
            //Disabling FilterBox if any of the column is invalid
            try {
                Set<Field> fieldSet = hpccService.getColumns(configuration.getDatasetName(), configuration.getHpccConnection());
                for (Field feild : fields) {
                    if(!fieldSet.contains(feild)){
                        Events.postEvent(Constants.API_FILTER_EVENT, filterHolder, FALSE);
                        break;
                    }
                }
            } catch (Exception e) {
                LOG.error("Error getting column Schema", e);
                //Diabling filters when file doesn't exist
                Events.postEvent(Constants.API_FILTER_EVENT, filterHolder, FALSE);
            }
            
        } else {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Querying Coloumn Schema \n File -> " + chartData.getFiles().get(0) + 
                            " \nHpcc Connection -> " + chartData.getHpccConnection());
                }
                if(chartData.getFields() == null){
	                List<Field> fields;
	                QuerySchema querySchema = null;
	                for (String fileName : chartData.getFiles()) {
	                    fields = new ArrayList<Field>();
	                    
	                    if(!chartData.getIsQuery()){
	                        fields.addAll(hpccService.getColumns(fileName, chartData.getHpccConnection()));
	                    } else {
	                    	querySchema = hpccQueryService.getQuerySchema(fileName, chartData.getHpccConnection(),
	                    			chartData.isGenericQuery(),chartData.getInputParamQuery());
	                        fields.addAll(querySchema.getFields());
	                    }
	                    
	                    fieldMap.put(fileName, fields);
	                }
	                
	                chartData.setFields(fieldMap);
                }
                
                // Constructing Roxie query input parameters
                if (chartData.getIsQuery()) {
                    LOG.debug(filterHolder);
                    Events.sendEvent(Constants.CREATE_PARAM_EVENT, filterHolder, null);
                }
                
            } catch (Exception e) {
                Clients.showNotification(Labels.getLabel("unableToFetchColumns"), Constants.ERROR_NOTIFICATION, comp,Constants.POSITION_CENTER, 3000, true);
                LOG.error(Constants.EXCEPTION, e);
                return;
            }
        }
        if(fieldMap != null && !fieldMap.isEmpty()){
        	//Setting fields to ChartData
        	chartData.setFields(fieldMap);
        }

       
        
        //removing the second column If chartType is PieChart while changing the chart Type. 
        if (Constants.CATEGORY_PIE == chartService.getCharts().get(portlet.getChartType()).getCategory()) {
            if (chartData.getMeasures().size() > 1) {
                chartData.getMeasures().remove(1);
            }
            chartData.removeGroupAttribute();
        }
        
        // When live chart is present in ChartPanel
        if (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
            Attribute attribute = chartData.getAttribute();
            Attribute groupAttribute = chartData.getGroupAttribute();
            boolean xColumnExist = false;
            boolean groupColumnExist = false;
            for (Map.Entry<String, List<Field>> entry : chartData.getFields().entrySet()) {
                for (Field field : entry.getValue()) {
                    if (attribute.getFileName().equals(entry.getKey()) 
                            && attribute.getColumn().trim().equals(field.getColumnName().trim())) {
                        xColumnExist = true;
                    }
                    if(chartData.isGrouped() &&
                            groupAttribute.getFileName().equals(entry.getKey()) && 
                            groupAttribute.getColumn().equals(field.getColumnName().trim())) {
                        groupColumnExist = true;
                    }
                    if(groupColumnExist && xColumnExist) {
                        break;
                    }
                }
            }

            if (xColumnExist) {
                createAttributeListChild(attribute, false);
            } else {
                chartData.removeAttribute();
            }
            if (groupColumnExist) {
                createAttributeListChild(attribute, true);
            } else {
                chartData.removeGroupAttribute();
            }

            List<String> columnList = new ArrayList<String>();
            
            boolean isScendaryMeasurePresent = false;
            for (Measure measure : chartData.getMeasures()) {
                boolean yColumnExist = false;
                for (Map.Entry<String, List<Field>> entry : chartData.getFields().entrySet()) {
                    for (Field field : entry.getValue()) {
                        if (measure.getFileName().equals(entry.getKey()) 
                                && measure.getColumn().trim().equals(field.getColumnName().trim())) {
                            yColumnExist = true;
                            break;
                        }
                    }
                }
                if (yColumnExist) {
                    if(Constants.NONE.equals(measure.getAggregateFunction())) {
                        yAxisListbox.getParent().setAttribute(Constants.NONE, true);
                    }
                    createYListChild(measure);
                    isScendaryMeasurePresent = !isScendaryMeasurePresent ? measure.isSecondary():true;
                } else {
                    columnList.add(measure.getColumn());
                }
            }
            
            if(isScendaryMeasurePresent) {
                y2AxisListbox.setVisible(true);
                secondAxisCheck.setChecked(true);
                Events.postEvent(Events.ON_CHECK, secondAxisCheck, null);
            }
            
            for (String column : columnList) {
                chartData.getMeasures().remove(column);
            }
            validateDroppable();

            if(chartData.getIsFiltered()) {
                for (Filter filter : chartData.getFilters()) {
                    if(!filter.getIsCommonFilter()){
                        Events.sendEvent(Constants.CREATE_FILTER_EVENT, filterHolder, filter);
                    }
                }
            }

            // Checking to avoid error while on the fly widget type change
            // happens
            if (chartData.getAttribute() != null && !chartData.getMeasures().isEmpty()) {
                constructChart();
            }
        }
        
        //Create Measures and Attributes
        LOG.debug("chartData.getFields() -->"+chartData.getFields());
        UiGenerator.generateTabboxChildren(measureTabbox, attributeTabbox, chartData.getFields(), !chartData.getIsQuery());

        if(LOG.isDebugEnabled()){
            LOG.debug("Portlet object -- " + portlet);
        }
        
    }    
    


    /**
     * Method to render chart when item dropped in Y Axis
     * 
     * @param dropEvent
     * @throws Exception 
     */
    @Listen("onDrop = #yAxisListbox, #y2AxisListbox")
    public void onDropToYAxisListbox(final DropEvent dropEvent) {
        if("yAxisListbox".equals(dropEvent.getDragged().getParent().getId()) ||
                "y2AxisListbox".equals(dropEvent.getDragged().getParent().getId())) {
            processYAxisDropOnListbox(dropEvent, (Listbox) dropEvent.getTarget());
        }
        else{
            Listbox target = (Listbox) dropEvent.getTarget();
            processYAxisDrop(dropEvent, target); 
        }
       
    }

    private void processYAxisDrop(final DropEvent dropEvent, Listbox target) {
        final Vbox targetParent = (Vbox) target.getParent();
        
        final Listitem draggedListitem = (Listitem) ((DropEvent) dropEvent).getDragged();
        Tabpanel tabpanel = (Tabpanel) draggedListitem.getParent().getParent();
                
        String str = Labels.getLabel("dropMeasureOnly");
        
        // Validations
        if(!Constants.DATA_TYPE_NUMERIC.equals(draggedListitem.getAttribute(Constants.COLUMN_DATA_TYPE))){
            Clients.showNotification(str, Constants.ERROR_NOTIFICATION, yAxisListbox, Constants.POSITION_END_CENTER, 3000, true);
            return;
        }
        
        // Creating new instance purposefully
        Measure measure = (Measure) draggedListitem.getAttribute(Constants.MEASURE);
        Measure newMeasure = new Measure(measure.getColumn(), measure.getAggregateFunction());
        newMeasure.setIsSecondary(target == y2AxisListbox);
        newMeasure.setFileName(tabpanel.getLinkedTab().getLabel());
        
        
        // Validations
        if (chartData.getMeasures().contains(newMeasure)
                || chartData.hasAttribute(new Attribute(newMeasure.getColumn()))) {
            Clients.showNotification(Labels.getLabel("droppedColumnAlreadyUsed"), Constants.ERROR_NOTIFICATION,
                    yAxisListbox, Constants.POSITION_END_CENTER, 3000, true);
            return;
        }
        if (chartData.isGrouped() && !chartData.getMeasures().isEmpty()) {
            Clients.showNotification(Labels.getLabel("chartAlreadyGrouped"), Constants.ERROR_NOTIFICATION, yAxisListbox, Constants.POSITION_END_CENTER, 3000, true);
            return;
        }
        if(!chartData.getMeasures().isEmpty() 
                && targetParent.getAttribute(Constants.NONE) != null
                && !Constants.NONE.equals(measure.getAggregateFunction())
                && targetParent.getAttribute(Constants.NONE).equals(true)) {
            Clients.showNotification(Labels.getLabel("aggregationNotAllowed"), Constants.ERROR_NOTIFICATION, yAxisListbox, Constants.POSITION_END_CENTER, 3000, true);
            return;
        }
        
        //Handling No aggregation
        if(Constants.NONE.equals(measure.getAggregateFunction())) {
            if(!chartData.getMeasures().isEmpty() 
                    && targetParent.getAttribute(Constants.NONE) == null) {
                Clients.showNotification(Labels.getLabel("aggregationNotAllowed"), Constants.ERROR_NOTIFICATION, yAxisListbox, Constants.POSITION_END_CENTER, 3000, true);
                return;
            }
            targetParent.setAttribute(Constants.NONE, true);
        }
        
        createYListChild(newMeasure);
        chartData.getMeasures().add(newMeasure);
        if (chartData.isDrawable()) {
            constructChart();
        }
        validateDroppable();
    }

    /**
     * Event listener to fetch data from HPCC and draw the chart
     */
    EventListener<Event> drawChart = new EventListener<Event>() {
        @Override
        public void onEvent(Event event) {
            if(LOG.isDebugEnabled()) {
                LOG.debug("Drawing chart..");
            }
            
            if(chartData.isDrawable()) {
                try {
					chartRenderer.constructChartJSON(chartData, portlet, true);
					chartRenderer.drawChart(Constants.EDIT_WINDOW_CHART_DIV, portlet);
				} catch (Exception e) {
					LOG.error(Constants.EXCEPTION,e);
					Clients.showNotification(Labels.getLabel("couldntRetrieveData"), Constants.ERROR_NOTIFICATION, 
							EditChartController.this.getSelf(), Constants.POSITION_CENTER, 3000, true);
				}
               
            }
            Clients.clearBusy(chart);
        }
    };
    /**
     * Method to process with X/Y column data add/clearance function
     */
    private void constructChart() {
        try {
            // Drawing chart except in API chart configuration flow
            if (authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)) {
                try {
                    Set<Field> columnSet = hpccService.getColumns(chartData.getFiles().get(0),    chartData.getHpccConnection());
                    
                    //Attribute
                    boolean xColumnExist = false;
                    for (Field field : columnSet) {
                        if (chartData.getAttribute().getColumn().equals(field.getColumnName().trim())) {
                            xColumnExist = true;
                            break;
                        }
                    }
                    if (!xColumnExist) {
                        throw new WrongValueException("X Column " + chartData.getAttribute().getColumn() + " not present in Dataset");
                    }
                    
                    //Group Attribute
                    boolean groupColumnExist = false;
                    for (Field field : columnSet) {
                        if (chartData.getGroupAttribute().getColumn().equals(field.getColumnName().trim())) {
                            xColumnExist = true;
                            break;
                        }
                    }
                    if (!groupColumnExist) {
                        throw new WrongValueException("X Column " + chartData.getGroupAttribute().getColumn() + " not present in Dataset");
                    }
                    
                    //Measure
                    for (Measure measure : chartData.getMeasures()) {
                        boolean yColumnExist = false;
                        for (Field field : columnSet) {
                            if (measure.getColumn().trim().equals(field.getColumnName().trim())) {
                                yColumnExist = true;
                                break;
                            }
                        }
                        if (!yColumnExist) {
                            throw new WrongValueException("Y Column "    + measure.getColumn() + " not present in Dataset");
                        }
                    }
                    Events.echoEvent(new Event(Constants.DRAW_CHART_EVENT, this.getSelf()));
                } catch (Exception e) {
                    Clients.showNotification(Labels.getLabel("couldntRetrieveData"), Constants.ERROR_NOTIFICATION, this.getSelf(), Constants.POSITION_CENTER, 3000, true);
                    LOG.error("Chart Rendering failed", e);
                }
            } else {
                Clients.showBusy(chart, "Retriving data");
                Events.echoEvent(new Event(Constants.DRAW_CHART_EVENT, this.getSelf()));
            }
            doneButton.setDisabled(false);
        } catch (Exception ex) {
            Clients.showNotification(
                    Labels.getLabel("unableToFetchHpccData"), Constants.ERROR_NOTIFICATION,
                    this.getSelf(), Constants.POSITION_CENTER, 3000, true);
            LOG.error("Exception while fetching column data from Hpcc", ex);
            return;
        }
    }

    /**
     * Enables/Disables Drops in Y & X axis list boxes 
     * based on conditions from application constants
     * @throws Exception 
     */
    private void validateDroppable() {
        ChartDetails chartDetails = chartService.getCharts().get(portlet.getChartType());
        
        // Measures
        if (Constants.CATEGORY_PIE == chartDetails.getCategory()) {
            if(chartData.getMeasures().size() < 1) {
                yAxisListbox.setDroppable(TRUE);
            } else {
                yAxisListbox.setDroppable(FALSE);
            }
        }
        
        // Attributes
        if(chartData.getAttribute() != null) {
            xAxisListbox.setDroppable(FALSE);
        } else {
            xAxisListbox.setDroppable(TRUE);
        }
 
        // Attributes
        if (((XYConfiguration)chartDetails.getConfiguration()).getEnableXGrouping()) {
            if (chartData.getGroupAttribute() != null) {
                groupListbox.setDroppable(FALSE);
            } else {
                groupListbox.setDroppable(TRUE);
            }
        }
    }

    private void createYListChild(Measure measure) {
        Listitem yAxisItem = new Listitem();
        final Textbox textBox = new Textbox();
        textBox.setInplace(true);
        textBox.setStyle("border: none;    color: black; width: 150px;");
        yAxisItem.setAttribute(Constants.MEASURE, measure);
        yAxisItem.setDraggable(TRUE);
        yAxisItem.setDroppable(TRUE);
        yAxisItem.addEventListener(Events.ON_DROP, yAxisItemSwapListener);
        Listcell listcell = new Listcell();
        //For roxie query, no aggregate function supported
        if(measure.getAggregateFunction() == null){
            textBox.setValue(measure.getColumn());
        }else if (measure.getDisplayYColumnName() == null && Constants.NONE.equals(measure.getAggregateFunction())) {
            textBox.setValue(measure.getColumn());
        } else if(measure.getDisplayYColumnName() == null) {
            textBox.setValue(measure.getColumn() + "_" + measure.getAggregateFunction());
        } else {
            textBox.setValue(measure.getDisplayYColumnName());
        }
        textBox.addEventListener(Events.ON_CHANGE, titleChangeLisnr);
        listcell.appendChild(textBox);
        Button closeBtn = new Button();
        closeBtn.setSclass(Constants.CLOSE_BUTTON_STYLE);
        closeBtn.addEventListener(Events.ON_CLICK, yAxisItemDetachListener);
        listcell.appendChild(closeBtn);
        yAxisItem.appendChild(listcell);
        
        if(measure.isSecondary()){
            yAxisItem.setParent(y2AxisListbox);
        } else {
            yAxisItem.setParent(yAxisListbox);
        }
        
     
        if(Constants.NONE.equals(measure.getAggregateFunction())) {
            yAxisListbox.setAttribute(Constants.NONE, true);
        }
    }
    
    private EventListener<DropEvent> yAxisItemSwapListener = new EventListener<DropEvent>() {
        public void onEvent(final DropEvent event)  {
            
            Listitem dropped = (Listitem) event.getTarget();
            
            if("yAxisListbox".equals(event.getDragged().getParent().getId()) ||
                    "y2AxisListbox".equals(event.getDragged().getParent().getId())) {
                processYAxisDropOnListitem(event,dropped);
            } else {
                processYAxisDrop(event, dropped.getListbox());
            }
         
        }
    };
 
    private void processYAxisDropOnListitem(DropEvent event,Listitem dropped){
        Listitem dragged = (Listitem) event.getDragged();
        Measure measure1 = (Measure) dragged.getAttribute(Constants.MEASURE);
        Measure measure2 = (Measure) dropped.getAttribute(Constants.MEASURE);
        dropped.getListbox().insertBefore(dragged, dropped); 
        
        Collections.swap(chartData.getMeasures(), 
                chartData.getMeasures().indexOf(measure1),
                chartData.getMeasures().indexOf(measure2));
        
      
    }
    
    private void processYAxisDropOnListbox(DropEvent event,Listbox dropped){
        Listitem dragged = (Listitem) event.getDragged();
        Measure measure1 = (Measure) dragged.getAttribute(Constants.MEASURE);
        Measure measure2 = chartData.getMeasures().get(chartData.getMeasures().size() - 1);
        
        Collections.swap(chartData.getMeasures(), 
                chartData.getMeasures().indexOf(measure1),
                chartData.getMeasures().indexOf(measure2));
        
        dropped.appendChild(dragged);
    }

    // Event Listener for Change of YColumn title text
    private EventListener<Event> titleChangeLisnr = new EventListener<Event>() {
        public void onEvent(final Event event) {
            Listitem listItem = (Listitem) event.getTarget().getParent().getParent();
            Measure measure = (Measure) listItem.getAttribute(Constants.MEASURE);
            Textbox textBox = (Textbox) event.getTarget();
            if (LOG.isDebugEnabled()) {
                LOG.debug("YColumn Title is being changed");
            }
            measure.setDisplayYColumnName(textBox.getValue());
        }
    };
    
   
    private EventListener<Event> yAxisItemDetachListener = new EventListener<Event>() {
        public void onEvent(final Event event)  {
            Listitem yAxisItem = (Listitem) event.getTarget().getParent().getParent();
            Component parentComp = yAxisItem.getParent();
           
            Measure measure = (Measure) yAxisItem.getAttribute(Constants.MEASURE);
            if(measure.isSecondary()){
            	secondAxisCheck.setChecked(false);
            }
            yAxisItem.detach();
            chartData.getMeasures().remove(measure);
            if(parentComp.getChildren().size() < 1){
                parentComp.setAttribute(Constants.NONE, null);
            }
            // Only clear the existing chart when no columns are present otherwise recreate the chart
            if (chartData.getMeasures().isEmpty()) {
                Clients.evalJavaScript("clearChart('" + Constants.EDIT_WINDOW_CHART_DIV +  "')");
            } else {
                constructChart();
            }
            validateDroppable();
        }
    };

    /**
     * Method to render chart when item dropped in X Axis
     * @param dropEvent
     * @throws Exception 
     */

    @Listen("onDrop = #xAxisListbox")
    public void onDropToXAxisTabBox(final DropEvent dropEvent)  {

        final Listitem draggedListitem = (Listitem) ((DropEvent) dropEvent).getDragged();
            
        Attribute attribute = new Attribute(draggedListitem.getLabel());
        Tabpanel tabpanel = (Tabpanel) draggedListitem.getParent().getParent();
        attribute.setFileName(tabpanel.getLinkedTab().getLabel());
        
        // Validations
        if (chartData.hasAttribute(attribute)) {
            Clients.showNotification(Labels.getLabel("columnOnlyUsedWhilePlottingGraph"), 
                    Constants.ERROR_NOTIFICATION, xAxisListbox, Constants.POSITION_END_CENTER, 3000, true);
            return;
        }
        if (!Constants.DATA_TYPE_STRING.equals(draggedListitem.getAttribute(Constants.COLUMN_DATA_TYPE))) {
            Clients.showNotification("\"" + draggedListitem.getLabel() + Labels.getLabel("discreteValueError"), 
                    "warning", xAxisListbox, Constants.POSITION_END_CENTER, 5000, true);
        }
        
        createAttributeListChild(attribute, false);
        chartData.setAttribute(attribute);
        if (chartData.isDrawable()) {
            constructChart();
        }
        validateDroppable();
    }

    @Listen("onDrop = #groupListbox")
    public void onDropGroupAttribute(final DropEvent dropEvent) {
        //Measure grouped validation
        if (chartData.hasMultipleMeasures() || chartData.isGrouped()) {
            Clients.showNotification(Labels.getLabel("chartAlreadyGrouped"), Constants.ERROR_NOTIFICATION, 
                    groupListbox, Constants.POSITION_END_CENTER, 3000, true);
            return;
        }
        
        final Listitem draggedListitem = (Listitem) ((DropEvent) dropEvent).getDragged();
        
        Attribute attribute = new Attribute(draggedListitem.getLabel());
        Tabpanel tabpanel = (Tabpanel) draggedListitem.getParent().getParent();
        attribute.setFileName(tabpanel.getLinkedTab().getLabel());
        
        createAttributeListChild(attribute, true);
        chartData.setGroupAttribute(attribute);
        chartData.setIsGrouped(true);
        
        if(chartData.isDrawable()) {
            constructChart();
        }
    }
    
    private void createAttributeListChild(Attribute attribute, boolean isGroupAttribute) {
        final Listitem xAxisItem = new Listitem();
        final Textbox textBox = new Textbox();
        textBox.setInplace(true);
        textBox.setStyle("border: none;    color: black; width: 150px;");
        xAxisItem.setAttribute(Constants.ATTRIBUTE, attribute);
        final Listcell listcell = new Listcell();
        if (attribute.getDisplayName() == null) {
            textBox.setValue(attribute.getColumn());
        } else {
            textBox.setValue(attribute.getDisplayName());
        }
        textBox.addEventListener(Events.ON_CHANGE, xcolumnTitleChangeLisnr);
        listcell.appendChild(textBox);

        Button closeBtn = new Button();
        closeBtn.setSclass(Constants.CLOSE_BUTTON_STYLE);


        listcell.appendChild(closeBtn);

        xAxisItem.appendChild(listcell);
       
        if(isGroupAttribute) {
           xAxisItem.setParent(groupListbox);
           closeBtn.addEventListener(Events.ON_CLICK, remopveGroupAttributeListener);
       } else {
           xAxisItem.setParent(xAxisListbox);
           //Adding attribute to dateformat popup
           dateFormatBox.addAttribute(attribute);
           closeBtn.addEventListener(Events.ON_CLICK, xAxisItemDetachListener);
       }
    }
    
    EventListener<Event> addDateformatListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            Attribute attribute = (Attribute) event.getData();
            for (Component component : xAxisListbox.getChildren()) {
                Listitem listitem = (Listitem) component;
                
                if(attribute.equals(listitem.getAttribute(Constants.ATTRIBUTE))) {
                    Listcell listcell = (Listcell) listitem.getFirstChild();
                    listcell.setIconSclass("glyphicon glyphicon-calendar");
                    
                    constructChart();
                    
                    break;
                }
            }
        }
    };

    // Event Listener for Change of XColumn title text
    private EventListener<Event> xcolumnTitleChangeLisnr = new EventListener<Event>() {
        public void onEvent(final Event event) {
            final Listitem listItem = (Listitem) event.getTarget().getParent().getParent();
            Attribute attribute = (Attribute) listItem.getAttribute(Constants.ATTRIBUTE);
            Textbox textBox = (Textbox) event.getTarget();
            if (LOG.isDebugEnabled()) {
                LOG.debug("XColumn Title is being changed");
            }
            attribute.setDisplayName(textBox.getValue());
        }
    };

    private EventListener<Event> xAxisItemDetachListener = new EventListener<Event>() {
        public void onEvent(final Event event) {
            Listitem xAxisItem = (Listitem) event.getTarget().getParent().getParent();
            Attribute attribute = (Attribute) xAxisItem.getAttribute(Constants.ATTRIBUTE);
            
            xAxisItem.detach();
            chartData.removeAttribute();
            
            //Removing attribute from Dateformatbox
            dateFormatBox.removeAttribute(attribute);
            
            doneButton.setDisabled(true);
            
            Clients.evalJavaScript("clearChart('" + Constants.EDIT_WINDOW_CHART_DIV + "')");
            
            validateDroppable();
        }
    };
    
    private EventListener<Event> remopveGroupAttributeListener = new EventListener<Event>() {
        public void onEvent(final Event event) {
            Listitem xAxisItem = (Listitem) event.getTarget().getParent().getParent();
            
            xAxisItem.detach();
            chartData.removeGroupAttribute();
            chartData.setIsGrouped(false); 
            
            Clients.evalJavaScript("clearChart('" + Constants.EDIT_WINDOW_CHART_DIV + "')"); 
            validateDroppable(); 
            if (chartData.getAttribute() != null && !chartData.getMeasures().isEmpty()) { 
                constructChart(); 
            }	
        }
    };
    
    @Listen("onClick = #minMaxSaveBtn")
    public void onSaveYAxisMinMaxValue(Event event){
    	if(checkNullEmpty(yAxisMinVal.getValue())){
    		chartData.setyAxisMinVal(new BigDecimal(yAxisMinVal.getValue()));
    	}else{
    	    chartData.setyAxisMinVal(null);
    	}
    	if(checkNullEmpty(yAxisMaxVal.getValue())){
    		chartData.setyAxisMaxVal(new BigDecimal(yAxisMaxVal.getValue()));
    	}else{
    	    chartData.setyAxisMaxVal(null);
    	}
    	if(checkNullEmpty(y2AxisMinVal.getValue())){
    	    chartData.setY2AxisMinVal(new BigDecimal(y2AxisMinVal.getValue()));
    	}else{
    	    chartData.setY2AxisMinVal(null);
    	}
    	if(checkNullEmpty(y2AxisMaxVal.getValue())){
    	    chartData.setY2AxisMaxVal(new BigDecimal(y2AxisMaxVal.getValue()));
    	}else{
    	    chartData.setY2AxisMaxVal(null);
    	}
    	if(checkNullEmpty(yAxisThresholdMin.getValue())){
            chartData.setyThresholdValMin(yAxisThresholdMin.getValue());
        }else{
            chartData.setyThresholdValMin(null);
        }
    	if(checkNullEmpty(yAxisThresholdMax.getValue())){
            chartData.setyThresholdValMax(yAxisThresholdMax.getValue());
        }else{
            chartData.setyThresholdValMax(null);
        }
    	if(checkNullEmpty(y2AxisThresholdMin.getValue())){
            chartData.setY2ThresholdValMin(y2AxisThresholdMin.getValue());
        }else{
            chartData.setY2ThresholdValMin(null);
        }
    	if(checkNullEmpty(y2AxisThresholdMax.getValue())){
            chartData.setY2ThresholdVaMaxl(y2AxisThresholdMax.getValue());
        }else{
            chartData.setY2ThresholdVaMaxl(null);
        }
    	if (chartData.isDrawable()) {
            constructChart();
        }  	
    	minMaxPopup.close();
    }

    private boolean checkNullEmpty(Object obj) {
        if(obj != null && !obj.toString().isEmpty()){
            return true;
        }
        return false;
    }

    public boolean isQueryDataSource() {
        return queryDataSource;
    }
    public void setQueryDataSource(boolean isQuery) {
        this.queryDataSource = isQuery;
    }
    
    @Listen("onCheck = #secondAxisCheck")
    public void onCheckSecondaryAxis() {
        if(secondAxisCheck.isChecked()) {
            y2MinMax.setVisible(true);
            y2MinMax.setVisible(true);
            y2AxisListbox.setVisible(true);
            measureContainer.invalidate();
        } else {
            if(!y2AxisListbox.getChildren().isEmpty()) {
                for (Component component : y2AxisListbox.getChildren()) {
                    if(component instanceof Listitem) {
                        Button closeBtn = (Button) component.getFirstChild().getLastChild();
                        Events.postEvent(Events.ON_CLICK, closeBtn, null);
                    }
                }
            }
            y2AxisThresholdMin.setValue(null);
            y2AxisThresholdMax.setValue(null);
            y2AxisMinVal.setValue(null);
            y2AxisMaxVal.setValue(null);
            y2MinMax.setVisible(false);
            y2AxisListbox.setVisible(false);
            y2AxisThresholdMin.setVisible(false);
            y2AxisThresholdMax.setVisible(false);
            measureContainer.invalidate();
        }
    }
    
    @Listen("onCheck = #rotateAxis")
    public void onCheckHorizontalBar(){
    	 if(rotateAxis.isChecked()) {
    		 chartData.setIsAxisrotated(true);
    	 }else{
    		 chartData.setIsAxisrotated(false);
    	 }
    }
    
    @Listen("onCheck = #secondAxisHideCheck")
    public void onCheckSecondAxisHideCheck(){
        
        if(secondAxisHideCheck.isChecked()) {
            chartData.setHideY2Axis(true);
        }else{
            chartData.setHideY2Axis(false);
        }
    }
    
    
}

