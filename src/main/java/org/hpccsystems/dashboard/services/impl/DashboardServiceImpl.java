package org.hpccsystems.dashboard.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.DashboardMenu;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.SidebarPage;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service("dashboardService")
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DashboardServiceImpl implements DashboardService {
	
	private final static Log log = LogFactory.getLog(DashboardServiceImpl.class);
	
	private DashboardDao dashboardDao;


	public List<String> retrieveApplicationIds() {
				
		return dashboardDao.fetchApplicationIds();
	}
	
	
	public List<SidebarPage> retrieveDashboardMenuPages() {
		
		if (log.isDebugEnabled()) {
			log.debug("Handling 'retrieveDashboardMenuPages' in DashboardServiceImpl");
		}
		
		HashMap<String,SidebarPage> pageMap = new LinkedHashMap<String,SidebarPage>();
		
		List<DashboardMenu> menuList = dashboardDao.fetchDashboardMenuPages();
		
		String fnName = "fn";

		for (int i = 0; i < menuList.size(); i++) 
		{
			DashboardMenu entry = (DashboardMenu)menuList.get(i);
			int j = i + 1;									
			String fnNameStr = fnName + j;						
			pageMap.put(fnNameStr,new SidebarPage(fnNameStr,entry.getDashMenuName(), entry.getDashMenuImageLocation(), entry.getDashMenuPageLocation()));
						
		}
		

		
		return new ArrayList<SidebarPage>(pageMap.values());
	}

	public DashboardDao getDashboardDao() {
		return dashboardDao;
	}

	public void setDashboardDao(DashboardDao dashboardDao) {
		this.dashboardDao = dashboardDao;
	}
	
	
	

}

