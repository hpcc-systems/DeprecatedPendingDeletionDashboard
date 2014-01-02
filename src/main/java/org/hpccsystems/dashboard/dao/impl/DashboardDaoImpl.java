package org.hpccsystems.dashboard.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.object.MappingSqlQuery;


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
	public void setDataSourceToTemplate(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private DataSource dataSource;

	private SelectDashboardMenus selectDBMenus;
	
	private SelectDashboardMenus getSelectDashboardMenus(final Application application,final String userId) {
		final StringBuffer sql = new StringBuffer(Queries.RETRIEVE_DASHBOARD);
		sql.append(application.getAppId())
		.append("' and user_id='")
		.append(userId).append("' order by sequence");
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("getSelectDashboardMenus in DashboardDaoImpl");
			LOG.debug("dashboardmenuquery -->" + sql.toString());
		}
		selectDBMenus = new SelectDashboardMenus(dataSource,
				sql.toString());
		return selectDBMenus;
	}

	public class SelectDashboardMenus extends MappingSqlQuery {

		public SelectDashboardMenus(final DataSource dataSource, final String sql) {
			super(dataSource, sql);
			compile();
		}

		protected Object mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
			final Dashboard entry = new Dashboard();
			entry.setApplicationId(resultSet.getString(1));
			entry.setDashboardId(Integer.parseInt(resultSet.getString(2)));
			entry.setName(resultSet.getString(3));
			entry.setDashboardState(resultSet.getString(4));
			entry.setColumnCount(resultSet.getInt(5));
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("DashboardDaoImpl SelectDashboardMenus in mapRow");
				LOG.debug("dashboardname -->" + entry.getName());
				LOG.debug("Column count -->" + entry.getColumnCount());
			}
			return entry;
		}
	}
	 /**
	  * Fetching DashboardMenuPages details from dashboard_details table.
	 * @param application
	 * @return List<DashboardMenu>
	 * @throws DataAccessException
	 */
	@SuppressWarnings("unchecked")
	public List<Dashboard> fetchDashboardDetails(final Application application,final String userId)
			throws DataAccessException {

		try {
			return (List<Dashboard>) getSelectDashboardMenus(application,userId)
					.execute();
		} catch (final DataAccessException e) {
				LOG.error("DataAccessException occurred during retrieveApplicationIds() in DashboardDaoImpl",e);
			throw e;
		}
	}

	public void initialize() {
		Validate.notNull(dataSource, "'dataSource' must be set!");
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Inserts Dashboard details to dashboard_details table & returns the ID of Dashboard that is inserted.
	 * @param applnId
	 * @param dashBoardName
	 * @param layout
	 * @throws SQLException
	 */
	public int addDashboardDetails(final String applnId, final String dashBoardName,
			final String userId	) {

		getJdbcTemplate().update(Queries.INSERT_DASHBOARD, new Object[] { 
				dashBoardName,
				userId,
				applnId  
		});
		
		return getJdbcTemplate().queryForObject(Queries.GET_MAX_DASHBOARD_ID, new Object[] {userId} , Integer.class);
	}

	public void updateSequence(final Integer dashboardId, final int sequence,
			final String dashboardName) throws SQLException {
		
		getJdbcTemplate().update(Queries.UPDATE_DASHBOARD_SEQUENCE, new Object[] { 
				sequence,
				dashboardName,
				dashboardId
		});
	}

	public void deleteDashboard(final Integer dashboardId, final String userId) throws SQLException {
		
		getJdbcTemplate().update(Queries.DELETE_DASHBOARD_WIDGETS, new Object[] { 
				dashboardId
		});
		
		getJdbcTemplate().update(Queries.DELETE_DASHBOARD, new Object[] { 
				dashboardId,
				userId
		});
	}


	public void updateDashboardState(final Integer dashboardId,final String emptyState,
			final int sequence,final String dashboardName) throws SQLException {
		
		getJdbcTemplate().update(Queries.UPDATE_DASHBOARD_STATE, new Object[] { 
				emptyState,
				sequence,
				dashboardName,
				dashboardId
		});
	}

	public void updateDashboardDetails(Integer dashboardId, int sequence,
			String dashboardName, int columnCount) throws SQLException {
		
		getJdbcTemplate().update(Queries.UPDATE_DASHBOARD_DETAILS, new Object[] { 
				sequence,
				dashboardName,
				columnCount,
				dashboardId
		});
	}	
	
}
