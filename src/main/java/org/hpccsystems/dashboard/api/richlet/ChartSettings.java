package org.hpccsystems.dashboard.api.richlet;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
		String format;
		String config;
		
		try {
			Map<String, String[]> args = Executions.getCurrent().getParameterMap();
			source = args.get("source")[0];
			sourceId = args.get("source_id")[0];
			format = args.get("format")[0];
			config = args.get("config")[0];
		} catch (Exception e) {
			Clients.showNotification("Malformated URL string", false);
			//Executions.sendRedirect("/demo/index.zul");
			return;
		}
		ApiConfiguration apiConfig = new ApiConfiguration();
		apiConfig.setApiChartSetting(true);
		Sessions.getCurrent().setAttribute("apiConfiguration", apiConfig);	
		//TODO: have to set user details into session
		Sessions.getCurrent().setAttribute("user", new User());	
		if(LOG.isDebugEnabled()) {
			LOG.debug("Creating API edit portlet screen...");
		}
		StringBuilder url = new StringBuilder("/demo/layout/edit_chart.zul?");
		url.append("source").append("=").append(source).append("&")
				.append("source_id").append("=").append(sourceId).append("&")
				.append("format").append("=").append(format).append("&")
				.append("config").append("=").append(config);
		Executions.sendRedirect(url.toString());		
	}

}
