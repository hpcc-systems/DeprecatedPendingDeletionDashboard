package org.hpccsystems.dashboard.services.impl;

import java.io.Serializable;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.dao.AuthenticationDao;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

/**
 * AuthenticationServiceImpl is implementation class for AuthenticationService.
 *
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService,Serializable{
	private static final long serialVersionUID = 1L;
	
	private static final  Log LOG = LogFactory.getLog(AuthenticationServiceImpl.class); 
	
	AuthenticationDao authendicationDao;
	
	public AuthenticationDao getAuthendicationDao() {
		return authendicationDao;
	}
	@Autowired
	public void setAuthendicationDao(AuthenticationDao authendicationDao) {
		this.authendicationDao = authendicationDao;
	}
	
	public UserCredential getUserCredential(){
		final Session sess = Sessions.getCurrent();
		UserCredential cre = (UserCredential)sess.getAttribute("userCredential");
		if(cre==null){
			cre = new UserCredential();//new a anonymous user and set to session
			sess.setAttribute("userCredential",cre);
		}
		return cre;
	}
	public boolean login(final String name, final String passWord) {
		return false;
	}

	public void logout(Object object) {
		if(object!= null)
		{				
		try {
			authendicationDao.updateActiveFlag((User)object);
		} catch (SQLException e) {
			LOG.error("SQL Exception", e);
		}		
		}
	}
	
	public User authendicateUser(String userName, String Password) {
		User user = null;
		try {
			user = authendicationDao.authendicateUser(userName,Password);
		}  catch (SQLException e) {
			LOG.error("SQLException", e);
		}		
		return user;
	}
}
