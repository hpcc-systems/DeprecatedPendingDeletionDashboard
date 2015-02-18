package org.hpccsystems.dashboard.dao.impl;

import javax.sql.DataSource;

import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.UserDao;
import org.hpccsystems.dashboard.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserDaoImpl implements UserDao {

private JdbcTemplate jdbcTemplate;
    
    public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    
    @Override
    public boolean addUser(User user) {
        int rows = getJdbcTemplate().update(Queries.INSERT_USER, new Object[]{
                user.getAccount(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword()
        });
        return rows > 0;
    }

    @Override
    public boolean userExists(String userId) {
        return getJdbcTemplate().queryForList(Queries.GET_ALL_USER_IDS, String.class).contains(userId);
    }

}
