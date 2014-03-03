package org.hpccsystems.dashboard.api.richlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.services.UserCredential;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;

public class ViewDashboard extends GenericRichlet {

	private static final  Log LOG = LogFactory.getLog(ViewDashboard.class);
		
	@Override
	public void service(Page page) throws Exception {
		try {
			
			String source =Executions.getCurrent().getParameter(Constants.SOURCE);
			String[] dashboardIdArray = ((String[])Executions.getCurrent().getParameterValues(Constants.DB_DASHBOARD_ID));
			
			//TODO:Have to reset user and User credential values passed from circuit
			UserCredential credential = new UserCredential("2", "admin",  Constants.CIRCUIT_APPLICATION_ID);
			Sessions.getCurrent().setAttribute("userCredential", credential);
			credential.addRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD);
			
			
			
			StringBuilder url = new StringBuilder("/demo/?");			
			url.append(Constants.SOURCE).append("=").append(source);
			for(String dashId : dashboardIdArray){
				url.append("&").append(Constants.DB_DASHBOARD_ID).append("=")
					.append(dashId);
			}
			if(LOG.isDebugEnabled()){
				LOG.debug("URL from External/Circuit source : "+url);				
			}
			Executions.sendRedirect(url.toString());
			
		} catch (Exception ex) {			
			Clients.showNotification("Malformated URL string", false);
			LOG.error("Exception while parsing Request Parameter in ViewDashboard.service()", ex);
			return;			
			}

	}
}
