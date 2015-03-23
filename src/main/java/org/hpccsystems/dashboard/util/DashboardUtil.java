package org.hpccsystems.dashboard.util;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.cluster.ClusterData;
import org.hpccsystems.dashboard.chart.entity.ChartData;
import org.hpccsystems.dashboard.chart.entity.Filter;
import org.hpccsystems.dashboard.chart.entity.TableData;
import org.hpccsystems.dashboard.chart.entity.XYChartData;
import org.hpccsystems.dashboard.chart.gauge.GaugeChartData;
import org.hpccsystems.dashboard.chart.tree.entity.TreeData;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.SpringUtil;

public class DashboardUtil { 


    private static final  Log LOG = LogFactory.getLog(DashboardUtil.class); 
    
    private static final byte[] SALT = { (byte) 0x21, (byte) 0x21, (byte) 0xF0, (byte) 0x55, (byte) 0xC3, (byte) 0x9F, (byte) 0x5A, (byte) 0x75 };
    private final static int ITERATION_COUNT = 31;
    private static final String ALGORITHM = "PBEWithMD5AndDES";
    private static final String PASSCODE = "genericLN";
    
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


    public static ChartData getChartData(Portlet portlet) {
        
        ChartService chartService = (ChartService) SpringUtil.getBean("chartService"); 
        
        ChartData chartData = null;
        //TODO enable when common filter enabled for tree
        if(Constants.CATEGORY_TABLE == chartService.getCharts().get(portlet.getChartType())
                .getCategory()){
            chartData = (TableData) portlet.getChartData();
        }else if (Constants.CATEGORY_XY_CHART == chartService.getCharts().get(portlet.getChartType())
                .getCategory()    || Constants.CATEGORY_PIE == chartService.getCharts().get(portlet.getChartType())
                        .getCategory() || Constants.CATEGORY_USGEO == chartService.getCharts().get(portlet.getChartType())
                                .getCategory()) {
            chartData =  (XYChartData) portlet.getChartData();
        } else if (Constants.CATEGORY_HIERARCHY ==  chartService.getCharts().get(portlet.getChartType())
                .getCategory()) {    
            chartData = (TreeData) portlet.getChartData();
        }else if(Constants.CATEGORY_CLUSTER ==  chartService.getCharts().get(portlet.getChartType())
                .getCategory()){
             chartData = (ClusterData) portlet.getChartData();
        }
        else if(Constants.CATEGORY_GAUGE ==  chartService.getCharts().get(portlet.getChartType())
                .getCategory()){
             chartData = (GaugeChartData) portlet.getChartData();
            
        } 
        return chartData;
    }
    
    public static String createShareParam(String dashboardId) throws Exception {
        KeySpec keySpec = new PBEKeySpec(PASSCODE.toCharArray(), SALT, ITERATION_COUNT);
        AlgorithmParameterSpec paramSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);

        SecretKey key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec);

        Cipher ecipher = Cipher.getInstance(key.getAlgorithm());
        ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

        byte[] enc = ecipher.doFinal(dashboardId.getBytes());

        String res = new String(Base64.encodeBase64(enc));
        // escapes for url
        res = res.replace('+', '-').replace('/', '_').replace("%", "%25").replace("\n", "%0A");

        return res;
    }
    
    public static String extractShareParam(String param) {
        try {
            String input = param.replace("%0A", "\n").replace("%25", "%").replace('_', '/').replace('-', '+');

            byte[] dec = Base64.decodeBase64(input.getBytes());

            KeySpec keySpec = new PBEKeySpec(PASSCODE.toCharArray(), SALT, ITERATION_COUNT);
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(SALT, ITERATION_COUNT);

            SecretKey key = SecretKeyFactory.getInstance(ALGORITHM).generateSecret(keySpec);

            Cipher dcipher = Cipher.getInstance(key.getAlgorithm());
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

            byte[] decoded = dcipher.doFinal(dec);

            String result = new String(decoded);
            return result;

        } catch (Exception e) {
            LOG.error(Constants.EXCEPTION + e);
        }

        return null;
    }
}
