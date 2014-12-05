package org.hpccsystems.dashboard.services;

import javax.naming.NamingException;

import org.hpccsystems.dashboard.entity.User;

/**
 * Class provides services to authenticate LDAP users
 *
 */
public interface LDAPAuthenticationService {
    
    /**
     * Service to authenticate LDAP user
     * @param userName
     * @param password
     * @return User
     * @throws Exception
     */
    public User authenticate(String userName,String password) throws NamingException;
    

}
