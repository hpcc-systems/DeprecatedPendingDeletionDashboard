package org.hpccsystems.dashboard.services;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.hpccsystems.dashboard.entity.User;
import org.springframework.dao.DataAccessException;

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
     *     true on successful login
     */
    User authenticate(final String account, final String password);    
    
    /**get current user credential**/
     UserCredential getUserCredential();

    /**
     * service call to logout an user by resetting the active flag
     * @param object
     */
    void logout(Object object)throws Exception;
    
    /**
     * Adds user to session
     * @param user
     * @throws Exception 
     */
    public boolean login(User user,String appId) throws DataAccessException, RemoteException, ServiceException;
    
}
