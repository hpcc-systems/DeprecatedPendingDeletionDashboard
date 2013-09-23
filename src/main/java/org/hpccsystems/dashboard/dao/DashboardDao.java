package org.hpccsystems.dashboard.dao;

import java.util.List;

import org.hpccsystems.dashboard.entity.DashboardMenu;
import org.springframework.dao.DataAccessException;

public interface DashboardDao {
			
	public List<String> fetchApplicationIds() throws DataAccessException;
	
	public List<DashboardMenu> fetchDashboardMenuPages() throws DataAccessException;

}
