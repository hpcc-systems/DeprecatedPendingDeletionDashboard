package org.hpccsystems.dashboard.manage.widget.filters;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;

public class FilterController extends SelectorComposer<Component>{
    private static final long serialVersionUID = 1L;
    
    private Widget widget;
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterController.class);
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        WidgetConfiguration configuration = (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        widget = configuration.getWidget();
        
        LOGGER.debug("Widget - {}", widget);
    }
    
}
