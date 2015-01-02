package org.hpccsystems.dashboard.manage.widget;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringEscapeUtils;
import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.ChartdataJSON;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.charts.Pie;
import org.hpccsystems.dashboard.service.HPCCFileService;
import org.hpccsystems.dashboard.service.WSSQLService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class PieChartController extends ConfigurationComposer<Component> {
    private static final String ON_LOADING = "onLoading";
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(PieChartController.class);
    
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
    
    
    private ListitemRenderer<Field> attributeRenderer = (listitem, field, index) -> {
        listitem.setLabel(field.getColumn());
        listitem.setDraggable(Constants.TRUE);
        listitem.setValue(field);
    };
    
    private ListitemRenderer<Measure> weightRenderer = (listitem, measure, index) -> {
        Listcell listcellOne = new Listcell();
    	Listcell listcellTwo = new Listcell();
    	listcellOne.setLabel(measure.getColumn());
    	Button button = new Button();
    	button.setLabel(measure.getAggregation().toString());
		button.setZclass("btn btn-xs");
		button.setStyle("font-size: 10px; float: right;");
		Button closeButton=new Button();
		closeButton.setParent(listcellTwo);
	    closeButton.setIconSclass("z-icon-times");
	    closeButton.addEventListener("onClick", event -> {
            weights.remove(measure);    
            pie.setWeight(null);
            weightListbox.setDroppable(Constants.TRUE);
        });
    	listcellTwo.appendChild(button);
    	listcellOne.setParent(listitem);
    	listcellTwo.setParent(listitem);
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
        });
    };
    
    private EventListener<Event> loadingListener = event -> {
        List<Field> fields = 
                hpccFileService.getFields(pie.getLogicalFile(), widgetConfiguration.getDashboard().getHpccConnection());
        measureListbox.setModel(
                new ListModelList<Measure>(
                        fields.stream().filter(field -> field.isNumeric())
                        .map(field -> new Measure(field))
                        .collect(Collectors.toList())));
        measureListbox.setItemRenderer(new MeasureRenderer());
        
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
        Measure measure = draggedItem.getValue();
      
        if(event.getDragged().getParent().equals(attributeListbox)){
            Clients.showNotification("Only measure objects can be dropped","warning",weightListbox,"end_center", 5000, true);
        }else{
            pie.setWeight(measure);
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
        pie.setLabel(attribute);
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
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Div id -{}\nJSON - {}", chart.getUuid(), new GsonBuilder().setPrettyPrinting().create().toJson(chartData));
        }
        Clients.evalJavaScript("createPreview('"+ chart.getUuid()+"','pie','"+ StringEscapeUtils.escapeJavaScript(new Gson().toJson(chartData))+"')");
    }
    
    
}

