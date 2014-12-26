package org.hpccsystems.dashboard.service;

import org.hpccsystems.dashboard.entity.Dashboard;

public interface DashboardService {

    void insertDashboard(Dashboard dashboard,String userId);

}
