package org.hpccsystems.dashboard.api.richlet;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.services.UserCredential;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;

public class ViewDashboard extends GenericRichlet {

	private static final  Log LOG = LogFactory.getLog(ViewDashboard.class);
		
	@Override
	public void service(Page page) throws Exception {
		try {
			
			String source =Executions.getCurrent().getParameter(Constants.SOURCE);
			String sourceId = Executions.getCurrent().getParameter(Constants.SOURCE_ID);
			String[] dashboardIdArray = ((String[])Executions.getCurrent().getParameterValues(Constants.DB_DASHBOARD_ID));
			
			//TODO:Have to reset user and User credential values passed from circuit
			UserCredential credential = new UserCredential("2", "admin", source);
			Sessions.getCurrent().setAttribute("userCredential", credential);
			credential.addRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD);
			
			List<String> dashIdList =Arrays.asList(dashboardIdArray);
			StringBuilder url = new StringBuilder("/demo/?");
			
			if(LOG.isDebugEnabled()){
				LOG.debug("URL from External/Circuit source : "+url);				
			}
			
			url.append(Constants.SOURCE).append("=").append(source)
					.append("&").append(Constants.SOURCE_ID).append("=")
					.append(sourceId);
			for(String dashId : dashIdList){
				url.append("&").append(Constants.DB_DASHBOARD_ID).append("=")
					.append(dashId);
			}
			Executions.sendRedirect(url.toString());
		
		} catch (Exception ex) {
			
			//TODO: Process exception
		}

	}
}
