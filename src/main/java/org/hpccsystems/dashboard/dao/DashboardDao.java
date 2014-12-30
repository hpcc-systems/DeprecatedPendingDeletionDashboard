package org.hpccsystems.dashboard.dao;

import java.util.List;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.springframework.dao.DataAccessException;

public interface DashboardDao {

    void insertDashboard(Dashboard dashboard,String userId);

    List<Dashboard> getDashboards(String userId, String applicationId);
    
    void deleteDashboard(Integer dashboardId) throws DataAccessException;

}
