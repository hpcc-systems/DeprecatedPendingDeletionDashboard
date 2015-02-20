package org.hpccsystems.dashboard.services;

import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;

public interface UserService  {

     boolean userExist(String userId);
     
     boolean addUser(User user) throws EncryptDecryptException;
     
     void resetPassword(User user) throws EncryptDecryptException;
}
