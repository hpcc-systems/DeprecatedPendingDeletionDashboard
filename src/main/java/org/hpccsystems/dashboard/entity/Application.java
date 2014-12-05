package org.hpccsystems.dashboard.entity;

/**
 * Application is model class for Application Id & Name.
 * 
 */
public class Application {
    String appId;
    String appName;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Application [appId=").append(appId).append(", appName=").append(appName).append("]");
        return builder.toString();
    }

    public Application() {
        super();
    }

    public Application(String appId, String appName) {
        super();
        this.appId = appId;
        this.appName = appName;
    }

    /**
     * @return the appId
     */
    public final String getAppId() {
        return appId;
    }

    /**
     * @param appId
     *            the appId to set
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
     * @param appName
     *            the appName to set
     */
    public final void setAppName(final String appName) {
        this.appName = appName;
    }

}
