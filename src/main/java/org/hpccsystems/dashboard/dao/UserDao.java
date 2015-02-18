package org.hpccsystems.dashboard.dao;

import org.hpccsystems.dashboard.entity.User;

public interface UserDao {
    boolean addUser(User user);
    
    boolean userExists(String userId);
}
