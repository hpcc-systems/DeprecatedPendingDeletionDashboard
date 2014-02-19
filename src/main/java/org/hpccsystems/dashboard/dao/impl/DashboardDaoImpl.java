package org.hpccsystems.dashboard.dao.impl; 

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.rowmapper.DashboardRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;


/**
 * DashboardDaoImpl is implementation class for DashboardDao.
 *
 */
public class DashboardDaoImpl implements DashboardDao {

	private static final  Log LOG = LogFactory.getLog(DashboardDaoImpl.class);
	
	private JdbcTemplate jdbcTemplate;
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	 /**
	  * Fetching DashboardMenuPages details from dashboard_details table.
	 * @param application
	 * @return List<DashboardMenu>
	 * @throws DataAccessException
	 */
	public List<Dashboard> fetchDashboardDetails(final String applicationId,final String userId, List<String> dashboardIdList, final String sourceId)
			throws DataAccessException {	
		
		List<Dashboard> dashboardList = null;
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append(Queries.RETRIEVE_DASHBOARD_DETAILS).append("'").append(
				applicationId).append("'");
		if(userId == null && dashboardIdList == null  && sourceId != null){
			sqlBuffer.append(" and source_id = '")
			.append(sourceId)
			.append("' order by last_updated_date desc");
		}
		else if (userId != null && dashboardIdList == null) {
			sqlBuffer.append(" and user_id='").append(userId)
					.append("' order by sequence");
		} else if (dashboardIdList != null && dashboardIdList.size() > 0) {
			sqlBuffer.append(Queries.DASHBOARD_IN_CLAUSE).append("( '");
			int count = 1;
			for (String dashboardId : dashboardIdList) {
				sqlBuffer.append(dashboardId).append("'");
				if (count != dashboardIdList.size()) {
					sqlBuffer.append(",'");
				}
				count++;
			}
			sqlBuffer.append(")").append((" order by sequence"));
		}
		LOG.info("retrieveDashboardDetails() Query -->" + sqlBuffer);
		dashboardList = getJdbcTemplate().query(sqlBuffer.toString(),
				new DashboardRowMapper());
		return dashboardList;
	}
	
	public int addDashboardDetails(final Dashboard dashboard,final String applicationId, final String sourceId,final String userId)
			throws DataAccessException {
		getJdbcTemplate().update(Queries.INSERT_DASHBOARD, new Object[] { 
				dashboard.getName(),
				userId,
				applicationId,
				dashboard.getLastupdatedDate(),
				dashboard.getColumnCount(),
				dashboard.getSequence(),
				dashboard.getSourceId()
		});
		return jdbcTemplate.queryForInt("select last_insert_id()"); 
	}

	public int deleteDashboard(final Integer dashboardId, final String userId) throws DataAccessException {
		int rowsdeleted = 0;
		rowsdeleted = getJdbcTemplate().update(Queries.DELETE_DASHBOARD_WIDGETS, new Object[] { 
				dashboardId
		});
		//TODO : need to disable this userId null check, when circuit passes the user details
		if(userId != null){
			rowsdeleted = getJdbcTemplate().update(Queries.DELETE_DASHBOARD, new Object[] { 
				dashboardId,
				userId
		});
		}else{
			rowsdeleted = getJdbcTemplate().update(Queries.API_DELETE_DASHBOARD, new Object[] { 
					dashboardId
					
		});
		}
		return rowsdeleted;
	}
	
	public void updateSidebarDetails(final List<Integer> dashboardIds)throws DataAccessException{
		String sql = Queries.UPDATE_SIDEBAR_DETAILS;
		getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter()
		{
			public void setValues(PreparedStatement statement, int i)
					throws SQLException {
				Integer dashboardId = dashboardIds.get(i);
				statement.setInt(1, i);
				statement.setInt(2, dashboardId);
			}
			public int getBatchSize() {
				return dashboardIds.size();
				}
		});
	}
	@Override
	public void updateDashboard(final Dashboard dashboard) throws DataAccessException {
		
		if(dashboard.getDashboardState()== null 
				|| !Constants.STATE_EMPTY.equals(dashboard.getDashboardState())){
			//updates dashboard Sequence & Column count,Name
			getJdbcTemplate().update(Queries.UPDATE_DASHBOARD, new Object[] { 
					dashboard.getName(),
					dashboard.getColumnCount(),
					dashboard.getLastupdatedDate(),				
					dashboard.getDashboardId()
			});
			
		}
		
		
	}
	
}
