package org.hpccsystems.dashboard.controller;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.Interactivity;
import org.hpccsystems.dashboard.chart.entity.RelevantData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.HPCCQueryService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Window;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class InteractivityController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final  Log LOG = LogFactory.getLog(InteractivityController.class);
    
    @Wire
    private Listbox sourceListBox;
    @Wire
    private Listbox targetListBox;
    @Wire
    private Combobox targetCombobox;
    @Wire
    private Combobox sourceCombobox;
    @Wire
    private Listbox interactivityListbox;
    @Wire
    private Hbox hbox;
    
    @WireVariable
    private ChartService chartService;
    @WireVariable
    private HPCCQueryService hpccQueryService;
    @WireVariable
    private WidgetService widgetService;
    
    private  Dashboard dashboard;
    private Window parent;
    
    
    
    EventListener<Event> populateTableAttributes = (event) -> {
        sourceListBox.setVisible(true);
        Comboitem selectedItem = sourceCombobox.getSelectedItem();
        Portlet selectedTable =selectedItem.getValue();
        ListModelList<Attribute> sourceListModel = new ListModelList<Attribute>();
        TableData tableData = (TableData) selectedTable.getChartData();
        sourceListModel.addAll(tableData.getAttributes());
        sourceListBox.setModel(sourceListModel);
        sourceListBox.setItemRenderer((listitem,attribute,index) -> {
            listitem.setLabel(((Attribute)attribute).getColumn());
        });
    };
    
   
    EventListener<Event> populateRelevantAttributes = (event) -> {
        
        targetListBox.setVisible(true);
        Comboitem selectedItem = targetCombobox.getSelectedItem();
        Portlet selectedTable =selectedItem.getValue();
        ListModelList<String> targetListModel = new ListModelList<String>();
        RelevantData relevantData = (RelevantData) selectedTable.getChartData();
        
       Set<String> inputParameter = hpccQueryService.getInputParameters(relevantData
                .getFiles().iterator().next(),relevantData.getHpccConnection(),
                relevantData.isGenericQuery(),relevantData.getInputParamQuery());
       LOG.debug("inputParameters -->"+inputParameter);
       
       targetListModel.addAll(inputParameter);
       targetListBox.setModel(targetListModel);
       targetListBox.setItemRenderer((listitem,inputParam,index) -> {
            listitem.setLabel(inputParam.toString());
        });
    };
    
    ListitemRenderer<Portlet> interactivityLabelRenderer = (listitem,portlet,index) ->{
        StringBuilder bilder = new StringBuilder();
        
        bilder.append(sourceCombobox.getSelectedItem().getLabel())
        .append("'s column ")
        .append(sourceListBox.getSelectedItem().getLabel())
        .append(" updates ")
        .append(targetCombobox.getSelectedItem().getLabel());
        
        listitem.setLabel(bilder.toString());
        listitem.setValue(portlet);
        
    };
     
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        dashboard = (Dashboard) Executions.getCurrent().getArg().get(Constants.DASHBOARD);
        parent = (Window) this.getSelf().getParent();
        
        ListModelList<Portlet> sourceModel = new ListModelList<Portlet>();
        ListModelList<Portlet> targetModel = new ListModelList<Portlet>();
      
            dashboard.getLiveCharts().stream().forEach(portlet ->{
                if(Constants.CATEGORY_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    sourceModel.add(portlet);
                }else if(Constants.RELEVANT_CONFIG == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    targetModel.add(portlet);
                }
            });
            
            LOG.debug("sourceModel -->"+ sourceModel.getSize());
            LOG.debug("targetModel -->"+ targetModel.getSize());
            
            if(!sourceModel.isEmpty() && !targetModel.isEmpty()){
                sourceCombobox.setModel(sourceModel);
                sourceCombobox.setItemRenderer((comboitem,widgetPassed,index)->{
                    Portlet widget =(Portlet)widgetPassed;
                    if(widget.getName().isEmpty()){
                    	comboitem.setLabel("Table - ".concat(widget.getId().toString()));
                    } else {
                    	comboitem.setLabel(widget.getName());
                    }
                    comboitem.setValue(widget);
                    sourceCombobox.addEventListener(Events.ON_CHANGE, populateTableAttributes);
                });
                
                
                targetCombobox.setModel(targetModel);
                targetCombobox.setItemRenderer((comboitem,widgetPassed,index)->{
                    Portlet widget =(Portlet)widgetPassed;
                    if(widget.getName().isEmpty()){
                    	comboitem.setLabel("Relavent - ".concat(widget.getId().toString()));
                    } else {
                    	comboitem.setLabel(widget.getName());
                    }
                    comboitem.setValue(widget);
                    targetCombobox.addEventListener(Events.ON_CHANGE, populateRelevantAttributes);
                });
            }else{
                Clients.showNotification(
                        Labels.getLabel("configureCharts"),
                       Clients.NOTIFICATION_TYPE_INFO, comp, Constants.POSITION_CENTER, 3000, true);
            }
            interactivityListbox.setModel(new ListModelList<Portlet>());
            interactivityListbox.setItemRenderer(interactivityLabelRenderer);
    }
    
    @Listen("onClick=#addInteractivity")
    public void click() {
        
        if(sourceListBox.getSelectedItem() == null || targetListBox.getSelectedItem() == null){
            Clients.showNotification(
                    Labels.getLabel("selectColumns"),
                   Clients.NOTIFICATION_TYPE_ERROR, hbox, Constants.POSITION_CENTER, 3000, true);
            return;
        }
        Interactivity interactivity=new Interactivity();
        Portlet sourceportlet=(Portlet) ((ListModelList<Object>)sourceCombobox.getModel()).getSelection().iterator().next();
        interactivity.setSourceId(sourceportlet.getId());
        
        Portlet targetportlet=(Portlet) ((ListModelList<Object>)targetCombobox.getModel()).getSelection().iterator().next();
        interactivity.setTargetId(targetportlet.getId());
        
        interactivity.setSourceColumn(sourceListBox.getSelectedItem().getLabel());
        interactivity.setTragetColumn(targetListBox.getSelectedItem().getLabel());
        
        TableData tableData=(TableData) sourceportlet.getChartData();
        tableData.setHasInteractivity(true);
        tableData.setInteractivity(interactivity);
        
        //To display the selected interactivity label
        ((ListModelList<Object>)interactivityListbox.getModel()).add(sourceportlet);
    }
    
    @Listen("onClick = #saveBtn")
    public void onClickSave(){
        
        List<Object> slectedPortlets =((ListModelList<Object>)interactivityListbox.getModel()).getInnerList();
        LOG.debug("selectedTables -->"+slectedPortlets);
        
        //Update tabledata with interactivity details into DB 
        ((ListModelList<Object>)interactivityListbox.getModel()).getInnerList().forEach(portlet ->{
            try {
                widgetService.updateWidget((Portlet)portlet);
            } catch (Exception e) {
               LOG.error(Constants.EXCEPTION,e);
               Clients.showNotification(
                       Labels.getLabel("unableToUpdateInteractivityInfo"),
                      Clients.NOTIFICATION_TYPE_INFO, this.getSelf(), Constants.POSITION_CENTER, 3000, true);
            }
        });
        
        Events.postEvent(Constants.ON_SAVE_INTERACTIVITY, parent, slectedPortlets);
        
       this.getSelf().detach();
    }
}
