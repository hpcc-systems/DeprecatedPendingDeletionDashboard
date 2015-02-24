package org.hpccsystems.dashboard.controller;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.Attribute;
import org.hpccsystems.dashboard.chart.entity.Interactivity;
import org.hpccsystems.dashboard.chart.entity.RelevantData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.ChartPanel;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.HPCCQueryService;
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
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
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
    
    @WireVariable
    private ChartService chartService;
    @WireVariable
    private HPCCQueryService hpccQueryService;
    
    private  Dashboard dashboard;
    
    
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
       
       targetListModel.addAll(inputParameter);
       targetListBox.setModel(targetListModel);
       targetListBox.setItemRenderer((listitem,inputParam,index) -> {
            listitem.setLabel(inputParam.toString());
        });
    
    };
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        dashboard = (Dashboard) Executions.getCurrent().getArg().get(Constants.DASHBOARD);
        
        ListModelList<Portlet> sourceModel = new ListModelList<Portlet>();
        ListModelList<Portlet> targetModel = new ListModelList<Portlet>();
        LOG.debug("list -->"+ dashboard);
            dashboard.getLiveCharts().stream().forEach(portlet ->{
                if(Constants.CATEGORY_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    sourceModel.add(portlet);
                }else if(Constants.RELEVANT_CONFIG == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                    targetModel.add(portlet);
                }
            });
            if(!sourceModel.isEmpty() && !targetModel.isEmpty()){
                sourceCombobox.setModel(sourceModel);
                sourceCombobox.setItemRenderer((comboitem,widgetPassed,index)->{
                    Portlet widget =(Portlet)widgetPassed;
                    comboitem.setLabel(widget.getName());
                    comboitem.setValue(widget);
                    sourceCombobox.addEventListener(Events.ON_CHANGE, populateTableAttributes);
                });
                
                
                targetCombobox.setModel(targetModel);
                targetCombobox.setItemRenderer((comboitem,widgetPassed,index)->{
                    Portlet widget =(Portlet)widgetPassed;
                    comboitem.setLabel(widget.getName());
                    comboitem.setValue(widget);
                    comboitem.addEventListener(Events.ON_SELECT, populateRelevantAttributes);
                });
            }else {
        	Clients.showNotification(
        			 Labels.getLabel("configureCharts"),
                    Clients.NOTIFICATION_TYPE_INFO, comp, Constants.POSITION_CENTER, 3000, true);
        	}
    }
    
    @Listen("onClick=#addInteractivity")
    public void click() {

    	Interactivity interactivity=new Interactivity();
    	Portlet portlet=(Portlet) ((ListModelList)sourceCombobox.getModel()).getSelection().iterator().next();
    	interactivity.setSourceId(portlet.getId());
    	portlet=(Portlet) ((ListModelList)targetCombobox.getModel()).getSelection().iterator().next();
    	interactivity.setTargetId(portlet.getId());
    	interactivity.setSourceColumn(sourceListBox.getSelectedItem().getValue());
    	interactivity.setTragetColumn(targetListBox.getSelectedItem().getLabel());
    	
    	//to set interactivity to tabledata
    	
   	}
    
}
