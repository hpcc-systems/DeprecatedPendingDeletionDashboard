package org.hpccsystems.dashboard.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.DashboardMenu;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.object.MappingSqlQuery;

public class DashboardDaoImpl implements DashboardDao {

    private final static Log log = LogFactory.getLog(DashboardDaoImpl.class);
	
	private DataSource dataSource;
	
	private SelectApplicationIds selectApplicationIds;
	
	private SelectDashboardMenus selectDashboardMenus;
	
	private SelectApplicationIds getSelectApplicationIds() {
		if (selectApplicationIds == null) {
			selectApplicationIds = new SelectApplicationIds(dataSource, SelectApplicationIds.SQL);
		}
		return selectApplicationIds;
	}
	
	private SelectDashboardMenus getSelectDashboardMenus() {
		if (selectDashboardMenus == null) {
			selectDashboardMenus = new SelectDashboardMenus(dataSource, SelectDashboardMenus.SQL);
		}
		return selectDashboardMenus;
	}

	@SuppressWarnings("rawtypes")
	public class SelectApplicationIds extends MappingSqlQuery {

		public final static String SQL = "SELECT dash_app_name FROM dash_application";

		public SelectApplicationIds(DataSource ds, String sql) {
			super(ds, sql);			
			compile();			
		}

		protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {

			return rs.getString(1).trim();
		}
	}
	
	@SuppressWarnings("rawtypes")
	public class SelectDashboardMenus extends MappingSqlQuery {

		public final static String SQL = "SELECT dash_application_id, dash_menu_id, dash_menu_name, dash_menu_image_location, dash_menu_page_location FROM dash_menu where dash_application_id = 'A001'";

		public SelectDashboardMenus(DataSource ds, String sql) {
			super(ds, sql);			
			compile();			
		}

		protected Object mapRow(ResultSet rs, int rowNum) throws SQLException {

			DashboardMenu entry = new DashboardMenu();
			entry.setDashApplicationId(rs.getString(1));
			entry.setDashMenuId(rs.getString(2));
			entry.setDashMenuName(rs.getString(3));
			entry.setDashMenuImageLocation(rs.getString(4));
			entry.setDashMenuPageLocation(rs.getString(5));
			
			return entry;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> fetchApplicationIds() throws DataAccessException {
						
		try {
			
			return (List<String>) getSelectApplicationIds().execute();
			

		} catch (final DataAccessException e) {
					
			if (log.isErrorEnabled()) {
				log.error("DataAccessException occurred during retrieveApplicationIds() in DashboardDaoImpl");
				log.error(e.getMessage());
				
			}
			throw e;
		}				
	}
	
	@SuppressWarnings("unchecked")
	public List<DashboardMenu> fetchDashboardMenuPages() throws DataAccessException {
						
		try {
			
			return (List<DashboardMenu>) getSelectDashboardMenus().execute();
			

		} catch (final DataAccessException e) {
						
			if (log.isErrorEnabled()) {
				log.error("DataAccessException occurred during retrieveApplicationIds() in DashboardDaoImpl");
				log.error(e.getMessage());
				
			}
			throw e;
		}				
	}
	
	public void initialize() {

		Validate.notNull(dataSource, "'dataSource' must be set!");

	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	
	
}
