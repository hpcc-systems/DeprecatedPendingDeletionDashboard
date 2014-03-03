package org.hpccsystems.dashboard.entity;

public class ChartDetails {
	
	public ChartDetails(int chartId, String staticImageURL, String chartName,
			String chartDesc,Integer maxXColumns, Integer maxYColumns) {
		super();
		this.chartId = chartId;
		this.staticImageURL = staticImageURL;
		this.chartName = chartName;
		this.chartDesc = chartDesc;
		this.maxXColumns = maxXColumns;
		this.maxYColumns = maxYColumns;
	}
	
	private int chartId;
	private String staticImageURL;
	private String chartName;
	private String chartDesc;
	/**
	 * Indicates maximum number of Columns that can be provided as Measures
	 * Value 0 indicates unlimited
	 */
	private Integer maxYColumns;

	/**
	 * Indicates maximum number of Columns that can be provided as Attributes
	 * Value 0 indicates unlimited
	 */
	private Integer maxXColumns;
	
	public final String getStaticImageURL() {
		return staticImageURL;
	}
	
	public final void setStaticImageURL(String staticImageURL) {
		this.staticImageURL = staticImageURL;
	}
	
	public final int getChartId() {
		return chartId;
	}
	
	public final void setChartId(int chartId) {
		this.chartId = chartId;
	}
	
	public final String getChartName() {
		return chartName;
	}
	
	public final void setChartName(String chartName) {
		this.chartName = chartName;
	}
	
	public final String getChartDesc() {
		return chartDesc;
	}
	
	public final void setChartDesc(String chartDesc) {
		this.chartDesc = chartDesc;
	}

	public Integer getMaxYColumns() {
		return maxYColumns;
	}

	public void setMaxYColumns(Integer maxYColumns) {
		this.maxYColumns = maxYColumns;
	}

	public Integer getMaxXColumns() {
		return maxXColumns;
	}

	public void setMaxXColumns(Integer maxXColumns) {
		this.maxXColumns = maxXColumns;
	}

}
