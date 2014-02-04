package org.hpccsystems.dashboard.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.lang.Validate;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.WidgetDao;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.rowmapper.WidgetRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Dao class to do widget related DB hits
 * @author 
 *
 */ 
public class WidgetDaoImpl implements WidgetDao{
	
	private JdbcTemplate jdbcTemplate;
	private DataSource dataSource;
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	@Autowired
	public void setDataSourceToJdbcTemplate(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
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
	
	public void updateWidgetDetails(final Integer dashboardId,final List<Portlet> portlets)  throws DataAccessException {
		
		String sql = Queries.UPDATE_WIDGET_DETAILS;
		getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter()
		{

			public void setValues(PreparedStatement statement, int i)
					throws SQLException {
				Portlet portlet = portlets.get(i);
				statement.setString(1, portlet.getName());
				statement.setString(2, portlet.getWidgetState());
				if (Constants.STATE_EMPTY.equals(portlet.getWidgetState())) {
					statement.setInt(3, 0);
				} else {
					statement.setInt(3, portlet.getChartType());
				}
				statement.setInt(4, portlet.getColumn());
				statement.setInt(5, portlet.getWidgetSequence());
				statement.setString(6, portlet.getChartDataXML());
				statement.setInt(7,portlet.getId());
				statement.setInt(8, dashboardId);
				
			}
			public int getBatchSize() {
				return portlets.size();
				}
			
		});
		
	}
	public void addWidgetDetails(final Integer dashboardId,
			final List<Portlet> portlets) throws DataAccessException {

		String sql = Queries.INSERT_WIDGET_DETAILS;
		
		getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {			
			public void setValues(PreparedStatement statement, int i) throws SQLException {
				Portlet portlet = portlets.get(i);
				statement.setInt(1, dashboardId);
				statement.setString(2,portlet.getName() );
				statement.setString(3, portlet.getWidgetState());
				if (Constants.STATE_EMPTY.equals(portlet.getWidgetState())) {
					statement.setInt(4, 0);
				} else {
					statement.setInt(4, portlet.getChartType());
				}
				statement.setInt(5, portlet.getColumn());
				statement.setInt(6,portlet.getWidgetSequence());
				statement.setString(7, portlet.getChartDataXML());				
			}
			
			public int getBatchSize() {
				return portlets.size();
			}
		});
	
	
		
	}
	public void deleteWidgets(final Integer dashboardId, final List<Portlet> portlets)
			throws DataAccessException {
		String sql =Queries.DELETE_WIDGETS;
		getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
			
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Portlet portlet = portlets.get(i);
				ps.setInt(1, portlet.getId());
			}
			
			public int getBatchSize() {
				return portlets.size();
			}
		});
	}
	
	public List<Portlet> retriveWidgetDetails(Integer dashboardId) throws DataAccessException{
			StringBuilder sqlBuffer = new StringBuilder();
			sqlBuffer.append(Queries.GET_WIDGET_DETAILS).append(dashboardId).append(" order by WIDGET_SEQUENCE");
			List<Portlet> portlets = getJdbcTemplate().query(sqlBuffer.toString(),new WidgetRowMapper());
			return portlets;
		}
}
