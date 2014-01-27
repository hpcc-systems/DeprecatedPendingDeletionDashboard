package org.hpccsystems.dashboard.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.hpccsystems.dashboard.entity.User;
import org.springframework.jdbc.core.RowMapper;

/**
 * Class to get mapped the User details from DB to User Object
 * @author 
 *
 */
public class UserRowMapper implements RowMapper<User> {
	
	private static final long serialVersionUID = 1L;
	public User mapRow(ResultSet rs, int rowNum) throws SQLException {
		User user = new User();
		user.setUserId(String.valueOf(rs.getInt("user_id")));
		user.setFullName(rs.getString("user_name"));
		user.setPassword(rs.getString("password"));
		user.setActiveFlag(rs.getString("active_flag"));
		return user;
	}



}
