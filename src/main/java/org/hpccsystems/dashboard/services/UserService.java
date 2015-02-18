package org.hpccsystems.dashboard.services;

import org.hpccsystems.dashboard.entity.User;

public interface UserService  {

     boolean userExist(String userId);
     
     boolean addUser(User user);
}
