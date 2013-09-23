package org.hpccsystems.dashboard.services;

import java.util.List;

public interface DashboardService {
	
	public List<String> retrieveApplicationIds();
	
	public List<SidebarPage> retrieveDashboardMenuPages();
}
