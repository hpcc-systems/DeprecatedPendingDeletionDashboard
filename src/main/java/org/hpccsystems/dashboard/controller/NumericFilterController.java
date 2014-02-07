package org.hpccsystems.dashboard.controller;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.entity.chart.XYChartData;
import org.hpccsystems.dashboard.entity.chart.utils.ChartRenderer;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.HPCCService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.ScrollEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Slider;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class NumericFilterController extends SelectorComposer<Component>{

	private static final long serialVersionUID = 1L;
	private static final  Log LOG = LogFactory.getLog(NumericFilterController.class);
	
	private Portlet portlet;
	private XYChartData chartData;
	private Button doneButton;
	
	@WireVariable
	ChartRenderer chartRenderer;
	
	@WireVariable
	HPCCService hpccService;
	
	@WireVariable
	AuthenticationService  authenticationService;
	
	@Wire
	Slider minimumSlider;
	@Wire
	Label minimumLabel;
	
	@Wire
	Slider maximumSlider;
	@Wire
	Label maximumLabel;
	@Wire
	Button filtersSelectedBtn;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		portlet = (Portlet) Executions.getCurrent().getAttribute(Constants.PORTLET);
		chartData = (XYChartData) Executions.getCurrent().getAttribute(Constants.CHART_DATA);
		doneButton = (Button) Executions.getCurrent().getAttribute(Constants.EDIT_WINDOW_DONE_BUTTON);
		
		Map<Integer, Integer> map = null;
		try	{
			map = hpccService.fetchFilterMinMax(chartData);
		} catch(Exception e) {
			Clients.showNotification("Unable to fetch data to Filter for the column dropped", 
					"error", doneButton.getParent().getParent().getParent(), "middle_center", 3000, true);
			LOG.error("Exception while fetching data from Hpcc for selected Numeric filter", e);
			return;
		}
		
		Integer min = map.get(Constants.FILTER_MINIMUM);
		Integer max = map.get(Constants.FILTER_MAXIMUM);
		
		minimumLabel.setValue(min.toString());
		maximumLabel.setValue(max.toString());
		
		//TODO Add minimum positions after upgrading to zk 7.0.1 
		minimumSlider.setMaxpos(max);
		minimumSlider.setCurpos(min);
		
		maximumSlider.setMaxpos(max);
		maximumSlider.setCurpos(max);
	}
	
	@Listen("onScroll = #minimumSlider")
	public void onMinSliderScroll(ScrollEvent event) {
		minimumLabel.setValue(String.valueOf(minimumSlider.getCurpos()));
	}
	
	@Listen("onScroll = #maximumSlider")
	public void onMaxSliderScroll(ScrollEvent event) {
		int currentMinimumPos = minimumSlider.getCurpos();
		
		maximumLabel.setValue(String.valueOf(maximumSlider.getCurpos()));
		
		minimumSlider.setMaxpos(maximumSlider.getCurpos());
		minimumSlider.setCurpos(currentMinimumPos);
		
		if(currentMinimumPos > maximumSlider.getCurpos()){
			minimumSlider.setCurpos(maximumSlider.getCurpos());
			minimumLabel.setValue(String.valueOf(maximumSlider.getCurpos()));
		}
	}
	
	@Listen("onClick = button#filtersSelectedBtn")
	public void onfiltersSelected() {
				
		chartData.getFilter().setStartValue((double) minimumSlider.getCurpos());
		chartData.getFilter().setEndValue((double) maximumSlider.getCurpos());
		chartData.setIsFiltered(true);
		try	{
			chartRenderer.constructChartJSON(chartData, portlet, true);
			chartRenderer.drawChart(chartData, Constants.EDIT_WINDOW_CHART_DIV, portlet);
		}catch(Exception ex) {
			Clients.showNotification("Unable to fetch column data from HPCC", "error", 
					doneButton.getParent().getParent().getParent(), "middle_center", 3000, true);
			LOG.error("Exception while fetching column data from Hpcc", ex);
			return;
		}		
		
		if(!authenticationService.getUserCredential().getApplicationId().equals(Constants.CIRCUIT_APPLICATION_ID) || 
				authenticationService.getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){
			doneButton.setDisabled(false);	
		}

		if(LOG.isDebugEnabled()){
			LOG.debug("Drawn filtered chart with Numeric filter");
		}
	}
}
