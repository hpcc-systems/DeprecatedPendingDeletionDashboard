package org.hpccsystems.dashboard.authentication;

import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class LoginController extends SelectorComposer<Component>{

	private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

	@Wire
	private Listbox apps;
	
	@WireVariable
	private AuthenticationService authenticationService;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);	
		
		LOGGER.debug("Class - {}", SpringUtil.getBean("authenticationService"));
		apps.setModel(new ListModelList<Application>(authenticationService.getAllApplications()));
		
	}
}
