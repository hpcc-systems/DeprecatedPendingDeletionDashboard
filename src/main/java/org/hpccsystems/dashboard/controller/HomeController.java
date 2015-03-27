package org.hpccsystems.dashboard.controller;

import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zkplus.spring.SpringUtil;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class HomeController extends SelectorComposer<Component>{
    private static final long serialVersionUID = 1L;
    
    
    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        AuthenticationService  authenticationService = (AuthenticationService) SpringUtil.getBean("authenticationService");
        
        if(Sessions.getCurrent().getAttributes().containsKey(Constants.REQUEST_PRAMS)) {
            authenticationService.getUserCredential().addRole(Constants.ROLE_API_VIEW_DASHBOARD);
        } else {
            authenticationService.getUserCredential().removeRole(Constants.ROLE_API_VIEW_DASHBOARD);
        }
        
        return super.doBeforeCompose(page, parent, compInfo);
    }
}
