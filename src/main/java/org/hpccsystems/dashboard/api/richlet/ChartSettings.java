package org.hpccsystems.dashboard.api.richlet;

import java.util.Map;
import org.apache.commons.logging.Log; 
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.services.UserCredential;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;


public class ChartSettings extends GenericRichlet{

	private static final Log LOG = LogFactory.getLog(ChartSettings.class);

	
	@Override
	public void service(Page page) throws Exception {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Inside Richlet.. ");
		}
	
		UserCredential credential = new UserCredential("2", "admin", Constants.CIRCUIT_APPLICATION_ID);
		Sessions.getCurrent().setAttribute("userCredential", credential);
		
		StringBuilder url = new StringBuilder("/demo/?");			
			try{			
			Map<String, String[]> args = Executions.getCurrent().getParameterMap();
			url.append("source").append("=").append(args.get(Constants.SOURCE)[0]);
				if (args.containsKey(Constants.SOURCE_ID) && args.containsKey(Constants.CIRCUIT_CONFIG)) {
					//Setting the role to user to Configure chart
					credential.addRole(Constants.CIRCUIT_ROLE_CONFIG_CHART);
					
					url.append("&").append(Constants.SOURCE_ID).append("=").append(args.get(Constants.SOURCE_ID)[0]);
					url.append("&").append("format").append("=").append(args.get(Constants.CHARTLIST_FORMAT)[0]);
					url.append("&").append(Constants.CIRCUIT_CONFIG).append("=").append(args.get(Constants.CIRCUIT_CONFIG)[0]);
					
				} else if (args.containsKey(Constants.CHART_TYPE) && args.containsKey(Constants.SOURCE_ID) && args.containsKey(Constants.CIRCUIT_DASHBOARD_ID)) {
					//Setting the role to user to view Chart
					credential.addRole(Constants.CIRCUIT_ROLE_VIEW_CHART);
					
					url.append("&").append(Constants.SOURCE_ID).append("=").append(args.get(Constants.SOURCE_ID)[0]);
					url.append("&").append(Constants.CHART_TYPE).append("=").append(args.get(Constants.CHART_TYPE)[0]);
					url.append("&").append(Constants.CIRCUIT_DASHBOARD_ID).append("=").append(args.get(Constants.DB_DASHBOARD_ID)[0]);
					
				} else if(args.containsKey(Constants.CIRCUIT_DASHBOARD_ID)){
					if(args.containsKey(Constants.CIRCUIT_CONFIG)){
						credential.addRole(Constants.CIRCUIT_ROLE_CONFIG_CHART);
						url.append("&").append(Constants.CIRCUIT_CONFIG).append("=").append(args.get(Constants.CIRCUIT_CONFIG)[0]);
					} else {
						credential.addRole(Constants.CIRCUIT_ROLE_VIEW_CHART);
					}
					
					url.append("&").append(Constants.CIRCUIT_DASHBOARD_ID).append("=").append(args.get(Constants.DB_DASHBOARD_ID)[0]);
					
				} else {
					// When none of above conditions are satisfied, URL is incorrect
					throw new Exception();
				}
			}catch(Exception ex){
				Clients.showNotification("Malformated URL string", false);
				LOG.error("Exception while parsing Request Parameter in ChartSettings.service()", ex);
				return;			
			}
			
			if(LOG.isDebugEnabled()) {
				LOG.debug("URL formed to view API chart config screen -->"+url.toString());
				LOG.debug("Creating API edit portlet screen...");
			}
			Executions.sendRedirect(url.toString());		
	}

}
