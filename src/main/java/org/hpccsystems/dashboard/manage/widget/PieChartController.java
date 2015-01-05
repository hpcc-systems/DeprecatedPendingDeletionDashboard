package org.hpccsystems.dashboard.manage.widget;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.charts.Pie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class PieChartController extends ConfigurationComposer<Component> {
    private static final String ON_LOADING = "onLoading";
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PieChartController.class);
    
    private Pie pie;
        
    @Wire
    private Listbox weightListbox;
    private ListModelList<Measure> weights = new ListModelList<Measure>();
    
    @Wire
    private Listbox labelListbox;
    private ListModelList<Attribute> labels = new ListModelList<Attribute>();
    
    private ListitemRenderer<Measure> weightRenderer = (listitem, measure, index) -> {
        Listcell listcell = new Listcell(measure.getColumn());
        Button button = new Button();
        button.setLabel(measure.getAggregation().toString());
        button.setZclass("btn btn-xs");
        
        Button closeButton=new Button();
        closeButton.setIconSclass("z-icon-times");
        closeButton.addEventListener("onClick", event -> {
            weights.remove(measure);    
            pie.setWeight(null);
            weightListbox.setDroppable(Constants.TRUE);
            clearChart();
        });
        
        listcell.appendChild(button);
        listcell.appendChild(closeButton);
        listitem.appendChild(listcell);
    };
    
    private ListitemRenderer<Attribute> labelRenderer = (listitem, attribute, index) -> {
        Listcell listItemCell=new Listcell();
        listItemCell.setLabel(attribute.getColumn());
        listItemCell.setParent(listitem);
        Button closeButton=new Button();
        closeButton.setParent(listItemCell);
        closeButton.setIconSclass("z-icon-times");
        listitem.appendChild(listItemCell);
        closeButton.addEventListener("onClick", event -> {
            labels.remove(attribute);    
            pie.setLabel(null);
            labelListbox.setDroppable(Constants.TRUE);
             clearChart();
        });
    };
        
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        pie = (Pie) widgetConfiguration.getWidget();
        hpccConnection = widgetConfiguration.getDashboard().getHpccConnection();
        comp.addEventListener(ON_LOADING, loadingListener);
        
        Clients.showBusy(comp, "Fetching fields");
        Events.echoEvent(ON_LOADING, comp, null);
        
        weightListbox.setModel(weights);
        weightListbox.setItemRenderer(weightRenderer);
        labelListbox.setModel(labels);
        labelListbox.setItemRenderer(labelRenderer);
        
        if(pie.isConfigured()) {
            weights.add(pie.getWeight());
            labels.add(pie.getLabel());
            drawChart();
        }
    }
    
    @Listen("onDrop = #weightListbox")
    public void onDropWeight(DropEvent event) {
        if(event.getDragged().getParent().equals(attributeListbox)){
            Clients.showNotification("Only measure objects can be dropped",Clients.NOTIFICATION_TYPE_ERROR,weightListbox,"end_center", 5000, true);
            return;            
        }
        
        Listitem draggedItem = (Listitem) event.getDragged();
        Measure measure = draggedItem.getValue();
        pie.setWeight(measure);
        weights.add(measure);
        weightListbox.setDroppable(Constants.FALSE);
        
        drawChart();
    }

    @Listen("onDrop = #labelListbox")
    public void onDropLabel(DropEvent event) {
        Listitem draggedItem = (Listitem) event.getDragged();
        Field field = draggedItem.getValue();
        Attribute attribute = new Attribute(field);
        pie.setLabel(attribute);
        labels.add(attribute);
        labelListbox.setDroppable(Constants.FALSE);
        
        drawChart();
    }
}

