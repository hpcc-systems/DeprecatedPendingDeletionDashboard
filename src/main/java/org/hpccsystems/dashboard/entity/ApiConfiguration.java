package org.hpccsystems.dashboard.entity;

/**
 * Class to check weather appl launched from external Source
 *
 */
public class ApiConfiguration {
	private boolean apiEnabled;
	private boolean apiConfig;
	private boolean apiChartConfig;
	
	public boolean isApiChartConfig() {
		return apiChartConfig;
	}

	public void setApiChartConfig(boolean apiChartConfig) {
		this.apiChartConfig = apiChartConfig;
	}

	@Override
	public String toString() {
		
		StringBuffer buffer = new StringBuffer();
		buffer.append("ApiConfiguration [apiEnabled=" ).append(apiEnabled).append(", apiConfig=")
		.append(apiConfig).append(", apiChartConfig=" ).append(apiChartConfig)
		.append("]");
		return buffer.toString();
	}

	public boolean isApiConfig() {
		return apiConfig;
	}

	public void setApiConfig(boolean apiConfig) {
		this.apiConfig = apiConfig;
	}		

	public boolean isApiEnabled() {
		return apiEnabled;
	}

	public void setApiEnabled(boolean apiEnabled) {
		this.apiEnabled = apiEnabled;
	}

}
