package org.hpccsystems.dashboard.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.hpccsystems.dashboard.entity.Application;
import org.springframework.jdbc.core.RowMapper;


/**
 * Class to get mapped the Application details from DB to application Object
 * @author 
 *
 */
public class ApplicationRowMapper implements RowMapper<Application> {

	public Application mapRow(ResultSet rs, int rowNum) throws SQLException {
		Application application = new Application();
		application.setAppId(rs.getString("dash_app_id"));
		application.setAppName(rs.getString("dash_app_name"));
		return application;
	}

}
