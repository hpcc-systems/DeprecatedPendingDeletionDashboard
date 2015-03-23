package org.hpccsystems.dashboard.controller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.hpccsystems.dashboard.services.impl.AuthenticationServiceImpl;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Initiator;

public class AuthenticationInit implements Initiator {
     //services
    AuthenticationService authService = new AuthenticationServiceImpl();
    
    private static final  Log LOG = LogFactory.getLog(AuthenticationInit.class); 
    
    @Override
    public void doInit(Page page, Map<String, Object> args) {
            UserCredential cre = authService.getUserCredential();
            if(cre==null || cre.isAnonymous()){
                if(LOG.isDebugEnabled()){
                    LOG.debug("User Authentication failed.." );
                    LOG.debug("Annonimity of user.." + cre.isAnonymous());
                    LOG.debug("Credentials - Account ->" + cre.getUserId() );
                    LOG.debug("Credentials - Name ->" + cre.getUserName() );
                    LOG.debug("Request path ->" + page.getRequestPath());
                }
                
                // Adding request path to session
                Sessions.getCurrent().setAttribute(Constants.REQUEST_PATH, page.getRequestPath());
                
                //Redirects to login page
                Executions.sendRedirect("/login.zhtml");
                return;
            }
    }
}
