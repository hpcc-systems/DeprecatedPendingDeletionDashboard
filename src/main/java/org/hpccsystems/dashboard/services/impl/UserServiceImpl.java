package org.hpccsystems.dashboard.services.impl;

import org.hpccsystems.dashboard.dao.UserDao;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceImpl implements UserService {

    private UserDao userDao;

    @Override
    public boolean userExist(String userId) {
        return getUserDao().userExists(userId);
    }

    @Override
    public boolean addUser(User user) {
        return getUserDao().addUser(user);
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

}
