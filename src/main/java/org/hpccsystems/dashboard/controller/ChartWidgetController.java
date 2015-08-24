package org.hpccsystems.dashboard.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.component.ChartPanel;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.ChartService;
import org.hpccsystems.dashboard.services.WidgetService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vbox;
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
    Rows rows;
    @Wire
    Window addChartWindow;
    
    private Portlet portlet; 
    
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        ChartService chartService = (ChartService)SpringUtil.getBean(Constants.CHART_SERVICE);
        Map<Integer, ChartDetails> charts = chartService.getCharts();
        
        Iterator<Entry<Integer, ChartDetails>> iterator = charts.entrySet().iterator();
        ChartDetails chartDetails;
        Row row;
        Vbox vbox; 
        Image image; 
        Label label; 
        Button button;
        while(iterator.hasNext()) {
            row = new Row();
            for (int i = 0; i < 4; i++) {
                if(!iterator.hasNext()) {
                    break;
                }
                Entry<Integer, ChartDetails> entry = iterator.next();
                chartDetails = entry.getValue();
                
                vbox = new Vbox();
                vbox.setAlign("center");
                
                image = new Image(chartDetails.getConfiguration().getImageURL());
                image.setZclass("addwidget-img");
                
                label = new Label(chartDetails.getName());
                
                button = new Button("Add");
                button.setZclass("btn btn-primary btn-sm");
                button.setAttribute(Constants.CHART_TYPE, chartDetails.getId());
                button.addEventListener(Events.ON_CLICK, addListener);
                
                vbox.appendChild(image);
                vbox.appendChild(label);
                vbox.appendChild(button);
                row.appendChild(vbox);
            }
            rows.appendChild(row);
        }
        
                
        portlet = (Portlet) Executions.getCurrent().getArg().get(Constants.PORTLET);
        
        if(LOG.isDebugEnabled()){
            LOG.debug("Portlet in ChartWidgetController.doAfterCompose()--->"+portlet);
        }
        
        //Retrieve parameters passed here by the caller
        parentDiv = (ChartPanel) arg.get(Constants.PARENT);
        if(LOG.isDebugEnabled()){
            LOG.debug("ChartWidgetController's PARENT DIV  " + parentDiv);
        }
        
    }
    
    final EventListener<Event> addListener = new EventListener<Event>() {
        @Override
        public void onEvent(final Event event) {
            final Map<String,Integer> paramMap = new HashMap<String, Integer>();
            Integer chartType =  (Integer) event.getTarget().getAttribute(Constants.CHART_TYPE);
            LOG.debug("Constants.CHART_TYPE............ "+chartType);
            paramMap.put(Constants.CHART_TYPE, chartType);
            
            portlet.setChartType(chartType);
            portlet.setWidgetState(Constants.STATE_GRAYED_CHART);
            
            //Add new widget into DB with chart type and 'Grayed' state
            try{
                WidgetService widgetService =(WidgetService) SpringUtil.getBean(Constants.WIDGET_SERVICE);
                widgetService.updateWidget(portlet);
            }catch(Exception ex){
                LOG.error(Constants.EXCEPTION, ex);
            }
            
            Events.sendEvent(new Event("onCloseDialog", parentDiv, paramMap));
            addChartWindow.detach();
        }
    };

}
