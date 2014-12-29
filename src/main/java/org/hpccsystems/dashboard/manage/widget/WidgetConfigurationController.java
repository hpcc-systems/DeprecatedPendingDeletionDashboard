package org.hpccsystems.dashboard.manage.widget;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
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
        WidgetConfiguration configuration = 
                (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        configuration.setHolder(holder);
        
        holder.setDynamicProperty(Constants.WIDGET_CONFIG, configuration);
        holder.setSrc("widget/chartList.zul");
        holder.addEventListener(WidgetConfiguration.ON_CHART_TYPE_SELECT, event -> {
                holder.setSrc("widget/fileBrowser.zul");
        });
        
        holder.addEventListener(WidgetConfiguration.ON_FILE_SELECT, event -> {
            holder.setSrc("widget/pie.zul");
        });
    }

}
