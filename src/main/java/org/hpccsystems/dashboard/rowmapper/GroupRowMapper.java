package org.hpccsystems.dashboard.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hpccsystems.dashboard.entity.Group;
import org.springframework.jdbc.core.RowMapper;

public class GroupRowMapper implements RowMapper<Group> {

    @Override
    public Group mapRow(ResultSet rs, int rowNum) throws SQLException {
        Group group = new Group();
        group.setCode(rs.getString("group_code"));
        group.setName(rs.getString("group_name"));        
        return group;    
    }

}
