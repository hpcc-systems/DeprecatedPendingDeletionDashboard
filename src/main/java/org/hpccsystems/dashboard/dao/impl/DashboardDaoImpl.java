package org.hpccsystems.dashboard.dao.impl; 

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.rowmapper.DashboardRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
	@SuppressWarnings("unchecked")
	public List<Dashboard> fetchDashboardDetails(final Application application,final String userId,List<String> dashboardIdList)
			throws DataAccessException {
		List<Dashboard> dashboardList = null;
		StringBuilder sqlBuffer = new StringBuilder();
		sqlBuffer.append(Queries.RETRIEVE_DASHBOARD_DETAILS).append("'").append(
				application.getAppId());
		if (userId != null && dashboardIdList == null) {
			sqlBuffer.append("' and user_id='").append(userId)
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
	

	/**
	 * Inserts Dashboard details to dashboard_details table & returns the ID of Dashboard that is inserted.
	 * @param sourceId
	 * @param dashBoardName
	 * @param layout
	 * @throws SQLException
	 */
	public int addDashboardDetails(final String sourceId,final String source, final String dashBoardName,
			final String userId, final Date dashBoardDate)throws DataAccessException {

		getJdbcTemplate().update(Queries.INSERT_DASHBOARD, new Object[] { 
				dashBoardName,
				userId,
				Constants.SOURCE_TYPE_ID.get(source),
				sourceId ,
				dashBoardDate
		});
		
		return getJdbcTemplate().queryForObject(Queries.GET_MAX_DASHBOARD_ID, new Object[] {userId} , Integer.class);
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
			rowsdeleted = getJdbcTemplate().update(Queries.CIRCUIT_DELETE_DASHBOARD, new Object[] { 
					dashboardId
					
		});
		}
		return rowsdeleted;
	}


	public void updateDashboardState(final Integer dashboardId,final String emptyState,
			final int sequence,final String dashboardName, Date updatedDate) throws DataAccessException {
		getJdbcTemplate().update(Queries.UPDATE_DASHBOARD_STATE, new Object[] { 
				emptyState,
				sequence,
				dashboardName,
				updatedDate,
				dashboardId
				
		});
	}

	public void updateDashboardDetails(Integer dashboardId, int sequence,
			String dashboardName, int columnCount, Date updatedDate) throws DataAccessException {
		getJdbcTemplate().update(Queries.UPDATE_DASHBOARD_DETAILS, new Object[] { 
				sequence,
				dashboardName,
				columnCount,				
				updatedDate,
				dashboardId
		});
	}
	
}
