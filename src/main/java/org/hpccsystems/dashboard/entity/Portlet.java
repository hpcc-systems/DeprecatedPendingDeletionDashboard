package org.hpccsystems.dashboard.entity;

import java.util.List;
import java.util.LinkedHashMap; 

public class Portlet {
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Portlet [name=").append(name)
				.append(", id=").append(id)
				.append(", chartType=").append(chartType)
				.append(", widgetState=").append(widgetState)
				.append(", chartDataXML=").append(chartDataXML)
				.append(", chartDataJSON=").append(chartDataJSON)
				.append(", tableDataMap=").append(tableDataMap)
				.append(", persisted=").append(persisted)
				.append(", column=").append(column).append("]");
		return  builder.toString() ;
	}
	private String name;
	private Integer id;
	private Integer chartType;
	private String widgetState;
	private Integer column;
	
	private String chartDataXML;
	private String chartDataJSON;
	private LinkedHashMap<String, List<String>> tableDataMap;
	private boolean persisted = true; 	

	public boolean isPersisted() {
		return persisted;
	}
	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
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
	public final LinkedHashMap<String, List<String>> getTableDataMap() {
		return tableDataMap;
	}
	public final void setTableDataMap(LinkedHashMap<String, List<String>> tableDataMap) {
		this.tableDataMap = tableDataMap;
	}
}
