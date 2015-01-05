package org.hpccsystems.dashboard.manage.widget;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.ChartdataJSON;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.NumericFilter;
import org.hpccsystems.dashboard.entity.widget.StringFilter;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.HPCCFileService;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Popup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigurationComposer<T> extends SelectorComposer<Component>{
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationComposer.class);

    protected WidgetConfiguration widgetConfiguration;
    private Widget widget;
        
    @Wire
    protected Listbox measureListbox;
    
    @Wire
    protected Listbox attributeListbox;
    
    @Wire
    private Div chart;
    
    @Wire
    private Listbox filterbox;
    private ListModelList<Filter> filterModel = new ListModelList<Filter>(); 
    
    @WireVariable
    private HPCCFileService hpccFileService;
    @WireVariable
    private WSSQLService wssqlService;
    
    protected HPCCConnection hpccConnection;
    
    private ListitemRenderer<Field> attributeRenderer = (listitem, field, index) -> {
        listitem.setLabel(field.getColumn());
        listitem.setDraggable(Constants.TRUE);
        listitem.setValue(field);
    };
    
    protected EventListener<Event> loadingListener = event -> {
        List<Field> fields = 
                hpccFileService.getFields(widgetConfiguration.getWidget().getLogicalFile(), widgetConfiguration.getDashboard().getHpccConnection());
        measureListbox.setModel(
                new ListModelList<Field>(
                        fields.stream().filter(field -> field.isNumeric())
                        .map(field -> new Measure(field))
                        .collect(Collectors.toList())));
        measureListbox.setItemRenderer(new MeasureRenderer());
        
        attributeListbox.setModel(
                new ListModelList<Field>(
                        fields.stream().filter(field -> !field.isNumeric())
                        .collect(Collectors.toList())));
        attributeListbox.setItemRenderer(attributeRenderer);
        
        Clients.clearBusy(ConfigurationComposer.this.getSelf());
    };
    
    private  ListitemRenderer<Filter> filterRenderer = (item, filter, index) -> {
        renderFilter(item, filter);
    };

    private EventListener<Event> drawChartListener = event -> {
        try {
            ChartdataJSON chartData;
            chartData = wssqlService.getChartdata(widget, hpccConnection);
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Div id -{}\nChart - {}\nJSON - {}", 
                        chart.getUuid(), 
                        widgetConfiguration.getWidget().getChartConfiguration().getHipieChartId(),
                        new GsonBuilder().setPrettyPrinting().create().toJson(chartData));
            }
            Clients.evalJavaScript("createPreview('"+ chart.getUuid()+"', '" 
                    + widgetConfiguration.getWidget().getChartConfiguration().getHipieChartId()
                    + "','"+ StringEscapeUtils.escapeJavaScript(new Gson().toJson(chartData))+"')");
        } catch (Exception e) {
           LOGGER.error(Constants.EXCEPTION,e);
           //TODO: Show error using JS
        }
        
        Clients.clearBusy(getSelf());
    };
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        widgetConfiguration = (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        widgetConfiguration.setComposer(this);
        widget = widgetConfiguration.getWidget();
        
        filterbox.setModel(filterModel);
        filterbox.setItemRenderer(filterRenderer);
        
        if(widgetConfiguration.getWidget().isConfigured()){
            filterModel.addAll(widgetConfiguration.getWidget().getFilters());
        }
        
        chart.addEventListener(Constants.ON_DRAW_CHART, drawChartListener);
    }
    
    
    @Listen("onDrop = #filterbox")
    public void onDropFilters(DropEvent event) {
        Listitem draggedItem = (Listitem) event.getDragged();
        Field field = draggedItem.getValue();
        
        Filter filter = field.isNumeric() ? new NumericFilter(field) : new StringFilter(field);
        if(widgetConfiguration.getWidget().getFilters() != null
                && widgetConfiguration.getWidget().getFilters().contains(filter)){
            Clients.showNotification("Filter already exists","warning",filterbox,"end_center", 5000, true);
        } else {
            widgetConfiguration.getWidget().addFilter(filter);
            filterModel.add(filter);
        }
    }
    
    private void renderFilter(Listitem item, Filter filter) {
        Listcell listcell = new Listcell(filter.getColumn());
        
        Button closeButton = new Button();
        closeButton.setIconSclass("z-icon-times");
        closeButton.addEventListener("onClick", event -> widget.removeFilter(filter));
        
        Button playButton = new Button();
        playButton.setIconSclass("z-icon-play");
        
        Popup popup = new Popup(){
            private static final long serialVersionUID = 1L;
            
            @Override
            public void open(Component ref, String position) {
                open(playButton, "end_center");
            };
            
        };
        Include include = new Include(filter.isNumeric() ? "widget/filter/numericPopup.zul" : "widget/filter/stringPopup.zul");
        include.setDynamicProperty(Constants.WIDGET_CONFIG, widgetConfiguration);
        include.setDynamicProperty(Constants.FILTER, filter);
        popup.appendChild(include);
        
        listcell.appendChild(playButton);
        listcell.appendChild(closeButton);
        listcell.appendChild(popup);
        
        playButton.setPopup(popup);
        
        item.appendChild(listcell);
    }
    
    public void drawChart() {
        if(widget.isConfigured()) {
            Clients.showBusy(getSelf(), "Retriving data");
            Events.echoEvent(Constants.ON_DRAW_CHART, chart, null);
        }
    }
    
    protected void clearChart() {
        Clients.evalJavaScript("clearChart('"+ chart.getUuid()+"')");
    }
}
