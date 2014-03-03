package org.hpccsystems.dashboard.services;

/**
 * AuthenticationService is used to authenticate the Dashboard user's.
 *
 */
public interface AuthenticationService  {

	
	 /**
	  * Authenticates, and on successful authentication, sets UserCredential object to session
	  * 
	 * @param account
	 * @param password
	 * @param applicationId
	 * @return
	 * 	true on successful login
	 */
	boolean login(final String account, final String password, final String applicationId);	
	
	/**get current user credential**/
	 UserCredential getUserCredential();

	/**
	 * service call to logout an user by resetting the active flag
	 * @param object
	 */
	void logout(Object object)throws Exception;
	
	
	
}
