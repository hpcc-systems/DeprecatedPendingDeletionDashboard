package org.hpccsystems.dashboard.service.impl;

import java.util.List;

import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;


@Service("dashboardService")
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DashboardServiceImpl implements DashboardService {

    private DashboardDao dashboardDao;
    
    @Autowired
    public void setDashboardDao(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }
    
    @Override
    public void insertDashboard(Dashboard dashboard,String userId) {
        dashboardDao.insertDashboard(dashboard,userId);
    }

    @Override
    public List<Dashboard> getDashboards(String userId, String applicationId) {
        return dashboardDao.getDashboards(userId,applicationId);
    }
    
    public void deleteDashboard(Integer dashboardId) throws DataAccessException{
        dashboardDao.deleteDashboard(dashboardId);
    }

}
