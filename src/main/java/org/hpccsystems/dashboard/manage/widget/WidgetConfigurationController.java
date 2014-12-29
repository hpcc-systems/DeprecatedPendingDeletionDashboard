package org.hpccsystems.dashboard.manage.widget;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.WidgetConfiguration;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;

public class WidgetConfigurationController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    
    @Wire
    private Include holder;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        WidgetConfiguration configuration = new WidgetConfiguration(holder);
        
        holder.setDynamicProperty(Constants.WIDGET_CONFIG, configuration);
        
        holder.setSrc("widget/chartList.zul");
        
        
        holder.addEventListener(WidgetConfiguration.ON_CHART_TYPE_SELECT, new EventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                holder.setSrc("widget/pie.zul");
            }
        });
    }

}
