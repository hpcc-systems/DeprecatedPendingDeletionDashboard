package org.hpccsystems.dashboard.dao.impl;

import javax.sql.DataSource;

import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.UserDao;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.hpccsystems.dashboard.util.EncryptDecrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserDaoImpl implements UserDao {

private JdbcTemplate jdbcTemplate;
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    
    @Override
    public boolean addUser(User user) throws EncryptDecryptException {
        String encryptedPwd = encryptPassword(user.getPassword());
        int rows = jdbcTemplate.update(Queries.INSERT_USER, new Object[]{
                user.getAccount(),
                user.getFirstName(),
                user.getLastName(),
                encryptedPwd
        });
        return rows > 0;
    }

    @Override
    public boolean userExists(String userId) {
        return jdbcTemplate.queryForList(Queries.GET_ALL_USER_IDS, String.class).contains(userId);
    }

    @Override
    public void resetPassword(User user) throws EncryptDecryptException {
       String encryptedPwd = encryptPassword(user.getPassword());
        jdbcTemplate.update(Queries.RESET_USER_PASSWORD, new Object[] {
                encryptedPwd, user.getId() });
    }


    private String encryptPassword(String pwd) throws EncryptDecryptException {
        EncryptDecrypt encrypter = null;
        encrypter = new EncryptDecrypt("");
        String encryptedPwd = encrypter.encrypt(pwd);
        return encryptedPwd;
    }

}
