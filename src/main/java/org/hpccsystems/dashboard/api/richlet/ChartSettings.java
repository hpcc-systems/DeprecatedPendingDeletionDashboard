package org.hpccsystems.dashboard.api.richlet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.SpringUtil;

public class ChartSettings extends GenericRichlet {

    private static final Log LOG = LogFactory.getLog(ChartSettings.class);

    @Override
    public void service(Page page) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside Richlet.. ");
        }

        //TODO:remove hardcoding usercredential, when API call passes Credential
        UserCredential credential = new UserCredential("user", "user", Constants.CIRCUIT_APPLICATION_ID,false);
        Sessions.getCurrent().setAttribute("userCredential", credential);
        boolean isLoginSuccessful = true;
        
        StringBuilder url = new StringBuilder("/demo/?");            
            try{            
                Map<String, String[]> args = Executions.getCurrent().getParameterMap();
                url.append("source").append("=").append(args.get(Constants.SOURCE)[0]);
                
                //Authendicating the API user
                /*String userName = args.get(Constants.USERNAME)[0];
                String pwd =  args.get(Constants.CREDENTIAL)[0];
                boolean isLoginSuccessful = false;
                User user = null;
                LDAPAuthenticationService LDAPService = (LDAPAuthenticationService)SpringUtil.getBean("LDAPService");
                AuthenticationService  authenticationService = (AuthenticationService)SpringUtil.getBean("authenticationService");
                
                if("true".equals(Labels.getLabel("enableLDAP"))){
                    user = LDAPService.authenticate(userName, pwd);                    
                }else{
                    user = authenticationService.authenticate(userName, pwd);                
                }
                isLoginSuccessful = authenticationService.addUser(user, Constants.CIRCUIT_APPLICATION_ID);
                */
                LOG.debug("User authenticated .." + isLoginSuccessful);
                
            if (isLoginSuccessful) {
                credential = (UserCredential) Sessions.getCurrent().getAttribute("userCredential");
                
                if (args.containsKey(Constants.SOURCE_ID)
                        || args.containsKey(Constants.CIRCUIT_DASHBOARD_ID)) {
                    if (args.containsKey(Constants.SOURCE_ID)) {
                        url.append("&").append(Constants.SOURCE_ID).append("=").append(args.get(Constants.SOURCE_ID)[0]);
                    }
                    if (args.containsKey(Constants.CIRCUIT_DASHBOARD_ID)) {
                        url.append("&").append(Constants.CIRCUIT_DASHBOARD_ID).append("=")
                        .append(args.get(Constants.DB_DASHBOARD_ID)[0]);
                    }
                } else {
                    throw new Exception("Either source_id or dashboard_id must be passed to configure chart");
                }

                if (args.containsKey(Constants.CIRCUIT_CONFIG)) {
                    url.append("&").append("format").append("=").append(args.get(Constants.CHARTLIST_FORMAT)[0]);
                    url.append("&").append(Constants.CIRCUIT_CONFIG).append("=")
                            .append(args.get(Constants.CIRCUIT_CONFIG)[0]);

                    // Setting the role to user to Configure chart
                    credential.addRole(Constants.CIRCUIT_ROLE_CONFIG_CHART);
                } else {
                    // Chart type Should only be passed when not configuring
                    if (args.containsKey(Constants.CHART_TYPE)) {
                        url.append("&").append(Constants.CHART_TYPE).append("=")
                                .append(args.get(Constants.CHART_TYPE)[0]);
                    }
                    // Setting the role to user to View chart
                    credential.addRole(Constants.CIRCUIT_ROLE_VIEW_CHART);
                }

                // dashboardId validation for View Chart API call
                final String[] dashboardId = args.get(Constants.DB_DASHBOARD_ID);
                if (dashboardId != null) {
                    final String dashboardID = dashboardId[0];
                    final List<String> dashboardIdList = new ArrayList<String>();
                    dashboardIdList.add(dashboardID);
                    final DashboardService dashboardService = (DashboardService) SpringUtil.getBean(Constants.DASHBOARD_SERVICE);
                    final List<Dashboard> dashboardList = dashboardService.retrieveDashboardMenuPages(
                                    args.get(Constants.SOURCE)[0].toString(),/*user.getUserId()*/credential.getUserId(), dashboardIdList, null);
                    if (!(dashboardList != null && !dashboardList.isEmpty())) {
                        Clients.showNotification("Invalid DashboarId", false);
                        Sessions.getCurrent().invalidate();
                        return;
                    }
                }
            }else{
                Clients.showNotification("Username or Password are not correct.", true);
                return;
            }
            
        } catch (Exception ex) {
            Clients.showNotification(Labels.getLabel("malFormedUrlString"),false);
            LOG.error(Constants.EXCEPTION,ex);
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("URL formed to view API chart config screen -->"+ url.toString());
            LOG.debug("Creating API edit portlet screen...");
        }
        
        Executions.sendRedirect(url.toString());
    }
}
