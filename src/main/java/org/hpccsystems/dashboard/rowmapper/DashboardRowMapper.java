package org.hpccsystems.dashboard.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.springframework.jdbc.core.RowMapper;

public class DashboardRowMapper implements RowMapper<Dashboard> {
    Dashboard dashboard;
    @Override
    public Dashboard mapRow(ResultSet rs, int index) throws SQLException {
        dashboard = new Dashboard();
        dashboard.setId(rs.getInt("id"));
        dashboard.setApplicationId(rs.getString("application_id"));
        dashboard.setHpccId(rs.getString("hpcc_id"));
        dashboard.setName(rs.getString("name"));
        dashboard.setVisiblity(rs.getInt("visibility"));
        dashboard.setCompositionName(rs.getString("composition_name"));
        return dashboard;
    }

}
