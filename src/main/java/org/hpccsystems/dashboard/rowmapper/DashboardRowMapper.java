package org.hpccsystems.dashboard.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.springframework.jdbc.core.RowMapper;

public class DashboardRowMapper implements RowMapper<Dashboard> {

	@Override
	public Dashboard mapRow(ResultSet rs, int rowNum) throws SQLException {
		 Dashboard dashboard = new Dashboard();
		 dashboard.setName(rs.getString("DASHBOARD_NAME"));
		 dashboard.setDashboardId(rs.getInt("DASHBOARD_ID"));
		 dashboard.setDashboardState(rs.getString("DASHBOARD_STATE"));
		 dashboard.setColumnCount(rs.getInt("COLUMN_COUNT"));
		 dashboard.setUpdatedDate(rs.getDate("updateddate"));
		return dashboard;
	}

}
