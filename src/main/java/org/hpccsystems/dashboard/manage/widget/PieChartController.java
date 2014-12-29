package org.hpccsystems.dashboard.manage.widget;

import java.util.List;
import java.util.stream.Collectors;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.charts.Pie;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.HPCCFileService;
import org.zkoss.zk.ui.Component;
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
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class PieChartController extends SelectorComposer<Component> {
    private static final String ON_LOADING = "onLoading";
    private static final long serialVersionUID = 1L;
    
    private WidgetConfiguration widgetConfiguration;
    private Pie pie;
    
    @Wire
    private Listbox measureListbox;
    @Wire
    private Listbox attributeListbox;
    
    @Wire
    private Listbox weightListbox;
    private ListModelList<Measure> weights = new ListModelList<Measure>();
    
    @Wire
    private Listbox labelListbox;
    private ListModelList<Attribute> labels = new ListModelList<Attribute>();
    
    @WireVariable
    private HPCCFileService hpccFileService;
    
    private ListitemRenderer<Field> measureRenderer = (listitem, field, index) -> {
        listitem.setLabel(field.getColumn());
        listitem.setDraggable(Constants.TRUE);
    };
    
    private ListitemRenderer<Field> attributeRenderer = (listitem, field, index) -> {
        listitem.setLabel(field.getColumn());
        listitem.setDraggable(Constants.TRUE);
    };
    
    private ListitemRenderer<Measure> weightRenderer = (listitem, measure, index) -> {
        listitem.setLabel(measure.getColumn());
    };
    private ListitemRenderer<Attribute> labelRenderer = (listitem, attribute, index) -> {
        listitem.setLabel(attribute.getColumn());
    };
    
    private EventListener<Event> loadingListener = event -> {
        List<Field> fields = 
                hpccFileService.getFields(pie.getLogicalFile(), widgetConfiguration.getDashboard().getHpccConnection());
        measureListbox.setModel(
                new ListModelList<Field>(
                        fields.stream().filter(field -> field.isNumeric())
                        .collect(Collectors.toList())));
        measureListbox.setItemRenderer(measureRenderer);
        
        attributeListbox.setModel(
                new ListModelList<Field>(
                        fields.stream().filter(field -> !field.isNumeric())
                        .collect(Collectors.toList())));
        attributeListbox.setItemRenderer(attributeRenderer);
        
        Clients.clearBusy(PieChartController.this.getSelf());
    };
    
    
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        widgetConfiguration = (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        pie = (Pie) widgetConfiguration.getWidget();
        
        comp.addEventListener(ON_LOADING, loadingListener);
        
        Clients.showBusy(comp, "Fetching fields");
        Events.echoEvent(ON_LOADING, comp, null);
        
        weightListbox.setModel(weights);
        weightListbox.setItemRenderer(weightRenderer);
        labelListbox.setModel(labels);
        labelListbox.setItemRenderer(labelRenderer);
    }
    
    @Listen("onDrop = #weightListbox")
    public void onDropWeight(DropEvent event) {
        Listitem draggedItem = (Listitem) event.getDragged();
        Field field = draggedItem.getValue();
        Measure measure = new Measure(field);
        weights.add(measure);
    }
    
    @Listen("onDrop = #labelListbox")
    public void onDropLabel(DropEvent event) {
        Listitem draggedItem = (Listitem) event.getDragged();
        Field field = draggedItem.getValue();
        Attribute attribute = new Attribute(field);
        labels.add(attribute);
    }
}

