package org.hpccsystems.dashboard.manage.widget;


import java.util.Map.Entry;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.Constants.CHART_TYPES;
import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;
import org.hpccsystems.dashboard.entity.widget.charts.Pie;
import org.hpccsystems.dashboard.entity.widget.charts.USMap;
import org.hpccsystems.dashboard.entity.widget.charts.XYChart;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Anchorchildren;
import org.zkoss.zul.Anchorlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ChartListController extends SelectorComposer<Component>{
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChartListController.class);
    
    private WidgetConfiguration widgetConfiguration;
    
    @Wire
    private Anchorlayout chartList;
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        widgetConfiguration = (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        
        
      
        for (Entry<CHART_TYPES, ChartConfiguration> entry : Constants.CHART_CONFIGURATIONS.entrySet()) {
        	
        	Anchorchildren anchorChildren = new Anchorchildren();
        	Vbox vbox = new Vbox();
        	Label label = new Label();
        	label.setValue(entry.getValue().getName()+" Chart");
        	Image img = new Image();
        	img.setAttribute("config", entry.getValue());
        	img.setSrc(entry.getValue().getStaticImage());
        	img.setHeight("100px");
        	img.setWidth("200px");
        	img.setStyle("cursor:pointer");
			img.addEventListener(Events.ON_CLICK, editChartPanel);
        	anchorChildren.setParent(chartList);
        	label.setParent(vbox);
        	img.setParent(vbox);
        	vbox.setParent(anchorChildren);
        	
        }
    }
    
    EventListener<Event> editChartPanel = new EventListener<Event>() {
        
        @Override
        public void onEvent(Event event) throws Exception {
        	Image img=(Image) event.getTarget();
        	 ChartConfiguration configuration= (ChartConfiguration) img.getAttribute("config");        
        	 if(configuration.getType()==CHART_TYPES.PIE || configuration.getType()==CHART_TYPES.DONUT){
        		 widgetConfiguration.setWidget(new Pie()); 
        	 }else if(configuration.getType()==CHART_TYPES.BAR ||configuration.getType()==CHART_TYPES.COLUMN ||configuration.getType()==CHART_TYPES.LINE){
        		 widgetConfiguration.setWidget(new XYChart());
        	 }else if(configuration.getType()==CHART_TYPES.US_MAP ){
        		 widgetConfiguration.setWidget(new USMap());
        	 }
        	  widgetConfiguration.getWidget().setChartConfiguration(configuration);
        	 Events.postEvent(WidgetConfiguration.ON_CHART_TYPE_SELECT, widgetConfiguration.getHolder(), null);
           
        }
    }; 

    
   
}
