package org.hpccsystems.dashboard.api.richlet;

import java.util.HashMap;
import java.util.Map;

import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Window;

public class ViewDashboard extends GenericRichlet {

	DashboardService dashboardService;
	@Autowired
	public void setDashboardService(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}
	@Override
	public void service(Page page) throws Exception {
		try {
			Map<String, String[]> args = Executions.getCurrent().getParameterMap();
			String dashboardId =args.get(Constants.DB_DASHBOARD_ID)[0];
			String sourceTypeString = args.get(Constants.SOURCE)[0];
			Integer sourceTypeInt = 0;
			if(sourceTypeString != null)
			{
				sourceTypeInt =Constants.SOURCE_TYPE_ID.get(sourceTypeString.trim());
			}
			//Dashboard dashboard = dashboardService.getDashboard(Integer.valueOf(dashboardId), sourceTypeInt);
			final Session session = Sessions.getCurrent();
			User user = new User();
			user.setFullName("admin");
			user.setUserId("2");
			user.setValidUser(true);
			user.setActiveFlag("Y");
			session.setAttribute("user", user);
			Window window = new Window("View Dashboard", "normal", true);		
			
			Map<String,Object> parameters = new HashMap<String, Object>();
			Executions.createComponents("/demo/index.zul", window, parameters);
			window.setPage(page);
		} catch (Exception ex) {

		}

	}
}
