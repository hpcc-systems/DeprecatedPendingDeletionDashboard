package org.hpccsystems.dashboard.dao;

import java.util.List;

import org.hpccsystems.dashboard.entity.Dashboard;

public interface DashboardDao {

    void insertDashboard(Dashboard dashboard,String userId);

    List<Dashboard> getDashboards(String userId, String applicationId);

}
