package org.hpccsystems.dashboard.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.cluster.ClusterData;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.QuerySchema;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.util.UiGenerator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Vbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditClusterController extends SelectorComposer<Hbox> {

	
	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(EditClusterController.class);
	
	@WireVariable
    private HPCCService hpccService;
    @WireVariable
    private ChartRenderer chartRenderer;
    @WireVariable
    private HPCCQueryService hpccQueryService;
    @Wire
    private Tabbox filesTabbox;
    @Wire
    private Listbox uniqueIdentifier;
    @Wire
    private Listbox categoryColumn;
    @Wire
    private Listbox nodeDetails;
    @Wire
    private Listbox relationColumn;
    @Wire
    private Div chart;
    
    @Wire
    private Include filterHolder;
    
    private ClusterData chartData;
	private Portlet portlet;
	private Button doneButton;
	private boolean queryDataSource;

    private EventListener<Event> drawChartListener = new EventListener<Event>() {

        @Override
        public void onEvent(Event event) throws Exception {
            chartRenderer.constructClusterJSON(chartData, portlet, true);
            chartRenderer.drawChart(Constants.EDIT_WINDOW_CHART_DIV, portlet);
            Clients.clearBusy(chart);
            doneButton.setDisabled(false);
        }
    };
	    
	@Override
	public void doBeforeComposeChildren(Hbox comp) throws Exception {
		super.doBeforeComposeChildren(comp);
		
		Execution execution = Executions.getCurrent();
      
        chartData = (ClusterData) execution.getAttribute(Constants.CHART_DATA);
        portlet = (Portlet) execution.getAttribute(Constants.PORTLET);
        doneButton = (Button) execution.getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
        
        queryDataSource =chartData.getIsQuery();
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("isQuery - " + chartData.getIsQuery());
        }
	}
	 
	@Override
	public void doAfterCompose(Hbox comp) throws Exception {
		super.doAfterCompose(comp);
		
        //Setting params for filter include
        filterHolder.setDynamicProperty(Constants.BUSY_COMPONENT, chart);
        filterHolder.setDynamicProperty(Constants.PARENT, this.getSelf());
        if (chartData.getIsQuery()) {
            filterHolder.setSrc("layout/input_parameters.zul");
        } else {
            filterHolder.setSrc("layout/filter.zul");
        }
		
        if(chartData.getFields() == null){
        	  Map<String, List<Field>> fieldMap = null;
        	 try {
                 fieldMap = new LinkedHashMap<String, List<Field>>();
                 List<Field> fields;
                 QuerySchema querySchema = null;
                 for (String fileName : chartData.getFiles()) {
                     fields = new ArrayList<Field>();
                     if (!chartData.getIsQuery()) {
                         fields.addAll(hpccService.getColumns(fileName, chartData.getHpccConnection()));
                     } else {
                    	 querySchema = hpccQueryService.getQuerySchema(fileName, chartData.getHpccConnection(),
                    			 chartData.isGenericQuery(),chartData.getInputParamQuery());
                         fields.addAll(querySchema.getFields());
                     }
                     fieldMap.put(fileName, fields);
                 }
                 // Setting fields to ChartData
                 chartData.setFields(fieldMap);
             } catch (Exception e) {
                 Clients.showNotification(Labels.getLabel("unableToFetchColumns"), "error", comp, "middle_center", 3000,
                         true);
                 LOG.error(Constants.EXCEPTION, e);
                 return;
             }
        }            

        this.getSelf().addEventListener("onDrawChart", drawChartListener);

       
        UiGenerator.generateTabboxChildren(chartData, filesTabbox);
        
        // Constructing Roxie query input parameters
        if (chartData.getIsQuery()) {
            Events.sendEvent(Constants.CREATE_PARAM_EVENT, filterHolder, null);
        }
        
        //Recreating from values and chart from persistence
        if(isChartDataComplete()) {
            uniqueIdentifier.appendChild(createAttributeItem(chartData.getId()));
            relationColumn.appendChild(createAttributeItem(chartData.getRelation()));
            categoryColumn.appendChild(createAttributeItem(chartData.getCategory()));
            
            for (Attribute attribute : chartData.getDetails()) {
                nodeDetails.appendChild(createDetailsAttributeItem(attribute));
            }
            
            if(chartData.getIsFiltered()) {
                for (Filter filter : chartData.getFilters()) {
                    if(!filter.getIsCommonFilter()){
                        Events.sendEvent(Constants.CREATE_FILTER_EVENT, filterHolder, filter);
                    }
                }
            }
            
            drawchart();
        } 
	}
	
	@Listen("onDrop = #uniqueIdentifier, #categoryColumn")
	public void onDropUniqueIdentifier(DropEvent event) {
		Listbox target = (Listbox) event.getTarget();
		
		final Listitem draggedListitem = (Listitem) ((DropEvent) event).getDragged();
        Tabpanel tabpanel = (Tabpanel) draggedListitem.getParent().getParent();
        String fileName = tabpanel.getLinkedTab().getLabel();
        String columnName = draggedListitem.getLabel();
        
        Attribute attribute = new Attribute();
        attribute.setColumn(columnName);
        attribute.setFileName(fileName);
        if("uniqueIdentifier".equals(target.getId())){
        	chartData.setId(attribute);
        } else if ("relationColumn".equals(target.getId())) {
        	chartData.setRelation(attribute);
        } else if("categoryColumn".equals(target.getId())) {
        	chartData.setCategory(attribute);
        }
       
        target.appendChild(createAttributeItem(attribute));
        
        target.setDroppable("false");
        
        drawchart();
        
	}

	@Listen("onDrop = #relationColumn")
	public void onDropRelation(DropEvent event) {
	    final Listitem draggedListitem = (Listitem) ((DropEvent) event).getDragged();
	    Field field = (Field) draggedListitem.getAttribute(Constants.FIELD);
	    
	    if(field.isDatasetField()) {
	        Tabpanel tabpanel = (Tabpanel) draggedListitem.getParent().getParent();
	        Listbox target = (Listbox) event.getTarget();
	        
	        String fileName = tabpanel.getLinkedTab().getLabel();
	        String columnName = draggedListitem.getLabel();
	        
	        Attribute attribute = new Attribute();
	        attribute.setColumn(columnName);
	        attribute.setFileName(fileName);
	        
	        chartData.setRelation(attribute);
	        
	        target.appendChild(createNestedAttributeItem(attribute, field));
	        
	        target.setDroppable("false");
	        
	        if(LOG.isDebugEnabled()) {
	            LOG.debug("Relation - " + chartData.getRelation().getChildren());
	        }
	        drawchart();
	    } else {
	        onDropUniqueIdentifier(event);
	    }
	}
	
	private Listitem createAttributeItem(Attribute attribute) {
	    Listcell listcell = new Listcell(attribute.getColumn());
        Button closeBtn = new Button();
        closeBtn.setSclass(Constants.CLOSE_BUTTON_STYLE);
        closeBtn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                Listitem listitem = (Listitem) event.getTarget().getParent().getParent();
                Listbox listbox = (Listbox) listitem.getParent();
                
                listitem.detach();
                
                listbox.setDroppable("true");
            }
        });
        listcell.appendChild(closeBtn);
        Listitem listitem = new Listitem();
        listitem.appendChild(listcell);
        
        return listitem;
	}
	
	private Listitem createNestedAttributeItem(Attribute attribute, Field field) {
	    Listitem listitem = createAttributeItem(attribute);
	    Listcell listcell =  (Listcell) listitem.getFirstChild();
	    
	    List<Attribute> attributes = new ArrayList<Attribute>();
	    Attribute idAttribute = new Attribute(field.getChildren().get(0).getColumnName());
	    attributes.add(idAttribute);
	    Attribute relationAttribute = new Attribute(field.getChildren().get(1).getColumnName());
	    attributes.add(relationAttribute);
	    attribute.setChildren(attributes);
	    
	    Vbox vbox = new Vbox();
	    vbox.appendChild(new Label("ID: " + idAttribute.getDisplayName()));
	    vbox.appendChild(new Label("Type: " + relationAttribute.getDisplayName()));
	    listcell.appendChild(vbox);
	    
	    return listitem;
	}
	
	@Listen("onDrop = #nodeDetails")
	public void onDropNodeDetails(DropEvent event){
		Listbox target = (Listbox) event.getTarget();
		
		final Listitem draggedListitem = (Listitem) ((DropEvent) event).getDragged();
        Tabpanel tabpanel = (Tabpanel) draggedListitem.getParent().getParent();
        String fileName = tabpanel.getLinkedTab().getLabel();
        String columnName = draggedListitem.getLabel();
        
        Attribute attribute = new Attribute();
        attribute.setColumn(columnName);
        attribute.setFileName(fileName);
        if(chartData.getDetails() == null){
        	chartData.setDetails(new ArrayList<Attribute>());
        }
        chartData.getDetails().add(attribute);
        
        target.appendChild(createDetailsAttributeItem(attribute));
        drawchart();
	}
	
	private Listitem createDetailsAttributeItem(Attribute attribute) {
	    Listitem listitem = new Listitem();
        listitem.setAttribute(Constants.ATTRIBUTE, attribute);
        Listcell listcell = new Listcell(attribute.getColumn());
        Button closeBtn = new Button();
        closeBtn.setSclass(Constants.CLOSE_BUTTON_STYLE);
        closeBtn.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                Listitem listitem = (Listitem) event.getTarget().getParent().getParent();
                listitem.detach();
                
                Attribute attribute = (Attribute) listitem.getAttribute(Constants.ATTRIBUTE);
                chartData.getDetails().remove(attribute);
            }
        });
        listcell.appendChild(closeBtn);
        
        listitem.appendChild(listcell);
        
        return listitem;
	}
	
	@Listen("onDrop = #chartFilter")
	public void onDropChartFilter(DropEvent event){
		
	}
	
	private void drawchart(){
		if(isChartDataComplete()){
			Clients.showBusy(chart, "Retrieving data");
			Events.echoEvent("onDrawChart", this.getSelf(), null);
		}
	}
	
	private boolean isChartDataComplete() {
	    return chartData.getCategory() != null 
	            && chartData.getId() != null
                && chartData.getRelation() != null 
                && chartData.getDetails() != null 
                && !chartData.getDetails().isEmpty();
	}
	
    public boolean getQueryDataSource() {
        return queryDataSource;
	}
}
