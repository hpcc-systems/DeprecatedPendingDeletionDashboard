package org.hpccsystems.dashboard.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.ChartPanel;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.entity.chart.utils.TableRenderer;
import org.hpccsystems.dashboard.services.HPCCService;
import org.zkoss.zk.ui.Component;
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
import org.zkoss.zul.Window;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditWidgetController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;
	private static final Log LOG = LogFactory.getLog(EditWidgetController.class);

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
	XYChartData chartData = new XYChartData();
	ChartPanel chartPanel;
	

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Inside Edit portlet constructor..");
		}

		super.doAfterCompose(comp);
		portlet = (Portlet) Executions.getCurrent().getArg()
				.get(Constants.PORTLET);
		chartPanel = (ChartPanel) Executions.getCurrent().getArg()
				.get(Constants.PARENT);
		
		if (Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())) {
			chartData = chartRenderer.parseXML(portlet.getChartDataXML());
		}

		// Listener to invoke when holderInclude is detached
		editPortletWindow.addEventListener("onIncludeDetach",
				new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (event.getData() != null
								&& event.getData()
										.equals(Constants.EDIT_WINDOW_TYPE_DATA_SELECTION)) {
							if (portlet.getChartType().equals(
									Constants.TABLE_WIDGET)) {
								holderInclude
										.setSrc("layout/edit_table.zul");
							} else {
								holderInclude.setSrc("layout/edit_chart.zul");
							}
						}
					}
				});

		holderInclude.setDynamicProperty(Constants.CHART_DATA, chartData);
		holderInclude.setDynamicProperty(Constants.PARENT, editPortletWindow);
		holderInclude.setDynamicProperty(Constants.PORTLET, portlet);
		holderInclude.setDynamicProperty(Constants.EDIT_WINDOW_DONE_BUTTON, doneButton);
		
		if(Constants.STATE_LIVE_CHART.equals(portlet.getWidgetState())){
			chartData = chartRenderer.parseXML(portlet.getChartDataXML());
			holderInclude.setDynamicProperty(Constants.CHART_DATA, chartData);
			
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
		Div div = chartPanel.removeStaticImage();
		//For Table Widget
		if(portlet.getChartType().equals(Constants.TABLE_WIDGET)) {
			portlet.setWidgetState(Constants.STATE_LIVE_CHART);
			portlet.setChartDataXML(chartRenderer.convertToXML(chartData));
			
			div.getChildren().clear();
			div.appendChild(
					tableRenderer.constructTableWidget(
							portlet.getTableDataMap(), false
							)
						);
			editPortletWindow.detach();
			return;
		}
		else{
			final String divToDraw = div.getId(); 
			try
			{
			chartRenderer.constructChartJSON(chartData, portlet, true);
			chartRenderer.drawChart(chartData, divToDraw, portlet);
			}catch(Exception ex)
			{
				Clients.showNotification("Unable to fetch column data from Hpcc", "error", doneButton.getParent(), "top_left", 3000, true);
				LOG.error("Exception while fetching column data from Hpcc", ex);
			}
			
	
			if (LOG.isDebugEnabled()) {
				LOG.debug("Drawn chart in portlet..");
				LOG.debug("Portlet - Div ID --> " + divToDraw);
			}
			portlet.setChartDataXML(chartRenderer.convertToXML(chartData));
			editPortletWindow.detach();
		}
	}
}
