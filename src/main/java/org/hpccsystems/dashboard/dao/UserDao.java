package org.hpccsystems.dashboard.dao;

import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;

public interface UserDao {
    boolean addUser(User user) throws EncryptDecryptException;
    
    boolean userExists(String userId);

    void resetPassword(User user) throws EncryptDecryptException;
}
