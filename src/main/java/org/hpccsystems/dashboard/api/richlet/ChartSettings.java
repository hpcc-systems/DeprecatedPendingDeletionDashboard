package org.hpccsystems.dashboard.api.richlet;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.ApiConfiguration;
import org.hpccsystems.dashboard.entity.User;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;


public class ChartSettings extends GenericRichlet{

	private static final Log LOG = LogFactory.getLog(ChartSettings.class);
	
	@Override
	public void service(Page page) throws Exception {		
		String source;
		String sourceId;
		String format = null;
		String config = null;
		String chartType = null;
		String dashboardId = null;
		try {
			ApiConfiguration apiConfig = new ApiConfiguration();
			try{			
			Map<String, String[]> args = Executions.getCurrent().getParameterMap();
			source = args.get(Constants.SOURCE)[0];
			sourceId = args.get(Constants.SOURCE_ID)[0];
			
				if (args.containsKey(Constants.CIRCUIT_CONFIG)) {
					apiConfig.setApiConfig(true);
					format = args.get(Constants.CHARTLIST_FORMAT)[0];
					config = args.get(Constants.CIRCUIT_CONFIG)[0];
				} else if (args.containsKey(Constants.CHART_TYPE)) {
					apiConfig.setApiChartConfig(true);
					chartType = args.get(Constants.CHART_TYPE)[0];
					dashboardId = args.get(Constants.DB_DASHBOARD_ID)[0];
				}
			}catch(Exception ex){
				Clients.showNotification("Malformated URL string", false);
				LOG.error("Exception while parsing Request Parameter in ChartSettings.service()", ex);
				return;			
			}
			
			
			Sessions.getCurrent().setAttribute("apiConfiguration", apiConfig);	
			//TODO: have to set user details into session
			User user =  new User();
			user.setFullName("admin");
			user.setUserId("2");
			user.setValidUser(true);
			user.setActiveFlag("Y"); 
			Sessions.getCurrent().setAttribute("user", user);	
			if(LOG.isDebugEnabled()) {
				LOG.debug("Creating API edit portlet screen...");
			}
			StringBuilder url = new StringBuilder("/demo/layout/edit_chart.zul?");			
			url.append("source").append("=").append(source).append("&")
					.append("source_id").append("=").append(sourceId).append("&");
			
			if(config != null){
				//constructing url for edit window without chart
					url.append("format").append("=").append(format).append("&")
					.append("config").append("=").append(config);			
			}else{
				//constructing url for edit window with chart
				url.append("chartType").append("=").append(chartType).append("&")
				.append("dashboardId").append("=").append(dashboardId);	
			}
			Executions.sendRedirect(url.toString());		
		} catch (Exception e) {
			Clients.showNotification("Unable to configure chart.Please input valid data", false);
			LOG.error("Exception while parsing Request Parameter in ChartSettings.service()", e);
			return;
		}
		
	}

}
