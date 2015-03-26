package org.hpccsystems.dashboard.api.richlet;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.services.UserCredential;
import org.hpccsystems.dashboard.util.DashboardUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;

public class ViewDashboard extends GenericRichlet {

    private static final  Log LOG = LogFactory.getLog(ViewDashboard.class);
        
    @Override
    public void service(Page page) throws Exception {
        
        //TODO:remove hardcoding usercredential, when API call passes Credential
           // UserCredential credential = new UserCredential("user", "user", Constants.CIRCUIT_APPLICATION_ID,false);
            
            UserCredential credential = new UserCredential("admin", "Administrator", "demo",false);
            Sessions.getCurrent().setAttribute("userCredential", credential);
            boolean isLoginSuccessful = true;
            
        try {
            
            Map<String, String[]> args = Executions.getCurrent().getParameterMap();
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
                DashboardUtil.redirectDashboardURI(args);
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
