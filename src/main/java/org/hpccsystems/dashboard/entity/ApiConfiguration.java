package org.hpccsystems.dashboard.entity;

/**
 * Class to check weather appl launched from external Source
 *
 */
public class ApiConfiguration {
	private boolean apiEnabled;
	private boolean apiChartSetting;

	public boolean isApiChartSetting() {
		return apiChartSetting;
	}

	public void setApiChartSetting(boolean apiChartSetting) {
		this.apiChartSetting = apiChartSetting;
	}

	public boolean isApiEnabled() {
		return apiEnabled;
	}

	public void setApiEnabled(boolean apiEnabled) {
		this.apiEnabled = apiEnabled;
	}

}
