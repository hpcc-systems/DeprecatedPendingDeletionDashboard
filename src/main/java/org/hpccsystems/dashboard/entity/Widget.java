package org.hpccsystems.dashboard.entity;


public class Widget {
	
	private String chartName;
	private Widget chartConfig;
	private String chartType;
	
	public String getChartType() {
		return chartType;
	}
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	public Widget getChartConfig() {
		return chartConfig;
	}
	public void setChartConfig(Widget chartConfig) {
		this.chartConfig = chartConfig;
	}
	public String getChartName() {
		return chartName;
	}
	public void setChartName(String chartName) {
		this.chartName = chartName;
	}

}
