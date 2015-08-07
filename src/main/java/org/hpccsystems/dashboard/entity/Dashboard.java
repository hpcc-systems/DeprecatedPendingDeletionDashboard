package org.hpccsystems.dashboard.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hpccsystems.dashboard.chart.entity.HpccConnection;
import org.hpccsystems.dashboard.chart.entity.InputParam;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.services.ChartService;
import org.zkoss.zkplus.spring.SpringUtil;

/**
 * This class is model for Dashboard.
 *
 */
public class Dashboard {
    private String sourceId;
    private String layout;
    private String name = "Dashboard Name";
    private Integer columnCount = 0;
    private Integer dashboardId;
    private String applicationId;
    private String dashboardState;
    private Timestamp lastupdatedDate;
    private Integer sequence;
    private boolean hasCommonFilter = false;
    private boolean showLocalFilter;
    private boolean lockCommonFilter;
    private Integer visibility;
    private String role;
  
    
    private Set<InputParam> commonQueryFilters; 
    

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
    private List<Portlet> portletList= new ArrayList<Portlet>();
    
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
    public final List<Portlet> getPortletList() {
        return portletList;
    }

    /**
     * @param portletList the portletList to set
     */
    public final void setPortletList(final List<Portlet> portletList) {
        this.portletList = portletList;
    }

    public boolean isPersisted() {
        return isPersisted;
    }

    public void setPersisted(boolean isPersisted) {
        this.isPersisted = isPersisted;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public Timestamp getLastupdatedDate() {
        return lastupdatedDate;
    }

    public void setLastupdatedDate(Timestamp lastupdatedDate) {
        this.lastupdatedDate = lastupdatedDate;
    }

    public boolean getHasCommonFilter() {
        return hasCommonFilter;
    }

    public void setHasCommonFilter(boolean hasCommonFilter) {
        this.hasCommonFilter = hasCommonFilter;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public boolean hasLiveChart() {
        if(this.portletList != null) {
            for (Portlet portlet : this.portletList) {
                if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Iterates through portletlist and returns the common HpccConnection object only 
     * if there is a same HpccConnection for all portlets with live charts in the dahboard
     * @param dashboard
     * @return
     *     Common HpccConnection Object if found, null otherwise
     *     Returns null if no live charts are present in dahboard 
     */
    public HpccConnection getCommonHpccConnection() {
        HpccConnection hpccConnection = null;
        Set<String> clusters = new HashSet<String>();
        Set<String> hostIps = new HashSet<String>();
        boolean hasNoLiveChart = true;
        boolean hasRoxieQuery = false;
        boolean hasLogicalFile = false;
        
        if(this.getPortletList() == null) {
            return null;
        }
       ChartService chartService = (ChartService) SpringUtil.getBean("chartService");
        for (Portlet portlet : this.getPortletList()) {
            if (portlet.getWidgetState().equals(Constants.STATE_LIVE_CHART)
            		&& Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                     && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                hasNoLiveChart = false;
                clusters.add(portlet.getChartData().getHpccConnection().getClusterType());
                hostIps.add(portlet.getChartData().getHpccConnection().getHostIp());
                //Checks for Roxie Query not to enable Common filter
                if(portlet.getChartData().getIsQuery()){
                    hasRoxieQuery = true;
                } else {
                    hasLogicalFile = true;
                }
                hpccConnection = portlet.getChartData().getHpccConnection();
            }
        }
        if (hasNoLiveChart || (hasRoxieQuery&&hasLogicalFile)) {
            return null;
        } else if (clusters.size() == 1 && hostIps.size() == 1) {
            if(hasRoxieQuery){
                hpccConnection.setDatasource(Constants.QUERY);
            }else if(hasLogicalFile){
                hpccConnection.setDatasource(Constants.LOGICAL_FILE);
            }
            return hpccConnection;
        }

        return null;
    }
    
    @Override
    public int hashCode() {
        return (dashboardId == null) ? 0 : dashboardId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Dashboard other = (Dashboard) obj;
        if (dashboardId == null) {
            if (other.dashboardId != null) {
                return false;
            }
        } else if (!dashboardId.equals(other.dashboardId)) {
            return false;
        }
        return true;
    }
    
    public List<Portlet> getLiveCharts(){
        List<Portlet> liveCharts = new ArrayList<Portlet>();
        if(this.portletList != null) {
            for (Portlet portlet : this.portletList) {
                if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
                    liveCharts.add(portlet);
                }
            }
        }
        return liveCharts;
    }
    
    public Portlet getPortlet(int potletId){
        return this.getPortletList().stream().filter(portlet -> potletId ==((Portlet)portlet).getId()).findFirst().get();
    }

    @Override
    public String toString() {    

        StringBuilder buffer = new StringBuilder();
        buffer.append("Dashboard [sourceId=").append(sourceId).append(", layout=")
                .append(layout).append(", name=").append(name)
                .append(", columnCount=").append(columnCount).append(", dashboardId=")
                .append(dashboardId).append(", applicationId=").append(applicationId)
                .append(", dashboardState=").append(dashboardState)
                .append(", lastupdatedDate=").append(lastupdatedDate)
                .append(", sequence=").append(sequence).append(", showFiltersPanel=")
                .append(hasCommonFilter).append(", visibility=").append(visibility)
                .append(", role=").append(role).append(", isPersisted=").append(isPersisted)               
                .append(", portletList=").append(portletList).append("]");
        
        return buffer.toString();        
        
    }

    public String getFileType() {
        ChartService chartService = (ChartService) SpringUtil.getBean("chartService");
            for(Portlet portlet : this.getPortletList()){
                if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())
                        && Constants.CATEGORY_TEXT_EDITOR != chartService.getCharts().get(portlet.getChartType()).getCategory()
                        && Constants.CATEGORY_SCORED_SEARCH_TABLE != chartService.getCharts().get(portlet.getChartType()).getCategory()) {
                    if(!portlet.getChartData().getIsQuery()){
                        //used logical files for all the charts
                        return Constants.LOGICAL_FILE;
                    }else{
                      //used queries for all the charts
                        return Constants.QUERY;
                    }
                }
            }
        return null;
    }

    public Set<InputParam> getCommonQueryFilters() {
        return commonQueryFilters;
    }

    public void setCommonQueryFilters(Set<InputParam> commonQueryFilters) {
        this.commonQueryFilters = commonQueryFilters;
    }

    public void addCommonQueryFilter(InputParam inputparam) {
        initiateCommonFilters();
        commonQueryFilters.add(inputparam);
    }

    private void initiateCommonFilters() {
        if(commonQueryFilters == null) {
            commonQueryFilters = new LinkedHashSet<InputParam>();
        }
    }

    public void addCommonQueryFilters(List<InputParam> persistedGlobalInputParams) {
        initiateCommonFilters();
        commonQueryFilters.addAll(persistedGlobalInputParams);
    }

    public boolean showLocalFilter() {
        return showLocalFilter;
    }

    public void setShowLocalFilter(boolean showLocalFilter) {
        this.showLocalFilter = showLocalFilter;
    }

    public boolean isLockCommonFilter() {
        return lockCommonFilter;
    }

    public void setLockCommonFilter(boolean lockCommonFilter) {
        this.lockCommonFilter = lockCommonFilter;
    }

}
