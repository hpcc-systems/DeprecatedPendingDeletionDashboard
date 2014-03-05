package org.hpccsystems.dashboard.entity;

import org.hpccsystems.dashboard.entity.chart.XYChartData;

public class Portlet {
	
	private String name;
	private Integer id;
	private Integer chartType;
	private String widgetState;
	private Integer column;
	
	private String chartDataXML;
	private String chartDataJSON;
	
	private XYChartData chartData;

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
	public XYChartData getChartData() {
		return chartData;
	}
	public void setChartData(XYChartData chartData) {
		this.chartData = chartData;
	}
	
	@Override
	public String toString() {
		return "Portlet [name=" + name + ", id=" + id + ", chartType="
				+ chartType + ", widgetState=" + widgetState + ", column="
				+ column + ", chartDataXML=" + chartDataXML
				+ ", chartDataJSON=" + chartDataJSON + ", chartData="
				+ chartData + "]";
	}
	
}
