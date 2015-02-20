package org.hpccsystems.dashboard.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.GroupDao;
import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.rowmapper.GroupRowMapper;
import org.hpccsystems.dashboard.rowmapper.UserRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class GroupDaoImpl implements GroupDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void addGroup(final int dashboardId, final Group group) {
        jdbcTemplate.update(Queries.INSERT_GROUP,
                new Object[] { dashboardId, group.getCode(), group.getName(),
                        group.getRole(), group.getLastUpdatedDate() });

    }

    @Override
    public void deleteGroup(final int dashboardId, final Group group) {
        jdbcTemplate.update(Queries.DELETE_GROUP, new Object[] { dashboardId,
                group.getCode() });

    }

    @Override
    public List<Group> selectGroups(Integer dashboardId) throws DataAccessException, SQLException{
        return this.jdbcTemplate.query(Queries.SELECT_GROUP, new Object[] { dashboardId }, new RowMapper<Group>() {
            public Group mapRow(ResultSet rs, int rowNum) throws SQLException {
                Group group = new Group();
                group.setCode(rs.getString("group_code"));
                group.setName(rs.getString("group_name"));
                group.setRole(rs.getString("role"));
                return group;
            }
        });
    }
    
    @Override
    public void updateGroupRole(final int dashboardId, final Group group) {
        jdbcTemplate.update(Queries.UPDATE_GROUP_ROLE,
                new Object[] { group.getRole(),dashboardId, group.getCode()});
    }

    @Override
    public List<Group> getGroups() throws DataAccessException {
        return  jdbcTemplate.query(Queries.GET_ALL_GROUPS, new GroupRowMapper());    
    }

    @Override
    public List<Group> getGroups(String userId) throws DataAccessException {
        return  jdbcTemplate.query(Queries.GET_USER_GROUPS, new GroupRowMapper(),new Object[] { userId });    
    }

    @Override
    public List<User> getGroupUsers(Group selectdGroup) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT user.id,user.first_name,user.last_name,user.password,user.active_flag FROM user_details as user ")
                .append("JOIN user_groups ON user_groups.user_id = user.id  ")
                .append("where user_groups.group_code = ? ");
        return jdbcTemplate.query(query.toString(), new UserRowMapper(),new Object[] { selectdGroup.getCode() });
    }

    @Override
    public List<User> getAllUser() {
        return jdbcTemplate.query(Queries.GET_ALL_USER, new UserRowMapper());
    }

    @Override
    public void addUser(Set<User> selectedUsers,Group group) {
        jdbcTemplate.batchUpdate(Queries.INSERT_GROUP_USER,new BatchPreparedStatementSetter() {
            List<User> users = new ArrayList<User>(selectedUsers);
            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                User user = users.get(index);
                ps.setString(1, group.getCode());
                ps.setString(2, user.getId());
            }
            
            @Override
            public int getBatchSize() {
                return selectedUsers.size();
            }

          });
    }

    @Override
    public void addgroup(Group newGroup) {
        jdbcTemplate.update(Queries.INSERT_NEW_GROUP, new Object[]{newGroup.getCode(),newGroup.getName()});
    }    
}
