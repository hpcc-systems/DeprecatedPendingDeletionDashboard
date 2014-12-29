package org.hpccsystems.dashboard.entity.widget;

import org.zkoss.zk.ui.Component;

public class WidgetConfiguration {
    public static final String ON_CHART_TYPE_SELECT = "onChartTypeSelect";
    
    private Component holder;
    private Widget widget;

    public WidgetConfiguration(Component holder) {
        this.holder = holder;
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    public Component getHolder() {
        return holder;
    }

    public void setHolder(Component holder) {
        this.holder = holder;
    }
}
