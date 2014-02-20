package org.hpccsystems.dashboard.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.api.entity.ChartConfiguration;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.ChartPanel;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.entity.chart.utils.TableRenderer;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.springframework.dao.DataAccessException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Window;

import com.google.gson.GsonBuilder;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditWidgetController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(EditWidgetController.class);

	@WireVariable
	AuthenticationService authenticationService;
	@WireVariable
	DashboardService dashboardService;
	@WireVariable
	WidgetService widgetService;
	
	@WireVariable
	HPCCService hpccService;
	@WireVariable
	ChartRenderer chartRenderer;
	@WireVariable
	TableRenderer tableRenderer;

	@Wire
	Include holderInclude;
	@Wire
	Window editPortletWindow;
	@Wire
	Button doneButton;

	Portlet portlet;
	XYChartData chartData;
	ChartPanel chartPanel;
	
	Dashboard dashboard;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Inside Edit portlet constructor..");
		}

		super.doAfterCompose(comp);
		
		Execution execution = Executions.getCurrent();
		chartData = new XYChartData();
		
		if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)) {
			//Configuring chart through API
			dashboard = new Dashboard();
			dashboard.setSourceId(execution.getParameter(Constants.SOURCE_ID));
			dashboard.setApplicationId(execution.getParameter(Constants.SOURCE));
			dashboard.setColumnCount(1);
			dashboard.setSequence(0);
			ChartConfiguration configuration = new GsonBuilder().create().fromJson(
					execution.getParameter(Constants.CIRCUIT_CONFIG),ChartConfiguration.class);
			portlet = new Portlet();
			portlet.setChartType(configuration.getChartType());
			portlet.setColumn(0);
			portlet.setName(configuration.getChartTitle());
			
			dashboard.setName(configuration.getDashboardTitle());
			
			chartData.setFileName(configuration.getDatasetName());
			chartData.setHpccConnection(configuration.getHpccConnection());
			
			holderInclude.setDynamicProperty(Constants.CIRCUIT_CONFIG, configuration);
		} else if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_CHART)){
			//Viewing chart through API
			List<String> dashboardIdList = null;
			if(execution.getParameter("dashboardId") != null) {
				dashboardIdList = new ArrayList<String>();
				dashboardIdList.add(execution.getParameter("dashboardId"));
			}
			dashboard = dashboardService.retrieveDashboardMenuPages(
							Constants.CIRCUIT_APPLICATION_ID, 
							authenticationService.getUserCredential().getUserId(), 
							dashboardIdList,
							execution.getParameter(Constants.SOURCE_ID))
								.get(0); // Assuming one Dashboard exists for a provided source_id 
			portlet = widgetService.retriveWidgetDetails(dashboard.getDashboardId())
						.get(0); //Assuming one Widget exists for the provided dashboard
			//Overriding chart type
			if(execution.getParameter(Constants.CHART_TYPE) != null) {
				portlet.setChartType(Integer.parseInt(execution.getParameter(Constants.CHART_TYPE)));
			}
			
			if (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
				chartData = chartRenderer.parseXML(portlet.getChartDataXML());
			}
		} else {
			//General flow
			portlet = (Portlet) Executions.getCurrent().getArg().get(Constants.PORTLET);
			chartPanel = (ChartPanel) Executions.getCurrent().getArg().get(Constants.PARENT);
			holderInclude.setDynamicProperty(Constants.PARENT, editPortletWindow);

			if (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
				chartData = chartRenderer.parseXML(portlet.getChartDataXML());
			}
		}
		
		
		holderInclude.setDynamicProperty(Constants.CHART_DATA, chartData);
		holderInclude.setDynamicProperty(Constants.PORTLET, portlet);
		holderInclude.setDynamicProperty(Constants.EDIT_WINDOW_DONE_BUTTON, doneButton);
		
		// Listener to invoke when holderInclude is detached
		editPortletWindow.addEventListener("onIncludeDetach",
				new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (event.getData() != null
								&& event.getData()
										.equals(Constants.EDIT_WINDOW_TYPE_DATA_SELECTION)) {
							if (portlet.getChartType().equals(
									Constants.TABLE_WIDGET)) {
								holderInclude.setSrc("layout/edit_table.zul");
							} else {
								holderInclude.setSrc("layout/edit_chart.zul");
							}
						}
					}
				});

		
		if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState()) ||
				authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)){
			// Not for configuring chart through API
			if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
				chartData = chartRenderer.parseXML(portlet.getChartDataXML());
				holderInclude.setDynamicProperty(Constants.CHART_DATA, chartData);
			}
			
			if(Constants.TABLE_WIDGET.equals(portlet.getChartType())){
				holderInclude.setSrc("layout/edit_table.zul");
			} else {
				holderInclude.setSrc("layout/edit_chart.zul");
			}
		} else {
			holderInclude.setDynamicProperty(Constants.CHART_DATA, chartData);
			holderInclude.setSrc("layout/edit_select_data.zul");
		}
	}

	/**
	 * Draws the chart from edit window to actual layout window and Adds the
	 * chart to session
	 * 
	 * @param event
	 */
	@Listen("onClick=#doneButton")
	public void closeEditWindow(final MouseEvent event) {
		
		portlet.setWidgetState(Constants.STATE_LIVE_CHART);
		
		if(authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_CONFIG_CHART)){
			//Configuring chart through API
			dashboard.setLastupdatedDate(new Timestamp(new Date().getTime()));
			
			try {
				List<Dashboard> dashboardList = dashboardService.retrieveDashboardMenuPages(dashboard.getApplicationId(), null, null,
								dashboard.getSourceId());
				if (dashboardList.isEmpty()) {
					dashboard.setDashboardId(dashboardService.addDashboardDetails(dashboard, Constants.CIRCUIT_APPLICATION_ID, dashboard
											.getSourceId(),	authenticationService.getUserCredential().getUserId()));
					portlet.setChartDataXML(chartRenderer.convertToXML(chartData));
					widgetService.addWidget(dashboard.getDashboardId(),	portlet, 0);
				} else {
					Integer widgetId = 0;
					List<Portlet> portletList = widgetService.retriveWidgetDetails(dashboardList.get(0).getDashboardId());
					for(Portlet portlet : portletList){
						widgetId = portlet.getId();						
					}
					dashboard.setDashboardId(dashboardList.get(0).getDashboardId());
					dashboardService.updateDashboard(dashboard);
					portlet.setChartDataXML(chartRenderer.convertToXML(chartData));
					portlet.setId(widgetId);
					widgetService.updateWidget(portlet);
				}
			} catch (DataAccessException e) {
				Clients.showNotification("Error occured while saving your changes");
			}
			
			Messagebox.show("Chart details are Updated Successfuly. You can close this window","",1,Messagebox.ON_OK);
			Clients.evalJavaScript("window.open('','_self',''); window.close();");
			editPortletWindow.detach();
			try {
				authenticationService.logout(null);
			} catch (Exception e) {
				Clients.showNotification("Error occured while logging out");
				LOG.error("Logout error", e);
			}
		} else if (authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_CHART)) {
			//Viewing chart through API
			portlet.setChartDataXML(chartRenderer.convertToXML(chartData));
			widgetService.updateWidget(portlet);
			
			Messagebox.show("Chart details are Updated Successfuly. You can close this window","",1,Messagebox.ON_OK);
			Clients.evalJavaScript("window.open('','_self',''); window.close();");
			editPortletWindow.detach();
			try {
				authenticationService.logout(null);
			} catch (Exception e) {
				Clients.showNotification("Error occured while logging out");
				LOG.error("Logout error", e);
			}
		} else {
			//General flow
			try {
				Div div = chartPanel.removeStaticImage();
				
				//For Table Widget
				if(portlet.getChartType().equals(Constants.TABLE_WIDGET)) {
					div.getChildren().clear();
					div.appendChild(
							tableRenderer.constructTableWidget(
									portlet.getTableDataMap(), false,portlet.getName())
								);
				} else {
					//For Chart Widgets
					final String divToDraw = div.getId(); 
						//isEdit Window is set to false as we are constructing the JSON to be drawn in the Widget itself
						chartRenderer.constructChartJSON(chartData, portlet, false); 
						chartRenderer.drawChart(chartData, divToDraw, portlet);		 
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("Drawn chart in portlet..");
						LOG.debug("Portlet - Div ID --> " + divToDraw);
					}
				}
				
				//update Live chart data into DB
				portlet.setChartDataXML(chartRenderer.convertToXML(chartData));
				widgetService.updateWidget(portlet);
				
				}catch(DataAccessException e){
					LOG.error("Exception in closeEditWindow() while updating Live chart data into DB", e);
				}catch(Exception ex) {
					Clients.showNotification("Unable to fetch column data from HPCC to draw chart", "error", this.getSelf(), "middle_center", 3000, true);
					LOG.error("Exception in closeEditWindow()", ex);
					return;
			}
			
			editPortletWindow.detach();
		}
	}
}
