package org.hpccsystems.dashboard.service;

import java.util.List;

import org.hpccsystems.dashboard.entity.Dashboard;

public interface DashboardService {

    void insertDashboard(Dashboard dashboard,String userId);

    List<Dashboard> getDashboards(String userId, String applicationId);

}
