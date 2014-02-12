package org.hpccsystems.dashboard.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.ChartPanel;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.WidgetService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

/**
 * ChartWidgetController class is used to handle the add widget in Dashboard project
 *  and controller class for add_widget.zul.
 *
 */
public class ChartWidgetController extends GenericForwardComposer<Component> {
	
	private static final long serialVersionUID = 1L;
	private static final  Log LOG = LogFactory.getLog(ChartWidgetController.class);

	ChartPanel parentDiv;
	
	@Wire
	Button barChartButton;
	@Wire
	Button lineChartButton;
	@Wire
	Button pieChartButton;
	@Wire
	Button tableWidget;
	@Wire
	Window idWindow;
	
		
	@Override
	public void doAfterCompose(final Component comp) throws Exception {
		super.doAfterCompose(comp);		
		barChartButton.setAttribute(Constants.CHART_TYPE, Constants.BAR_CHART);
		lineChartButton.setAttribute(Constants.CHART_TYPE, Constants.LINE_CHART);
		pieChartButton.setAttribute(Constants.CHART_TYPE, Constants.PIE_CHART);
		tableWidget.setAttribute(Constants.CHART_TYPE, Constants.TABLE_WIDGET);
		final Portlet portlet = (Portlet) Executions.getCurrent().getArg()
				.get(Constants.PORTLET);
		if(LOG.isDebugEnabled()){
			LOG.debug("Portlet in ChartWidgetController.doAfterCompose()--->"+portlet);
		}
		//Retrieve parameters passed here by the caller
		parentDiv = (ChartPanel) arg.get(Constants.PARENT);
		if(LOG.isDebugEnabled()){
			LOG.debug("ChartWidgetController's PARENT DIV  " + parentDiv);
		}
		
		//Listener event when 'Add Dashboard' button is clicked
		final EventListener<Event> closeClick = new EventListener<Event>() {
			public void onEvent(final Event event) throws Exception {
				final Map<String,Integer> paramMap = new HashMap<String, Integer>();
				Integer chartType =  (Integer) event.getTarget().getAttribute(Constants.CHART_TYPE);
				paramMap.put(Constants.CHART_TYPE, chartType);
				portlet.setChartType(chartType);
				portlet.setWidgetState(Constants.STATE_GRAYED_CHART);
				//Add new widget into DB with chart type and 'Grayed' state
				try{
				WidgetService widgetService =(WidgetService) SpringUtil.getBean("widgetService");
				widgetService.updateWidget(portlet);
				}catch(Exception ex){
					LOG.error("Exception while adding 'Gray' chart", ex);
				}
				
				Events.sendEvent(new Event("onCloseDialog", parentDiv, 
						paramMap));
				idWindow.detach();
				
			}
		};		
		barChartButton.addEventListener(Events.ON_CLICK, closeClick);
		lineChartButton.addEventListener(Events.ON_CLICK, closeClick);
		pieChartButton.addEventListener(Events.ON_CLICK, closeClick);
		tableWidget.addEventListener(Events.ON_CLICK, closeClick);
	}

}
