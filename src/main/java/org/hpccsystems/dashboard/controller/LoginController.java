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
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.ApplicationService;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;


/**
 * LoginController class is used to handle the login activities for Dashboard project
 *  and controller class for login.zul.
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class LoginController extends SelectorComposer<Component> {
	
	private static final  Log LOG = LogFactory.getLog(LoginController.class);
	
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
	@Wire
	Button login;
	@WireVariable
	AuthenticationService  authenticationService;
	
	@WireVariable
	ApplicationService applicationService;
	
	
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		super.doAfterCompose(comp);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Handling 'doAfterCompose' in LoginController");
			LOG.debug("dashboardService:loginctrler -->"+dashboardService);
		}
		try
		{			
		final List<Application> applicationList = new ArrayList<Application>(applicationService.retrieveApplicationIds());
		final ListModelList<Application> appModel = new ListModelList<Application>(applicationList);
		apps.setModel(appModel);
		}
		catch(Exception ex)
		{
			Clients.showNotification("Unable to retrieve applications from DB. Please try reloading the page", false);
			LOG.error("Exception while fetching applications from DB", ex);
		}
	}
	
	@Listen("onClick=#login; onOK=#loginWin")
	public void doLogin(){
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("Handling 'doLogin' in LoginController");
		}

		final String name = account.getValue();
		final String passWord = password.getValue();
		User user = null;
		try	{
			user =authenticationService.authendicateUser(name,passWord);
			LOG.debug("User authenticated sucessfully..");
		} catch(Exception ex) {
			Clients.showNotification("Your login attempt failed. Please try again", false);
			LOG.error("Exception while authendicating user in doLogin()", ex);
		}
		
		if(user != null) {
			if(!user.isValidUser()){
				message.setValue("account or password are not correct.");
				return;
			}
			/*if("Y".equals(user.getActiveFlag()))
				{
				message.setValue("Hi "+user.getFullName()+" you have already logged in.");
				return;
				}*/
		} else	{
			message.setValue("Account or Password are not correct.");
			return;
		}
		
		//Fetching the present application Id and setting into session
		LOG.debug("the present application Id and setting into session");
		final Session session = Sessions.getCurrent();
		session.setAttribute("sourceid", apps.getItemAtIndex(apps.getSelectedIndex()).getValue());
		session.setAttribute("source", apps.getItemAtIndex(apps.getSelectedIndex()).getLabel());
		session.setAttribute("user", user);
		message.setValue("Welcome, "+user.getFullName());
		message.setSclass("");
		
		//Setting current user to session
		UserCredential cre = new UserCredential(user.getFullName(), user.getFullName());
		session.setAttribute("userCredential",cre);
		
		LOG.debug("Loged in. sending redirect...");
		Executions.sendRedirect("/demo/");
	}
}
