package org.hpccsystems.dashboard.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.SpringUtil;

public class DashboardUtil { 
    private static final  Log LOG = LogFactory.getLog(DashboardUtil.class); 
    /**
     * Checks whether a column is numeric
     * @param column
     * @param dataType
     * @return
     */
    public static boolean checkNumeric(final String dataType) {
        boolean numericColumn = false;
            if(dataType.contains("integer")    || 
                    dataType.contains("real") || 
                    dataType.contains("decimal") ||  
                    dataType.contains("unsigned"))    {
                numericColumn = true;
            }
        return numericColumn;
    }   
   
    
    /**Checks for real/double data
     * @param dataType
     * @return boolean
     */
    public static boolean checkRealValue(final String dataType) {
        boolean realcColumn = false;
            if(dataType.contains("real")    || 
                    dataType.contains("decimal")){
                realcColumn = true;
            }
        return realcColumn;
    }
    
    public static boolean checkForMultipleDatasetFilter(final Set<Filter> chartDataFltr,String dataSetName) {
        Set<String> setToCheckMultiple=new HashSet<String>();
        setToCheckMultiple.add(dataSetName);
        for (Filter fltrObj : chartDataFltr) {
            setToCheckMultiple.add(fltrObj.getFileName());
        }
        return setToCheckMultiple.size()>1?true:false;
    }

    public static void redirectDashboardURI( Map<String, String[]> args){
        
        String source =args.get(Constants.SOURCE)[0];
        Boolean editRole = null;
        if(args.get(Constants.ROLE_EDIT) != null){
            editRole =Boolean.valueOf(args.get(Constants.ROLE_EDIT)[0]);
        }
        String[] dashboardIdArray = args.get(Constants.DB_DASHBOARD_ID); 

        UserCredential credential = (UserCredential) Sessions.getCurrent().getAttribute("userCredential");
        if(editRole == null || editRole ){
            credential.addRole(Constants.CIRCUIT_ROLE_VIEW_EDIT_DASHBOARD);
        }else if(editRole != null && !editRole){
            credential.addRole(Constants.ROLE_API_VIEW_DASHBOARD);
        }
        
        StringBuilder url = new StringBuilder("/demo/?");            
        url.append(Constants.SOURCE).append("=").append(source);
        for(String dashId : dashboardIdArray){
            url.append("&").append(Constants.DB_DASHBOARD_ID).append("=")
                .append(dashId);
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("URL from External/Circuit/Dashboard Share source : "+url);                
        }
        
        //dashboardId validation for View Dashboard API call
        if (dashboardIdArray != null) {
            final List<String> dashboardIdList = new ArrayList<String>();
            for (final String dashBoardId : dashboardIdArray) {
                dashboardIdList.add(dashBoardId);
            }
            final DashboardService dashboardService = (DashboardService) SpringUtil.getBean(Constants.DASHBOARD_SERVICE);
            final List<Dashboard> dashboardList = dashboardService.retrieveDashboardMenuPages(source, credential.getUserId(),
                            dashboardIdList, null);
            if (!(dashboardList != null && !dashboardList.isEmpty())) {
                Clients.showNotification("Invalid DashboarId", false);
                Sessions.getCurrent().invalidate();
                return;
            }
        }
        
        Executions.sendRedirect(url.toString());
    
    }
}
