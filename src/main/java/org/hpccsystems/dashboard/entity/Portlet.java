package org.hpccsystems.dashboard.entity;

import java.util.List;
import java.util.LinkedHashMap;
/**
 * This class is model for Portlet.
 *
 */
public class Portlet {
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Portlet [name=").append(name)
				.append(", id=").append(id)
				.append(", chartType=").append(chartType)
				.append(", xColName=").append(xColName)
				.append(", yColName=").append(yColName)
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
	private String xColName;
	private String yColName;
	private String widgetState;
	
	private String chartDataXML;
	private String chartDataJSON;
	private LinkedHashMap<String, List<String>> tableDataMap;
	private boolean persisted = true; 
	
	private int widgetSequence;
	
	/**
	 * @return boolean
	 */
	public boolean isPersisted() {
		return persisted;
	}
	/**
	 * @param persisted
	 */
	public void setPersisted(boolean persisted) {
		this.persisted = persisted;
	}
	/**
	 * Column indicator, starts with 0
	 */
	private Integer column;
	
	
	/**
	 * @return the column
	 */
	public final Integer getColumn() {
		return column;
	}
	/**
	 * @param column the column to set
	 */
	public final void setColumn(final Integer column) {
		this.column = column;
	}
	/**
	 * @return the title
	 */
	public final String getName() {
		return name;
	}
	/**
	 * @param title the title to set
	 */
	public final void setName(final String title) {
		this.name = title;
	}
	/**
	 * @return the id
	 */
	public final Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public final void setId(final Integer id) {
		this.id = id;
	}
	/**
	 * @return the chartType
	 */
	public final Integer getChartType() {
		return chartType;
	}
	/**
	 * @param chartType the chartType to set
	 */
	public final void setChartType(final Integer chartType) {
		this.chartType = chartType;
	}
	/**
	 * @return the xColName
	 */
	public final String getxColName() {
		return xColName;
	}
	/**
	 * @param xColName the xColName to set
	 */
	public final void setxColName(final String xColName) {
		this.xColName = xColName;
	}
	/**
	 * @return the yColName
	 */
	public final String getyColName() {
		return yColName;
	}
	/**
	 * @param yColName the yColName to set
	 */
	public final void setyColName(final String yColName) {
		this.yColName = yColName;
	}
	/**
	 * @return the chartDataJSON
	 */
	public final String getChartDataJSON() {
		return chartDataJSON;
	}
	/**
	 * @param chartDataJSON the chartDataJSON to set
	 */
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
	public int getWidgetSequence() {
		return widgetSequence;
	}
	public void setWidgetSequence(int widgetSequence) {
		this.widgetSequence = widgetSequence;
	}
}
