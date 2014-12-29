package org.hpccsystems.dashboard.entity.widget;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.zkoss.zk.ui.Component;

public class WidgetConfiguration {
    public static final String ON_CHART_TYPE_SELECT = "onChartTypeSelect";
    
    private ChartConfiguration chartConfiguration;
    private Dashboard dashboard;
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

    public Dashboard getDashboard() {
        return dashboard;
    }

    public void setDashboard(Dashboard dashboard) {
        this.dashboard = dashboard;
    }

    public ChartConfiguration getChartConfiguration() {
        return chartConfiguration;
    }

    public void setChartConfiguration(ChartConfiguration chartConfiguration) {
        this.chartConfiguration = chartConfiguration;
    }
}
