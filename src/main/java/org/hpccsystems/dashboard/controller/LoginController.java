/* 
	Description:
		ZK Essentials
	History:
		Created by dennis

Copyright (C) 2012 Potix Corporation. All Rights Reserved.
*/
package org.hpccsystems.dashboard.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;


@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class LoginController extends SelectorComposer<Component> {
	
	private final static Log log = LogFactory.getLog(LoginController.class);
	
	private static final long serialVersionUID = 1L;
	
	@WireVariable
	private DashboardService dashboardService;
	
	
	//wire components
	@Wire
	Textbox account;
	@Wire
	Textbox password;
	@Wire
	Label message;
	
	@Wire
	Listbox apps;
	
	//services
	AuthenticationService authService = new AuthenticationServiceDashboardImpl();
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		
		super.doAfterCompose(comp);
		
		if (log.isInfoEnabled()) {
			log.info("Handling 'doAfterCompose' in LoginController");
		}
		

		List<String> carsList = new ArrayList<String>(dashboardService.retrieveApplicationIds());

		ListModelList<String> appModel = new ListModelList<String>(carsList);
		apps.setModel(appModel);
		

	}


	@Listen("onClick=#login; onOK=#loginWin")
	public void doLogin(){
		
		if (log.isInfoEnabled()) {
			log.info("Handling 'doLogin' in LoginController");
		}

		String nm = account.getValue();
		String pd = password.getValue();

		if(!authService.login(nm,pd)){
			message.setValue("account or password are not correct.");
			return;
		}
		UserCredential cre= authService.getUserCredential();
		message.setValue("Welcome, "+cre.getName());
		message.setSclass("");
		
		Executions.sendRedirect("/demo/");
		
	}




	
	
}
