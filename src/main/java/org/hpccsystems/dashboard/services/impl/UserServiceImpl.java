package org.hpccsystems.dashboard.services.impl;

import org.hpccsystems.dashboard.dao.UserDao;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.hpccsystems.dashboard.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceImpl implements UserService {

    private UserDao userDao;
    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean userExist(String userId) {
        return userDao.userExists(userId);
    }

    @Override
    public boolean addUser(User user) throws EncryptDecryptException {
        return userDao.addUser(user);
    }
    
    @Override
    public void resetPassword(User user) throws EncryptDecryptException {
        userDao.resetPassword(user);
    }

}
