package org.hpccsystems.dashboard.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DashoardLinkController extends SelectorComposer<Window>{

    private static final long serialVersionUID = 1L;
    private static final  Log LOG = LogFactory.getLog(DashoardLinkController.class);
    private Dashboard dashboard;
    
    @WireVariable
    private AuthenticationService authenticationService;
    
    @Wire
    private Textbox linkTextbox;
    
    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        dashboard =(Dashboard) Executions.getCurrent().getArg().get(Constants.DASHBOARD);
        if(LOG.isDebugEnabled()){
            LOG.debug("Generating link for dashboard: "+dashboard);
        }
        StringBuilder link = new StringBuilder();
        link.append(Constants.HTTP)
                .append(Executions.getCurrent().getServerName())
                .append(":")
                .append(Executions.getCurrent().getServerPort())
                .append(Executions.getCurrent().getContextPath())
                .append("/api/share_dashboard/?")
                .append(Constants.SOURCE)
                .append("=")
                .append(authenticationService.getUserCredential()
                        .getApplicationId()).append("&")
                .append(Constants.DB_DASHBOARD_ID).append("=")
                .append(dashboard.getDashboardId()).append("&")
                .append(Constants.ROLE_EDIT).append("=")
                .append(Constants.FALSE).append("&")
                .append(Constants.DASHBOARD_SHARE).append("=")
                .append(Constants.TRUE);
        linkTextbox.setValue(link.toString());
       
    }
    
    
}