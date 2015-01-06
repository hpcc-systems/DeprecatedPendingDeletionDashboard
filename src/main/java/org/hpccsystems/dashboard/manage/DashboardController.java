package org.hpccsystems.dashboard.manage;

import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.authentication.LoginController;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.json.JSONObject;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Window;

import com.google.gson.JsonObject;

public class DashboardController extends SelectorComposer<Component> {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DashboardController.class);

	@WireVariable
	private AuthenticationService authenticationService;
	@WireVariable
	private DashboardService dashboardService;
	@Wire
	private Div chartDiv;

	private Dashboard dashboard;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		dashboard = (Dashboard) Executions.getCurrent().getAttribute(
				Constants.ACTIVE_DASHBOARD);

		if (dashboard.getCompositionName() != null) {
			drawChart();
		}

	}

	/**
	 * Renders chart in dashboard container
	 */
	private void drawChart() {
		try {

			String viaualizationURL = dashboard.generateVisualizationURL();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("viaualizationURL -->" + viaualizationURL);
			}
			JsonObject chartObj = new JsonObject();
			chartObj.addProperty(Constants.URL, viaualizationURL);
			chartObj.addProperty(Constants.TARGET, chartDiv.getUuid());
			chartObj.addProperty(Constants.HPCC_ID, dashboard.getHpccId());
			final String wsWork = "WsWorkunits";
			final String wsEcl = "WsEcl";
			chartObj.addProperty(wsWork, dashboard.getHpccConnection()
					.getESPUrl() + wsWork);
			chartObj.addProperty(wsEcl, dashboard.getHpccConnection()
					.getRoxieServiceUrl() + wsEcl);

			String data = StringEscapeUtils.escapeJavaScript(chartObj
					.toString());
			Clients.evalJavaScript("visualizeDDLChart('" + data + "')");
		} catch (Exception e) {
			LOGGER.error(Constants.EXCEPTION, e);
			Clients.showNotification("Unable to recreate chart",
					Clients.NOTIFICATION_TYPE_ERROR, chartDiv, "middle_center",
					5000, true);
		}
	}

	@Listen("onClick = #addWidget")
	public void onAddWidget() {
		Window window = (Window) Executions.createComponents(
				"widget/config.zul", null, new HashMap<String, Object>() {
					private static final long serialVersionUID = 1L;
					{
						put(Constants.WIDGET_CONFIG, new WidgetConfiguration(
								dashboard, chartDiv));
					}
				});

		window.doModal();
	}

	@Listen("onClick = #deleteDashboard")
	public void deleteDashboard() {
		EventListener<ClickEvent> clickListener = event -> {
			if (Messagebox.Button.YES.equals(event.getButton())) {
				Component component = DashboardController.this.getSelf()
						.getParent().getParent().getFellow("navHolder");
				Events.postEvent(Constants.ON_DELTE_DASHBOARD, component,
						dashboard);
			}

		};

		Messagebox.show(Labels.getLabel("deletedashboard"),
				Labels.getLabel("deletedashboardtitle"),
				new Messagebox.Button[] { Messagebox.Button.YES,
						Messagebox.Button.NO }, Messagebox.QUESTION,
				clickListener);
	}
}
