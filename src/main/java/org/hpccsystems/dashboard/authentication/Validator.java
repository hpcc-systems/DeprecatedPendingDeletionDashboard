package org.hpccsystems.dashboard.authentication;

import java.util.Map;

import org.hpccsystems.dashboard.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Initiator;
import org.zkoss.zkplus.spring.SpringUtil;


public class Validator implements Initiator {

	private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);
	
	@Override
	public void doInit(Page page, Map<String, Object> arg1) throws Exception {
		AuthenticationService authenticationService = (AuthenticationService) SpringUtil.getBean("authenticationService");
		
		LOGGER.debug("Auth Service - {}", authenticationService);
		
		if( authenticationService.getUserCredential() == null) {
		    Executions.sendRedirect("login.zhtml");
		}
	}
	
}
