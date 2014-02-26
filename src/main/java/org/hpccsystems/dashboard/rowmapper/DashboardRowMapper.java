package org.hpccsystems.dashboard.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.springframework.jdbc.core.RowMapper;

public class DashboardRowMapper implements RowMapper<Dashboard> {

	@Override
	public Dashboard mapRow(ResultSet rs, int rowNum) throws SQLException {
		 Dashboard dashboard = new Dashboard();
		 dashboard.setApplicationId("application_id");
		 dashboard.setName(rs.getString("dashboard_name"));
		 dashboard.setDashboardId(rs.getInt("dashboard_id"));
		 dashboard.setColumnCount(rs.getInt("column_count"));
		 dashboard.setSourceId(rs.getString("source_id"));
		 dashboard.setLastupdatedDate(rs.getTimestamp("last_updated_date"));
		return dashboard;
	}

}
