package org.hpccsystems.dashboard.entity;

import java.util.ArrayList;

/**
 * This class is model for Dashboard.
 *
 */
public class Dashboard {
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Dashboard [layout=").append(layout).append( ", name=" )
		.append(name).append(", columnCount=").append(columnCount)
		.append(", dashboardId=").append(dashboardId).append(", applicationId=" )
		.append(applicationId).append(", dashboardState=").append(dashboardState)
		.append(", isPersisted=").append(isPersisted).append(", portletList=").append(portletList)
		.append("]");
		return buffer.toString();
	}

	private String layout;
	private String name = "Dashboard Name";
	private Integer columnCount = 0;
	private Integer dashboardId;
	
	private String applicationId;
	private String dashboardState;
	
	public String getDashboardState() {
		return dashboardState;
	}

	public void setDashboardState(String dashboardState) {
		this.dashboardState = dashboardState;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	private boolean isPersisted;

	/**
	 *  When portletList is empty Dashboard is considered new, with no charts of any state is added
	 *  This list is designed to be empty when the associated Dashboard is never accessed in a User Session  
	 */
	private ArrayList<Portlet> portletList= new ArrayList<Portlet>();
	
	public Integer getDashboardId() {
		return dashboardId;
	}
	
	public void setDashboardId(Integer dashBoardId) {
		this.dashboardId = dashBoardId;
	}

	/**
	 * @return the layout
	 */
	public final String getLayout() {
		return layout;
	}

	/**
	 * @param layout the layout to set
	 */
	public final void setLayout(final String layout) {
		this.layout = layout;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the columnCount
	 */
	public final Integer getColumnCount() {
		return columnCount;
	}

	/**
	 * @param columnCount the columnCount to set
	 */
	public final void setColumnCount(final Integer columnCount) {
		this.columnCount = columnCount;
	}

	/**
	 * @return the portletList
	 */
	public final ArrayList<Portlet> getPortletList() {
		return portletList;
	}

	/**
	 * @param portletList the portletList to set
	 */
	public final void setPortletList(final ArrayList<Portlet> portletList) {
		this.portletList = portletList;
	}

	public boolean isPersisted() {
		return isPersisted;
	}

	public void setPersisted(boolean isPersisted) {
		this.isPersisted = isPersisted;
	}

}
