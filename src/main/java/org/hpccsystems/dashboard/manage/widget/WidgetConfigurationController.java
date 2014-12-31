package org.hpccsystems.dashboard.manage.widget;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.authentication.LoginController;
import org.hpccsystems.dashboard.manage.WidgetConfiguration;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.CompositionService;
import org.hpccsystems.dashboard.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Include;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class WidgetConfigurationController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(WidgetConfigurationController.class);

    
    @Wire
    private Include holder;    
    
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
            holder.setSrc(configuration.getChartConfiguration().getEditLayout());
        });
    }
    
    
    /**
     * once chart is rendered, Hipie composition and plugin is created/updated
     */
    @Listen("onClick = #configOkButton")
    public void onClickOk() {
        compositionService.createComposition(configuration.getDashboard(),
                configuration.getWidget());
        compositionService.runComposition(configuration.getDashboard());
        dashboardService.updateDashboard(configuration.getDashboard(),
                authenticationService.getUserCredential().getId());
    }
    

}
