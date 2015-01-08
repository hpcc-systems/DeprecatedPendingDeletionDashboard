package org.hpccsystems.dashboard.manage.widget;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.CompositionService;
import org.hpccsystems.dashboard.service.DashboardService;
import org.hpccsystems.dashboard.util.DashboardExecutorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Include;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class WidgetConfigurationController extends SelectorComposer<Component> implements EventListener<Event>{

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetConfigurationController.class);

    
    @Wire
    private Include holder;    
    @WireVariable
    private Desktop desktop;
    @WireVariable
    private CompositionService compositionService;
    @WireVariable
    private DashboardService dashboardService;
    @WireVariable
    private AuthenticationService authenticationService;
    
    private WidgetConfiguration configuration;
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        configuration = 
                (WidgetConfiguration) Executions.getCurrent().getArg().get(Constants.WIDGET_CONFIG);
        configuration.setHolder(holder);
        
        holder.setDynamicProperty(Constants.WIDGET_CONFIG, configuration);
        holder.setSrc("widget/chartList.zul");
        holder.addEventListener(WidgetConfiguration.ON_CHART_TYPE_SELECT, event -> {
                holder.setSrc("widget/fileBrowser.zul");
        });
        
        holder.addEventListener(WidgetConfiguration.ON_FILE_SELECT, event -> {
            holder.setSrc(configuration.getWidget().getChartConfiguration().getEditLayout());
        });
    }
    
    
    /**
     * Saves/Updates Composition
     * Runs the composition
     * Pushes the preview chart to dashboard view
     * @throws Exception 
     */
    @Listen("onClick = #configOkButton")
    public void onClickOk() throws Exception {
        if (configuration.getWidget() == null || !configuration.getWidget().isConfigured()) {
            Clients.showNotification(Labels.getLabel("widgetNotConfigured"), 
                    Clients.NOTIFICATION_TYPE_ERROR,
                    configuration.getChartDiv(), "middle_center", 5000, true);
            return;
        }
        
        String userId = authenticationService.getUserCredential().getId();
        if (configuration.getDashboard().getCompositionName() == null) {
            compositionService.createComposition(configuration.getDashboard(), configuration.getWidget(), userId);
        } else {
            compositionService.updateComposition(configuration.getDashboard(), configuration.getWidget(), userId);
        }
        
     // Run composition in separate thread
        desktop.enableServerPush(true);
        Runnable runComposition = () -> {
            try {
                compositionService.runComposition(configuration.getDashboard(), userId);
            } catch (Exception e) {
                Executions.schedule(desktop, this, new Event("OnRunCompositionFailed", null, e));
                LOGGER.error(Constants.EXCEPTION, e);
            }
            Executions.schedule(desktop, this, new Event("OnRunCompositionCompleted"));
        };
        DashboardExecutorHolder.getExecutor().execute(runComposition);
        
        dashboardService.updateDashboard(configuration.getDashboard(), userId);
        
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Composition {}, Run sucessfully", configuration.getDashboard().getCompositionName());
        }
        
        this.getSelf().detach();
        drawChart();
    }

    /**
     * Renders chart in dashboard container
     */
	private void drawChart() {
	    Clients.evalJavaScript("injectPreviewChart()");
	}
	
	@Override
    public void onEvent(Event arg0) throws Exception {
        
        if ("OnRunCompositionCompleted".equals(arg0.getName())) {
            LOGGER.debug("Run Composition completed");
        } else {
            LOGGER.debug("Run Composition failed");
            //need to post the event to dashboard controller to show the failure message to the user.
        }
    }

}
