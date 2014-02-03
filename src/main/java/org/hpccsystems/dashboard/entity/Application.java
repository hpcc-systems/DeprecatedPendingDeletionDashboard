package org.hpccsystems.dashboard.entity;

/**
 * Application is model class for Application Id & Name.
 *
 */
public class Application {

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Application [appId=").append(appId)
				.append(", appName=").append(appName)
				.append(", appTypeId=").append(appTypeId)
				.append("]");
		return  builder.toString() ;	}

	public Application() {
		super();
	}

	public Application(String appId, String appName, Integer appTypeId) {
		super();
		this.appId = appId;
		this.appName = appName;
		this.appTypeId = appTypeId;
	}

	String appId;
	
	String appName;
	Integer appTypeId;

	public Integer getAppTypeId() {
		return appTypeId;
	}

	public void setAppTypeId(Integer appTypeId) {
		this.appTypeId = appTypeId;
	}

	/**
	 * @return the appId
	 */
	public final String getAppId() {
		return appId;
	}

	/**
	 * @param appId the appId to set
	 */
	public final void setAppId(final String appId) {
		this.appId = appId;
	}

	/**
	 * @return the appName
	 */
	public final String getAppName() {
		return appName;
	}

	/**
	 * @param appName the appName to set
	 */
	public final void setAppName(final String appName) {
		this.appName = appName;
	}

	
}
