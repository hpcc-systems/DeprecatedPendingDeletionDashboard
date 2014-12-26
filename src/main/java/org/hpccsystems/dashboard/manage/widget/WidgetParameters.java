package org.hpccsystems.dashboard.manage.widget;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.Widget;

public class WidgetParameters {
    private boolean isNew;
    private Dashboard dashboard;
    private Widget widget;
    
    public WidgetParameters(boolean isNew, Dashboard dashboard) {
        this.isNew = isNew;
        this.dashboard = dashboard;
    }
    
    public boolean isNew() {
        return isNew;
    }
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
    public Dashboard getDashboard() {
        return dashboard;
    }
    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }
    public Widget getWidget() {
        return widget;
    }
    public void setWidget(Widget widget) {
        this.widget = widget;
    }
}
