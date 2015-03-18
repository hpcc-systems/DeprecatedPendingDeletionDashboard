package org.hpccsystems.dashboard.api.richlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zkplus.spring.SpringUtil;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.UserCredential;


public class ShareDashboard extends GenericRichlet {

    private static final  Log LOG = LogFactory.getLog(ShareDashboard.class);
        
    @Override
    public void service(Page page) throws Exception {
        
        AuthenticationService authenticationService =(AuthenticationService)SpringUtil.getBean("authenticationService");
        UserCredential cre = authenticationService.getUserCredential();
        if(cre==null || cre.isAnonymous()) {
            String dashbordId =Executions.getCurrent().getParameter(Constants.DB_DASHBOARD_ID);
            String applnId =Executions.getCurrent().getParameter(Constants.SOURCE);
            StringBuilder uri = new StringBuilder();
            uri.append("/login.zhtml?").append(Constants.SOURCE).append("=")
                    .append(applnId).append("&").append(Constants.DB_DASHBOARD_ID)
                    .append("=").append(dashbordId).append("&")
                    .append(Constants.ROLE_EDIT).append("=false").append("&").append(Constants.DASHBOARD_SHARE)
                    .append("=true");
            if(LOG.isDebugEnabled()){
                LOG.debug("Dashboard Shrae URI -->"+uri);
            }
                Executions.sendRedirect(uri.toString());
                return;
        }
       
    }
}
