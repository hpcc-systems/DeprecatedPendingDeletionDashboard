package org.hpccsystems.dashboard.manage;

import java.util.HashMap;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.manage.widget.WidgetParameters;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Window;

public class DashboardController extends SelectorComposer<Component>{
    private static final long serialVersionUID = 1L;

    private Dashboard dashboard;
    
    @Listen("onClick = #addWidget")
    public void onAddWidget() {
        Window window = (Window)Executions.createComponents(
                "widget/config.zul", null, new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("", new WidgetParameters(true, dashboard));
                    }
                });
        
        window.doModal();
    }
}
