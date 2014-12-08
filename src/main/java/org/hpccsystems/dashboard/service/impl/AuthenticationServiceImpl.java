package org.hpccsystems.dashboard.service.impl;

import java.util.List;

import org.hpccsystems.dashboard.dao.UserDao;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service("authenticationService") 
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthenticationServiceImpl implements AuthenticationService{

	@Autowired
	UserDao userDao;
	
	@Override
	public boolean authenticate(String userId, String pasword) {
		
		return false;
	}

	@Override
	public List<Application> getAllApplications() {
		return userDao.getAllApplications();
	}

}
