package org.hpccsystems.dashboard.entity;

/**
 * Class to check weather appl launched from external Source
 *
 */
public class ApiConfiguration {
	private boolean apiEnabled;

	public boolean isApiEnabled() {
		return apiEnabled;
	}

	public void setApiEnabled(boolean apiEnabled) {
		this.apiEnabled = apiEnabled;
	}

}
