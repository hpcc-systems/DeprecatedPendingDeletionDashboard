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

public class ViewDashboard extends GenericRichlet {

    private static final  Log LOG = LogFactory.getLog(ViewDashboard.class);
        
    @Override
    public void service(Page page) throws Exception {
        
        //TODO:remove hardcoding usercredential, when API call passes Credential
            UserCredential credential = new UserCredential("user", "user", Constants.CIRCUIT_APPLICATION_ID,false);
            Sessions.getCurrent().setAttribute("userCredential", credential);
            boolean isLoginSuccessful = true;
            
        try {
            
            Map<String, String[]> args = Executions.getCurrent().getParameterMap();
            String source =Executions.getCurrent().getParameter(Constants.SOURCE);
            String[] dashboardIdArray = args.get(Constants.DB_DASHBOARD_ID);    
            /*UserCredential credential;
            User user = null;            
            
            //Authendicating the API user
            String userName = args.get(Constants.USERNAME)[0];
            String pwd =  args.get(Constants.CREDENTIAL)[0];
            boolean isLoginSuccessful = false;
            LDAPAuthenticationService LDAPService = (LDAPAuthenticationService)SpringUtil.getBean("LDAPService");
            AuthenticationService  authenticationService = (AuthenticationService)SpringUtil.getBean("authenticationService");
            
            if("true".equals(Labels.getLabel("enableLDAP"))){
                user = LDAPService.authenticate(userName, pwd);                    
            }else{
                user = authenticationService.authenticate(userName, pwd);                
            }
            
            isLoginSuccessful = authenticationService.addUser(user, Constants.CIRCUIT_APPLICATION_ID); */
            LOG.debug("User authenticated .." + isLoginSuccessful);
            
            if(isLoginSuccessful){
                credential = (UserCredential) Sessions.getCurrent().getAttribute("userCredential");                
                credential.addRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD);
                
                StringBuilder url = new StringBuilder("/demo/?");            
                url.append(Constants.SOURCE).append("=").append(source);
                for(String dashId : dashboardIdArray){
                    url.append("&").append(Constants.DB_DASHBOARD_ID).append("=")
                        .append(dashId);
                }
                if(LOG.isDebugEnabled()){
                    LOG.debug("URL from External/Circuit source : "+url);                
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
            }else{
                    Clients.showNotification("Username or Password are not correct.", true);
                    return;
            }
        } catch (Exception ex) {            
            Clients.showNotification(Labels.getLabel("malFormedUrlString"), false);
            LOG.error(Constants.EXCEPTION, ex);
            return;            
            }
    }
}
