package org.hpccsystems.dashboard.api.richlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zkplus.spring.SpringUtil;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.UserCredential;


public class ShareDashboard extends GenericRichlet {

    private static final  Log LOG = LogFactory.getLog(ShareDashboard.class);
        
    @Override
    public void service(Page page) throws Exception {
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("Request path - > " + page.getRequestPath());
        }
        
        AuthenticationService authenticationService =(AuthenticationService)SpringUtil.getBean("authenticationService");
        UserCredential cre = authenticationService.getUserCredential();
        if(cre==null || cre.isAnonymous()) {
            Sessions.getCurrent().setAttribute(Constants.REQUEST_PATH, page.getRequestPath());
            Executions.sendRedirect("/login.zhtml");
        } else {
            Executions.sendRedirect("/demo?share=" + page.getRequestPath().substring(page.getRequestPath().lastIndexOf('/') + 1));
        }
       
    }
}
