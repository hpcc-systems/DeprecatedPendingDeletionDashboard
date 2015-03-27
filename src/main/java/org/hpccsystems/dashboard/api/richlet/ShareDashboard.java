package org.hpccsystems.dashboard.api.richlet;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.RequestParams;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zkplus.spring.SpringUtil;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.hpccsystems.dashboard.util.DashboardUtil;


public class ShareDashboard extends GenericRichlet {

    private static final  Log LOG = LogFactory.getLog(ShareDashboard.class);
        
    @Override
    public void service(Page page) throws Exception {
        
        if(LOG.isDebugEnabled()) {
            LOG.debug("Request path - > " + page.getRequestPath());
        }
        
        Map<String, String[]> params = Executions.getCurrent().getParameterMap();
        if(params.get("id") == null) {
            return;
        }
        RequestParams requestParams = new RequestParams();
        requestParams.setDashbaordId(params.get("id")[0]);
        requestParams.setInputParams(DashboardUtil.extractInputParams(params));
        Sessions.getCurrent().setAttribute(Constants.REQUEST_PRAMS, requestParams);
        
        AuthenticationService authenticationService =(AuthenticationService)SpringUtil.getBean("authenticationService");
        UserCredential cre = authenticationService.getUserCredential();
        
        if(cre==null || cre.isAnonymous()) {
            Executions.sendRedirect("/login.zhtml");
        } else {
            Executions.sendRedirect("/demo");
        }
       
    }
}
