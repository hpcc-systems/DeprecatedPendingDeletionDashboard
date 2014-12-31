package org.hpccsystems.dashboard.service;

import java.util.List;

import org.hpccsystems.dashboard.entity.Dashboard;
import org.springframework.dao.DataAccessException;

public interface DashboardService {

    void insertDashboard(Dashboard dashboard,String userId);

    List<Dashboard> getDashboards(String userId, String applicationId);
    
    void deleteDashboard(Integer dashboardId) throws DataAccessException; 
    
    /**
     * Service to update dashboard details into DB
     * @param dashboard
     * @param userId 
     * @throws Exception
     */
    void updateDashboard(Dashboard dashboard, String userId);

	
}
