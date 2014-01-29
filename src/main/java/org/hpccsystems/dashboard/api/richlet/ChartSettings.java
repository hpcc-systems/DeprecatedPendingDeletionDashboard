package org.hpccsystems.dashboard.api.richlet;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.api.entity.ChartConfiguration;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.ChartPanel;
import org.hpccsystems.dashboard.entity.Portlet;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.GenericRichlet;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Window;

import com.google.gson.GsonBuilder;

public class ChartSettings extends GenericRichlet{

	private static final Log LOG = LogFactory.getLog(ChartSettings.class);
	
	@Override
	public void service(Page page) throws Exception {
		Window window = new Window("Chart Settings", "normal", true);
		Map<String,Object> parameters = new HashMap<String, Object>();
		
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
			Executions.sendRedirect("/demo/index.zul");
			return;
		}
		
		ChartConfiguration configuration = new GsonBuilder().create().fromJson(config,ChartConfiguration.class);
				
		Portlet portlet = new Portlet();
		portlet.setWidgetState(Constants.STATE_GRAYED_CHART);
		portlet.setChartType(configuration.getChartType());
		portlet.setColumn(0);
		portlet.setName(configuration.getChartTitle());
		portlet.setPersisted(false);
		
		ChartPanel chartPanel = new ChartPanel(portlet);
		
		parameters.put(Constants.PARAMS, configuration);
		parameters.put(Constants.PORTLET, portlet);
		parameters.put(Constants.PARENT, chartPanel);
		
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Creating edit portlet screen...");
		}
		
		Executions.createComponents("/api/chart_settings.zul", window, parameters);
		
		window.setPage(page);
	}

}
