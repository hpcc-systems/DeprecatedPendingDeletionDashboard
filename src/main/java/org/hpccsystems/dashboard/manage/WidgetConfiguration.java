package org.hpccsystems.dashboard.manage;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.zkoss.zk.ui.Component;

public class WidgetConfiguration {
    public static final String ON_CHART_TYPE_SELECT = "onChartTypeSelect";
    public static final String ON_FILE_SELECT = "onFileSelect";
    
    private ChartConfiguration chartConfiguration;
    private Dashboard dashboard;
    private Component holder;
    private Widget widget;
    
    public WidgetConfiguration(Dashboard dashboard) {
        this.dashboard = dashboard;
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
