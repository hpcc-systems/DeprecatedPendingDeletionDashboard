package org.hpccsystems.dashboard.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.GroupDao;
import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.rowmapper.GroupRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
}
