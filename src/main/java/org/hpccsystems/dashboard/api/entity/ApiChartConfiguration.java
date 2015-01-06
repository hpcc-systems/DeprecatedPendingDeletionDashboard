package org.hpccsystems.dashboard.api.entity;

import java.util.List;

import org.hpccsystems.dashboard.chart.entity.Field;
import org.hpccsystems.dashboard.chart.entity.HpccConnection;

public class ApiChartConfiguration {
    String datasetName;
    Integer chartType;
    String chartTitle;
    String dashboardTitle;
    List<Field> fields;
    HpccConnection hpccConnection;
    
    public String getDatasetName() {
        return datasetName;
    }
    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }
    public Integer getChartType() {
        return chartType;
    }
    public void setChartType(Integer chartType) {
        this.chartType = chartType;
    }
    public String getChartTitle() {
        return chartTitle;
    }
    public void setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
    }
    public String getDashboardTitle() {
        return dashboardTitle;
    }
    public void setDashboardTitle(String dashboardTitle) {
        this.dashboardTitle = dashboardTitle;
    }
    public List<Field> getFields() {
        return fields;
    }
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
    public HpccConnection getHpccConnection() {
        return hpccConnection;
    }
    public void setHpccConnection(HpccConnection hpccConnection) {
        this.hpccConnection = hpccConnection;
    }
}
