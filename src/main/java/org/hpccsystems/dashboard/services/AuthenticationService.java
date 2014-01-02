package org.hpccsystems.dashboard.services;

import org.hpccsystems.dashboard.entity.User;


/**
 * AuthenticationService is used to authenticate the Dashboard user's.
 *
 */
public interface AuthenticationService  {

/**login with account and password**/
	 boolean login(final String account, final String password);	
	
	/**get current user credential**/
	 UserCredential getUserCredential();
	 
	/**
	 * Method to authendicate user based on DB values
	 * @param userName
	 * @param Password
	 * @return boolean
	 */
	User  authendicateUser(String userName,String Password);
	
	/**
	 * service call to logout an user by resetting the active flag
	 * @param object
	 */
	void logout(Object object);
	
	
	
}
