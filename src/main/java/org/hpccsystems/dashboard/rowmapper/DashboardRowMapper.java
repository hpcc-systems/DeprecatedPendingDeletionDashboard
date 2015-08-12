package org.hpccsystems.dashboard.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.springframework.jdbc.core.RowMapper;

public class DashboardRowMapper implements RowMapper<Dashboard> {

    @Override
    public Dashboard mapRow(ResultSet rs, int rowNum) throws SQLException {
        Dashboard dashboard = new Dashboard();
        dashboard.setDashboardId(rs.getInt("dashboard_id"));
        dashboard.setApplicationId("application_id");
        dashboard.setName(rs.getString("dashboard_name"));
        dashboard.setColumnCount(rs.getInt("column_count"));
        dashboard.setSourceId(rs.getString("source_id"));
        dashboard.setVisibility(rs.getInt("visibility"));
        dashboard.setHasCommonFilter(rs.getBoolean("common_filter"));
        dashboard.setShowLocalFilter(rs.getBoolean("show_localfilter"));
        dashboard.setLockCommonFilter(rs.getBoolean("lock_commonfilter"));
        dashboard.setLockaddCommonFilter(rs.getBoolean("lock_add_commonfilter"));
        dashboard.setLockChartTitle(rs.getBoolean("lock_charttitle"));
        dashboard.setLastupdatedDate(rs.getTimestamp("last_updated_date"));
        return dashboard;
    }

}
