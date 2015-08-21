package org.hpccsystems.dashboard.entity;

import java.util.ArrayList;
import java.util.List;

import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.InputParam;
import org.hpccsystems.dashboard.chart.entity.TitleColumn;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.services.ChartService;
import org.zkoss.zkplus.spring.SpringUtil;

public class Portlet implements Cloneable{

    private String name;
    private Integer id;
    private Integer chartType;
    private String widgetState;
    private Integer column;

    private String chartDataXML;
    private String chartDataJSON;

    private ChartData chartData;
    private boolean isSinglePortlet;
    private List<TitleColumn> titleColumns;

    public List<TitleColumn> getTitleColumns() {
        return titleColumns;
    }

    public void setTitleColumns(List<TitleColumn> nameFields) {
        this.titleColumns = nameFields;
    }

    public boolean getIsSinglePortlet() {
		return isSinglePortlet;
	}

	public void setIsSinglePortlet(boolean isSinglePortlet) {
		this.isSinglePortlet = isSinglePortlet;
	}

	public final Integer getColumn() {
        return column;
    }

    public final void setColumn(final Integer column) {
        this.column = column;
    }

    public final String getName() {
        return name;
    }

    public final void setName(final String title) {
        this.name = title;
    }

    public final Integer getId() {
        return id;
    }

    public final void setId(final Integer id) {
        this.id = id;
    }

    public final Integer getChartType() {
        return chartType;
    }

    public final void setChartType(final Integer chartType) {
        this.chartType = chartType;
    }

    public final String getChartDataJSON() {
        return chartDataJSON;
    }

    public final void setChartDataJSON(final String chartDataJSON) {
        this.chartDataJSON = chartDataJSON;
    }

    public String getWidgetState() {
        return widgetState;
    }

    public void setWidgetState(String widgetState) {
        this.widgetState = widgetState;
    }

    public String getChartDataXML() {
        return chartDataXML;
    }

    public void setChartDataXML(String chartData) {
        this.chartDataXML = chartData;
    }

    public ChartData getChartData() {
        return chartData;
    }

    public void setChartData(ChartData chartData) {
        this.chartData = chartData;
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();
        buffer.append("Portlet [name=").append(name).append(", id=").append(id)
                .append(", chartType=").append(chartType)
                .append(", widgetState=").append(widgetState)
                .append(", column=").append(column).append(", chartDataXML=")
                .append(chartDataXML).append(", chartDataJSON=")
                .append(chartDataJSON).append(", chartData=").append(chartData)
                .append(", isSinglePortlet=").append(isSinglePortlet)
                .append("]");
        return buffer.toString();
    }

    public boolean isLive() {
        return Constants.STATE_LIVE_CHART.equals(widgetState);
    }

    @Override
    public Portlet clone() throws CloneNotSupportedException {
        Portlet clonedObj = (Portlet) super.clone();
        
        if(this.getChartData() != null) {
            clonedObj.setChartData(this.getChartData().clone());
        }
        
        return clonedObj;
    }

    
    public boolean isGloballyFilterable() {
        ChartService chartService = (ChartService) SpringUtil.getBean("chartService");
        int category = chartService.getCharts().get(chartType).getCategory();
        
        return Constants.STATE_LIVE_CHART.equals(widgetState)
                && Constants.CATEGORY_TEXT_EDITOR != category
                && Constants.CATEGORY_SCORED_SEARCH_TABLE != category;
    }

    public void applyInputParams(List<InputParam> inputParams) {
        if(chartData.getInputParams() != null && !chartData.getInputParams().isEmpty()){
            //Removing existing matched params
            inputParams.forEach(param -> chartData.getInputParams().remove(param));
            
            chartData.getInputParams().addAll(inputParams);
        } else {
            List<InputParam> params = new ArrayList<InputParam>();
            params.addAll(inputParams);
            chartData.setInputParams(inputParams);
        }
        
    }
}
