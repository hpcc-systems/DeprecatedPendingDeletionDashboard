package org.hpccsystems.dashboard.manage.widget;


import java.util.Map.Entry;

import org.hpccsystems.dashboard.ChartTypes;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;
import org.hpccsystems.dashboard.entity.widget.charts.Pie;
import org.hpccsystems.dashboard.entity.widget.charts.Table;
import org.hpccsystems.dashboard.entity.widget.charts.USMap;
import org.hpccsystems.dashboard.entity.widget.charts.XYChart;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.util.DashboardUtil;
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
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Anchorchildren;
import org.zkoss.zul.Anchorlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ChartListController extends SelectorComposer<Component>{
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChartListController.class);
    
    private WidgetConfiguration widgetConfiguration;
    
    @Wire
    private Anchorlayout chartList;
    @Wire
    private Textbox chartname;
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        widgetConfiguration = (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        
        
      
        for (Entry<String, ChartConfiguration> entry : Constants.CHART_CONFIGURATIONS.entrySet()) {
        	
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
            Image img = (Image) event.getTarget();
            ChartConfiguration configuration = (ChartConfiguration) img.getAttribute("config");
            
            if (configuration.getType() == ChartTypes.PIE.getChartCode()
                    || configuration.getType() == ChartTypes.DONUT .getChartCode()) {
                widgetConfiguration.setWidget(new Pie());
            } else if (configuration.getType() == ChartTypes.BAR.getChartCode()
                    || configuration.getType() == ChartTypes.COLUMN .getChartCode()
                    || configuration.getType() == ChartTypes.LINE.getChartCode()
                    || configuration.getType() == ChartTypes.SCATTER.getChartCode()
                    || configuration.getType() == ChartTypes.STEP.getChartCode()
                    || configuration.getType() == ChartTypes.AREA.getChartCode()) {
                widgetConfiguration.setWidget(new XYChart());
            } else if (configuration.getType() == ChartTypes.US_MAP.getChartCode()) {
                widgetConfiguration.setWidget(new USMap());
            }  else if (configuration.getType() == ChartTypes.TABLE.getChartCode()) {
                widgetConfiguration.setWidget(new Table());
            }
            widgetConfiguration.getWidget()
                    .setChartConfiguration(configuration);
            final String charnam = DashboardUtil.removeSpaceSplChar(chartname
                    .getText());
            final String emptyString = "";
            if (emptyString.equals(chartname.getText())
                    || emptyString.equals(charnam)) {
                Clients.showNotification("Enter a valid chart name", "warning",
                        chartname, "end_center", 5000);
            } else {
                widgetConfiguration.getWidget().setTitle(chartname.getText());
                widgetConfiguration.getWidget().setName(charnam);
                Events.postEvent(WidgetConfiguration.ON_CHART_TYPE_SELECT,
                        widgetConfiguration.getHolder(), null);
            }

        }
    }; 

    
   
}
