package org.hpccsystems.dashboard.api.richlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.ApiConfiguration;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

public class ViewDashboard extends GenericRichlet {

	private static final  Log LOG = LogFactory.getLog(ViewDashboard.class);
	DashboardService dashboardService;
	@Autowired
	public void setDashboardService(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}
	@Override
	public void service(Page page) throws Exception {
		try {
			final Session session = Sessions.getCurrent();
			//TODO:Have to reset user and User credential values passed from circuit
			User user = new User();
			user.setFullName("admin");
			user.setUserId("2");
			user.setValidUser(true);
			user.setActiveFlag("Y");
			session.setAttribute("user", user);
			UserCredential userCredential = new UserCredential(user.getFullName(), user.getFullName());
			session.setAttribute("userCredential", userCredential);
			ApiConfiguration config = new ApiConfiguration();
			config.setApiEnabled(true);
			session.setAttribute("apiConfiguration", config);
			String dashboardId  =Executions.getCurrent().getParameter(Constants.DB_DASHBOARD_ID);;
			String sourceType =Executions.getCurrent().getParameter(Constants.SOURCE);
			String sourceId = Executions.getCurrent().getParameter(Constants.SOURCE_ID);
			
			StringBuilder url = new StringBuilder("/demo/index.zul?");
			if(LOG.isDebugEnabled()){
				LOG.debug("URL from External/Circuit source : "+url);				
			}
			url.append(Constants.SOURCE).append("=").append(sourceType)
					.append("&").append(Constants.SOURCE_ID).append("=")
					.append(sourceId).append("&")
					.append(Constants.DB_DASHBOARD_ID).append("=")
					.append(dashboardId);
			Executions.sendRedirect(url.toString());
		
		} catch (Exception ex) {

		}

	}
}
