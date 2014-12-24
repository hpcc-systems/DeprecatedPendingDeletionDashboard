package org.hpccsystems.dashboard.service.impl;

import java.util.List;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.dao.UserDao;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.UserCredential;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;


@Service("authenticationService") 
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthenticationServiceImpl implements AuthenticationService{

	@Autowired
	UserDao userDao;
	
	@Override
	public boolean authenticate(String userId, String password) {
		return userDao.validatePassword(userId, password);
	}

	@Override
	public List<Application> getAllApplications() {
		return userDao.getAllApplications();
	}

	@Override
	public UserCredential getUserCredential() {
		Session session = Executions.getCurrent().getSession();
		UserCredential userCredential = (UserCredential)session.getAttribute(Constants.USER_CREDENTIAL);
		return userCredential;
	}

    @Override
    public void setUserCredential(UserCredential userCredential) {
        Session session = Executions.getCurrent().getSession();
        session.setAttribute(Constants.USER_CREDENTIAL, userCredential);
    }

}
