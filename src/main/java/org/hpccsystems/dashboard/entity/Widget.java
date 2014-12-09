package org.hpccsystems.dashboard.entity;

import org.hpccsystems.dashboard.chart.entity.ChartConfig;

public class Widget {
	
	private String chartName;
	private ChartConfig chartConfig;
	private String chartType;
	
	public String getChartType() {
		return chartType;
	}
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	public ChartConfig getChartConfig() {
		return chartConfig;
	}
	public void setChartConfig(ChartConfig chartConfig) {
		this.chartConfig = chartConfig;
	}
	public String getChartName() {
		return chartName;
	}
	public void setChartName(String chartName) {
		this.chartName = chartName;
	}

}
