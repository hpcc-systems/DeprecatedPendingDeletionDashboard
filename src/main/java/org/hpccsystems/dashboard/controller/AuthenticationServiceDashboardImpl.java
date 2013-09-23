/* 
	Description:
		ZK Essentials
	History:
		Created by dennis

Copyright (C) 2012 Potix Corporation. All Rights Reserved.
*/
package org.hpccsystems.dashboard.controller;

import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.profile.AuthenticationServiceImpl;
import org.hpccsystems.dashboard.profile.UserInfoServiceImpl;
import org.hpccsystems.dashboard.services.UserCredential;
import org.hpccsystems.dashboard.services.UserInfoService;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

public class AuthenticationServiceDashboardImpl extends AuthenticationServiceImpl{
	
	
	private static final long serialVersionUID = 1L;
	
	UserInfoService userInfoService = new UserInfoServiceImpl();
	
	@Override
	public boolean login(String nm, String pd) {
		User user = userInfoService.findUser(nm);
		//a simple plan text password verification
		if(user==null || !user.getPassword().equals(pd)){
			return false;
		}
		
		Session sess = Sessions.getCurrent();
		UserCredential cre = new UserCredential(user.getAccount(),user.getFullName());
		//just in case for this demo.
		if(cre.isAnonymous()){
			return false;
		}
		sess.setAttribute("userCredential",cre);
		
		//TODO handle the role here for authorization
		return true;
	}
	
	@Override
	public void logout() {
		Session sess = Sessions.getCurrent();
		sess.removeAttribute("userCredential");
	}
	

}
