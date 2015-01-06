package org.hpccsystems.dashboard.dao;

import java.sql.SQLException;

import org.hpccsystems.dashboard.entity.User;

/**
 *  Dao class,has abstract methods for user authentication related DB hits
 *
 */
public interface  AuthenticationDao {
    
    public static final long serialVersionUID = 1L;
    
    User authendicateUser(String userName,String password) throws SQLException;
    
    void updateActiveFlag(User user) throws SQLException;

}
