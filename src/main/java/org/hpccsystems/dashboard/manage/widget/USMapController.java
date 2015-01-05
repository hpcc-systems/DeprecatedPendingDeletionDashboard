package org.hpccsystems.dashboard.manage.widget;

import java.util.HashMap;
import java.util.Map;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.charts.USMap;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

/**
 * EditChartController class is used to handle the edit page of the Dashboard
 * project and controller class for edit_portlet.zul file.
 * 
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class USMapController extends ConfigurationComposer<Component> {
    private static final long serialVersionUID = 1L;
    private static final String ON_LOADING = "onLoading";
    private static final Logger LOGGER = LoggerFactory.getLogger(USMapController.class);
    
    private USMap usMap;

    @Wire
    private Listbox chartMeasureListbox;
    private ListModelList<Measure> measures = new ListModelList<Measure>();
    
    @Wire
    private Listbox chartAttributeListbox;
    private ListModelList<Attribute> states = new ListModelList<Attribute>();
    
    @WireVariable
    private WSSQLService wssqlService;
    
    @Wire
    private Div chart;
    
    private ListitemRenderer<Measure> chartMeasureRenderer = (listitem, measure, index) -> {
        Listcell listItemCell=new Listcell();
        listItemCell.setLabel(measure.getColumn());
        listItemCell.setParent(listitem);
        Button closeButton=new Button();
        closeButton.setParent(listItemCell);
        closeButton.setIconSclass("z-icon-times");
        listitem.appendChild(listItemCell);
        closeButton.addEventListener("onClick", event -> {
            measures.remove(measure);    
            usMap.setMeasure(null);
            chartMeasureListbox.setDroppable(Constants.TRUE);
        });
    };
    
    private ListitemRenderer<Attribute> chartAttributeRenderer = (listitem, attribute, index) -> {
        Listcell listItemCell=new Listcell();
        listItemCell.setLabel(attribute.getColumn());
        listItemCell.setParent(listitem);
        Button closeButton=new Button();
        closeButton.setParent(listItemCell);
        closeButton.setIconSclass("z-icon-times");
        listitem.appendChild(listItemCell);
        closeButton.addEventListener("onClick", event -> {
            states.remove(attribute);    
            usMap.setState(null);
            chartAttributeListbox.setDroppable(Constants.TRUE);
        });
    };
    
    
        
    final Map<String, Object> parameters = new HashMap<String, Object>();
    
    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        return super.doBeforeCompose(page, parent, compInfo);
    }
    
    @Override
    public void doBeforeComposeChildren(Component comp) throws Exception {
        super.doBeforeComposeChildren(comp);
    }
    
    public boolean getShowGroupPanel() {
        return false;
    }
    
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        usMap = (USMap) widgetConfiguration.getWidget();
        hpccConnection = widgetConfiguration.getDashboard().getHpccConnection();
        comp.addEventListener(ON_LOADING, loadingListener);
        
        Clients.showBusy(comp, "Fetching fields");
        Events.echoEvent(ON_LOADING, comp, null);
        
        chartMeasureListbox.setModel(measures);
        chartMeasureListbox.setItemRenderer(chartMeasureRenderer);
        chartAttributeListbox.setModel(states);
        chartAttributeListbox.setItemRenderer(chartAttributeRenderer);      
        if(usMap.isConfigured()){
            measures.add(usMap.getMeasure());
            states.add(usMap.getState());
            drawChart();
        }
    }
    
    @Listen("onDrop = #chartMeasureListbox")
    public void onDropWeight(DropEvent event) {
        Listitem draggedItem = (Listitem) event.getDragged();
        Field field = draggedItem.getValue();
        Measure measure = new Measure(field);
        if(event.getDragged().getParent().equals(attributeListbox)){
            Clients.showNotification("Only measure objects can be dropped","warning",chartMeasureListbox,"end_center", 5000, true);
        }else{
            usMap.setMeasure(measure);
            measures.add(measure);
            chartMeasureListbox.setDroppable(Constants.FALSE);
        }
        if(usMap.isConfigured()) {            
            try {
                drawChart();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Listen("onDrop = #chartAttributeListbox")
    public void onDropLabel(DropEvent event) {
        Listitem draggedItem = (Listitem) event.getDragged();
        Field field = draggedItem.getValue();
        Attribute attribute = new Attribute(field);
        usMap.setState(attribute);
        states.add(attribute);
        chartAttributeListbox.setDroppable(Constants.FALSE);
        if(usMap.isConfigured()) {            
            try {
                drawChart();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
}

