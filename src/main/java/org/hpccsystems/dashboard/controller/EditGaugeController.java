package org.hpccsystems.dashboard.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.entity.Measure;
import org.hpccsystems.dashboard.chart.gauge.GaugeChartData;
import org.hpccsystems.dashboard.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.util.UiGenerator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.CheckEvent;
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
import org.zkoss.zul.Include;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Vlayout;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditGaugeController extends SelectorComposer<Vlayout>{
    private static final long serialVersionUID = 1L;
    private static final Log LOG = LogFactory.getLog(EditGaugeController.class);
    
    @WireVariable
    private AuthenticationService authenticationService;
    @WireVariable
    private HPCCService hpccService;
    @WireVariable
    private HPCCQueryService hpccQueryService;
    @WireVariable
    private ChartRenderer chartRenderer;
    
    @Wire
    private Tabbox measureTabbox;
    @Wire
    private Tabbox attributeTabbox;
    @Wire
    private Div chart;
    
    @Wire
    private Listbox valueMeasuresListbox;
    @Wire
    private Listbox totalMeasuresListbox;
    @Wire
    private Listbox attributesListbox;
    @Wire
    private Checkbox percentageCheck;
    
    @Wire
    private Include filterHolder;
    
    private static final String EVENT_ON_DRAW_CHART = "onDrawChart";
    
    private boolean isAPIFlow;
    private boolean queryDataSource;
    
    private GaugeChartData chartData;
    private Button doneButton;
    private Portlet portlet;
    
    private EventListener<Event> drawChartListener = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
            chartRenderer.constructGaugeJSON(chartData, portlet, true);
            chartRenderer.drawChart(Constants.EDIT_WINDOW_CHART_DIV, portlet);
            Clients.clearBusy(chart);
        }
    }; 
    
    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        Execution execution = Executions.getCurrent();
        chartData =  (GaugeChartData) execution.getAttribute(Constants.CHART_DATA);
        portlet = (Portlet) execution.getAttribute(Constants.PORTLET);
        doneButton = (Button) execution.getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
        
        setQueryDataSource(chartData.getIsQuery());
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("isQuery - " + chartData.getIsQuery());
        }
                
        return super.doBeforeCompose(page, parent, compInfo);
    }
    
    @Override
    public void doAfterCompose(Vlayout comp) throws Exception {
        super.doAfterCompose(comp);
        
        this.getSelf().addEventListener(EVENT_ON_DRAW_CHART, drawChartListener);
        
        isAPIFlow = authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART);
        
        //Setting params for filter include
        filterHolder.setDynamicProperty(Constants.BUSY_COMPONENT, chart);
        filterHolder.setDynamicProperty(Constants.PARENT, this.getSelf());
        if (chartData.getIsQuery()) {
            filterHolder.setSrc("layout/input_parameters.zul");
        } else {
            filterHolder.setSrc("layout/filter.zul");
        }
                
        //Get Fields from data source
        Map<String, List<Field>> fieldMap = new LinkedHashMap<String, List<Field>>();
        if(isAPIFlow) {
            
        } else {
        	 if(chartData.getFields() == null){
        		 List<Field> fields;
        		 QuerySchema querySchema = null;
                 for (String fileName : chartData.getFiles()) {
                     fields = new ArrayList<Field>();
                     if(isQueryDataSource()) {
                    	 querySchema = hpccQueryService.getQuerySchema(fileName, chartData.getHpccConnection(),
                    			 chartData.isGenericQuery(), chartData.getInputParamQuery());
                         fields.addAll(querySchema.getFields());
                     } else {
                         fields.addAll(hpccService.getColumns(fileName, chartData.getHpccConnection()));
                     }
                     fieldMap.put(fileName, fields);
                 }
        	 }
        	           
        }

        if(fieldMap != null && !fieldMap.isEmpty()){
        	//Setting fields to ChartData
        	chartData.setFields(fieldMap);
        }
        
        // Create Measures and Attributes
        UiGenerator.generateTabboxChildren(measureTabbox, attributeTabbox, chartData.getFields(), !chartData.getIsQuery());
        
        //Creating input parameters
        if (chartData.getIsQuery()) {
            Events.sendEvent(Constants.CREATE_PARAM_EVENT, filterHolder, null);
        }
        
        //Recreate dropped attributes and measures for live chart
        if(isChartDataComplete()) {
            valueMeasuresListbox.appendChild(createMeasureItem(chartData.getValue(), false));
            attributesListbox.appendChild(createAttributeItem(chartData.getAttribute()));
            
            if(chartData.getIsTotalRequired()) {
                totalMeasuresListbox.appendChild(createMeasureItem(chartData.getTotal(), false));
            } else {
                percentageCheck.setChecked(true);
            }
            
            if(chartData.getIsFiltered()) {
                for (Filter filter : chartData.getFilters()) {
                    if(!filter.getIsCommonFilter()){
                        Events.sendEvent(Constants.CREATE_FILTER_EVENT, filterHolder, filter);
                    }
                }
            }
            
            drawChart();
        }
    }

    @Listen("onCheck = #percentageCheck")
    public void onCkeckPercentage(CheckEvent event) {
        Checkbox checkbox = (Checkbox) event.getTarget();
        chartData.setIsTotalRequired(!checkbox.isChecked());
        
        drawChart();
    }
     
    @Listen("onDrop = #totalMeasuresListbox, #valueMeasuresListbox")
    public void onDropTotalMeasuresListbox(DropEvent event) {
        Listbox target = (Listbox) event.getTarget();
        
        final Listitem draggedListitem = (Listitem) ((DropEvent) event).getDragged();
        Tabpanel tabpanel = (Tabpanel) draggedListitem.getParent().getParent();
        
        // Creating new instance purposefully
        Measure measure = (Measure) draggedListitem.getAttribute(Constants.MEASURE);
        if(measure != null && measure instanceof Measure){
            Measure newMeasure = new Measure(measure.getColumn(), measure.getAggregateFunction());
            newMeasure.setFileName(tabpanel.getLinkedTab().getLabel());
            
            if("totalMeasuresListbox".equals(target.getId())){
                chartData.setTotal(newMeasure);
                target.appendChild(createMeasureItem(newMeasure, true));
            } else {
                chartData.setValue(newMeasure);
                target.appendChild(createMeasureItem(newMeasure, false));
            }
            
            target.setDroppable("false");
            
            drawChart();
        }else{
            Clients.showNotification(Labels.getLabel("dropMeasureOnly"), Constants.ERROR_NOTIFICATION, target, Constants.POSITION_END_CENTER, 3000, true);
            return;
        }
       
    }
    
    
    @Listen("onDrop = #attributesListbox")
    public void onDropAttributesListbox(DropEvent event) {
        Listbox target = (Listbox) event.getTarget();
        
        final Listitem draggedListitem = (Listitem) ((DropEvent) event).getDragged();
        Tabpanel tabpanel = (Tabpanel) draggedListitem.getParent().getParent();
        
        Attribute attribute = new Attribute(draggedListitem.getLabel());
        attribute.setFileName(tabpanel.getLinkedTab().getLabel());
        
        chartData.setAttribute(attribute);
        target.appendChild(createAttributeItem(attribute));
        
        target.setDroppable("false");
        
        drawChart();
    }
    
    private Listitem createMeasureItem(Measure measure, final boolean isTotal) {
        Listitem listitem = new Listitem();
        listitem.setAttribute(Constants.MEASURE, measure);
        
        Listcell listcell;
        if(Constants.NONE.equals(measure.getAggregateFunction())) {
            listcell = new Listcell(measure.getColumn());
        } else {
            listcell = new Listcell(new StringBuilder(
                    measure.getColumn())
                    .append("_")
                    .append(measure.getAggregateFunction())
                    .toString());
        }
        Button closeBtn = new Button();
        closeBtn.setSclass(Constants.CLOSE_BUTTON_STYLE);
        closeBtn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                Listitem listitem = (Listitem) event.getTarget().getParent().getParent();
                Listbox listbox = (Listbox) listitem.getParent();
                
                listitem.detach();
                
                if(isTotal) {
                    chartData.setTotal(null);
                } else {
                    chartData.setValue(null);
                }
                listbox.setDroppable("true");
            }
        });
        listcell.appendChild(closeBtn);
        
        listitem.appendChild(listcell);
        
        return listitem;
    }
    
    private Listitem createAttributeItem(Attribute attribute) {
        Listitem listitem = new Listitem();
        listitem.setAttribute(Constants.ATTRIBUTE, attribute);
        
        Listcell listcell;
        listcell = new Listcell(attribute.getColumn());
        
        Button closeBtn = new Button();
        closeBtn.setSclass(Constants.CLOSE_BUTTON_STYLE);
        closeBtn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                Listitem listitem = (Listitem) event.getTarget().getParent().getParent();
                Listbox listbox = (Listbox) listitem.getParent();
                
                listitem.detach();
                
                chartData.setAttribute(null);
                listbox.setDroppable("true");
            }
        });
        listcell.appendChild(closeBtn);
        
        listitem.appendChild(listcell);
        
        return listitem;
    }
    
    private void drawChart() {
        if(isChartDataComplete()) {
            Clients.showBusy(chart, "Retriving data");
            Events.echoEvent(EVENT_ON_DRAW_CHART, this.getSelf(), null);
            
            doneButton.setDisabled(false);
        }
    }
    
    private boolean isChartDataComplete() {
        return chartData.getAttribute() != null 
                && chartData.getValue() != null 
                && (!chartData.getIsTotalRequired() || chartData.getTotal() != null);
    }
        
    public boolean isQueryDataSource() {
        return queryDataSource;
    }
    public void setQueryDataSource(boolean isQuery) {
        this.queryDataSource = isQuery;
    }
}
