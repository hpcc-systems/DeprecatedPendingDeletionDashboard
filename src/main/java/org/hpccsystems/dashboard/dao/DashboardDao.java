package org.hpccsystems.dashboard.dao;

import org.hpccsystems.dashboard.entity.Dashboard;

public interface DashboardDao {

    void insertDashboard(Dashboard dashboard,String userId);

}
