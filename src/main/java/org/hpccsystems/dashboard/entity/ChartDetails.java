/**
 * 
 */
package org.hpccsystems.dashboard.entity;

/**
 * @author 370627
 *
 */
public class ChartDetails {
	
	/**
	 * @return the staticImageURL
	 */
	public final String getStaticImageURL() {
		return staticImageURL;
	}
	/**
	 * @param staticImageURL the staticImageURL to set
	 */
	public final void setStaticImageURL(String staticImageURL) {
		this.staticImageURL = staticImageURL;
	}
	/**
	 * @param chartId
	 * @param staticImageURL
	 * @param chartName
	 * @param chartDesc
	 */
	public ChartDetails(int chartId, String staticImageURL, String chartName,
			String chartDesc) {
		super();
		this.chartId = chartId;
		this.staticImageURL = staticImageURL;
		this.chartName = chartName;
		this.chartDesc = chartDesc;
	}
	private int chartId;
	private String staticImageURL;
	private String chartName;
	private String chartDesc;
	/**
	 * @return the chartId
	 */
	public final int getChartId() {
		return chartId;
	}
	/**
	 * @param chartId the chartId to set
	 */
	public final void setChartId(int chartId) {
		this.chartId = chartId;
	}
	/**
	 * @return the chartName
	 */
	public final String getChartName() {
		return chartName;
	}
	/**
	 * @param chartName the chartName to set
	 */
	public final void setChartName(String chartName) {
		this.chartName = chartName;
	}
	/**
	 * @return the chartDesc
	 */
	public final String getChartDesc() {
		return chartDesc;
	}
	/**
	 * @param chartDesc the chartDesc to set
	 */
	public final void setChartDesc(String chartDesc) {
		this.chartDesc = chartDesc;
	}

}
