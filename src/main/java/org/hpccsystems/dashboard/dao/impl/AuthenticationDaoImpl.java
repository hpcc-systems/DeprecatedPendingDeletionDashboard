package org.hpccsystems.dashboard.dao.impl;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.AuthenticationDao;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.rowmapper.UserRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

/**
 * Dao class to do user authentication related DB hits
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class AuthenticationDaoImpl implements AuthenticationDao{
	
	public static final long serialVersionUID = 1L;
	
	private static final  Log LOG = LogFactory.getLog(AuthenticationDaoImpl.class); 
	private JdbcTemplate jdbcTemplate;
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	//TODO:  Return UserCredential Instead of User
	public User authendicateUser(String userName, String password) throws SQLException {
		User user =null;			
		String sql=Queries.GET_USER_DETAILs;
		
		try
		{
		user = getJdbcTemplate().queryForObject(sql, new Object[]{userName}, new UserRowMapper());
		if(user != null)
		{
			String userId = user.getUserId();
			String pwd = user.getPassword();
			String flag = user.getActiveFlag();	
			if(password.trim().equals(pwd))
			{
				user.setValidUser(true);
				if(Constants.INACTIVE_FLAG.equals(flag))
				{
				user.setActiveFlag(flag);
				String sqlQuery=Queries.RESET_USER_FLAG;
				getJdbcTemplate().update(sqlQuery, new Object[]{Constants.ACTIVE_FLAG,userId});
				}
				else
				{
					user.setActiveFlag(Constants.ACTIVE_FLAG);
				}
				
			}
		}
		}
		catch(EmptyResultDataAccessException ex) {
			LOG.error("authendicateUser failed to execute the query due to invalid user! Return an empty string", ex);
			return null;
		}
		
		return user;
	}
		
	
	public void updateActiveFlag(User user)  throws SQLException{
		String sqlQuery=Queries.RESET_USER_FLAG;
		getJdbcTemplate().update(sqlQuery, new Object[]{Constants.INACTIVE_FLAG,user.getUserId()});
	}

}
