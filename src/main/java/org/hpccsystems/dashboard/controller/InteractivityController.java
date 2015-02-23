package org.hpccsystems.dashboard.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.RelevantData;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.ChartService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Combobox;
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
    
    private  Dashboard dashboard;
    
    EventListener<Event> populateTableAttributes = (event) -> {
        
    };
    
    EventListener<Event> populateRelevantAttributes = (event) -> {
        
    };
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        dashboard = (Dashboard) Executions.getCurrent().getAttribute(Constants.DASHBOARD);
        
        ListModelList<Portlet> sourceModel = new ListModelList<Portlet>();
        ListModelList<Portlet> targetModel = new ListModelList<Portlet>();
        
        dashboard.getPortletList().stream().forEach(portlet ->{
            if(Constants.CATEGORY_TABLE == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                sourceModel.add(portlet);
            }else if(Constants.RELEVANT_CONFIG == chartService.getCharts().get(portlet.getChartType()).getCategory()){
                targetModel.add(portlet);
            }
        });
        
        sourceCombobox.setModel(sourceModel);
        sourceCombobox.setItemRenderer((comboitem,widgetPassed,index)->{
            Portlet widget =(Portlet)widgetPassed;
            comboitem.setLabel(widget.getName());
            comboitem.setValue(widget);
            comboitem.addEventListener(Events.ON_CLICK, populateTableAttributes);
        });
        
        
        targetCombobox.setModel(targetModel);
        targetCombobox.setItemRenderer((comboitem,widgetPassed,index)->{
            Portlet widget =(Portlet)widgetPassed;
            comboitem.setLabel(widget.getName());
            comboitem.setValue(widget);
            comboitem.addEventListener(Events.ON_CLICK, populateRelevantAttributes);
        });
    } 
    
    
}
