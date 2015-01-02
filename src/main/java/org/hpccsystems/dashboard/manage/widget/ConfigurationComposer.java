package org.hpccsystems.dashboard.manage.widget;

import java.util.List;
import java.util.stream.Collectors;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.HPCCFileService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.ListitemRenderer;

public class ConfigurationComposer<T> extends SelectorComposer<Component>{
    private static final long serialVersionUID = 1L;

    protected WidgetConfiguration widgetConfiguration;
    
    @Wire
    Include filterHolder;
    
    @Wire
    private Listbox measureListbox;
    
    @Wire
    protected Listbox attributeListbox;
    
    @WireVariable
    private HPCCFileService hpccFileService;
    protected HPCCConnection hpccConnection;
    
    private ListitemRenderer<Field> attributeRenderer = (listitem, field, index) -> {
        listitem.setLabel(field.getColumn());
        listitem.setDraggable(Constants.TRUE);
        listitem.setValue(field);
    };
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        widgetConfiguration = (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        filterHolder.setDynamicProperty(Constants.WIDGET_CONFIG, widgetConfiguration);
    }
    
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
}
