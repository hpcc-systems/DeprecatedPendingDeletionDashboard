package org.hpccsystems.dashboard.manage.widget;

import java.util.List;
import java.util.stream.Collectors;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.ChartdataJSON;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.charts.Pie;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.HPCCFileService;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.zkoss.zhtml.Div;
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

import com.google.gson.Gson;

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
    
    @WireVariable
    private WSSQLService wssqlService;
    private HPCCConnection hpccConnection;
    @Wire
    private Div chart;
    
    private ListitemRenderer<Field> measureRenderer = (listitem, field, index) -> {
        listitem.setLabel(field.getColumn());
        listitem.setDraggable(Constants.TRUE);
        listitem.setValue(field);
    };
    
    private ListitemRenderer<Field> attributeRenderer = (listitem, field, index) -> {
        listitem.setLabel(field.getColumn());
        listitem.setDraggable(Constants.TRUE);
        listitem.setValue(field);
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
        hpccConnection = widgetConfiguration.getDashboard().getHpccConnection();
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
        if(event.getDragged().getParent().equals(attributeListbox)){
            Clients.showNotification("Only measure objects can be dropped","warning",weightListbox,"end_center", 5000, true);
        }else{
            weights.add(measure);
            weightListbox.setDroppable("false");
        }
        if(pie.isConfigured()) {            
            try {
                drawChart();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Listen("onDrop = #labelListbox")
    public void onDropLabel(DropEvent event) {
        Listitem draggedItem = (Listitem) event.getDragged();
        Field field = draggedItem.getValue();
        Attribute attribute = new Attribute(field);
        labels.add(attribute);
        labelListbox.setDroppable("false");
        if(pie.isConfigured()) {            
            try {
                drawChart();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private void drawChart() throws Exception {
        
        ChartdataJSON chartData = wssqlService.getChartdata(pie, hpccConnection);
        Clients.evalJavaScript("visualize('"+chart.getUuid()+"','pie','"+new Gson().toJson(new ChartdataJSON())+")");
    }
    
    
}

