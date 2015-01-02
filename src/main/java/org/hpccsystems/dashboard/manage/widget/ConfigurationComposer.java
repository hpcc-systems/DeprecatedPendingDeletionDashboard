package org.hpccsystems.dashboard.manage.widget;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Include;

public class ConfigurationComposer<T> extends SelectorComposer<Component>{
    private static final long serialVersionUID = 1L;

    protected WidgetConfiguration widgetConfiguration;
    
    @Wire
    Include filterHolder;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        widgetConfiguration = (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        filterHolder.setDynamicProperty(Constants.WIDGET_CONFIG, widgetConfiguration);
    }
}
