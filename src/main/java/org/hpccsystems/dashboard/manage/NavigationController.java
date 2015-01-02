package org.hpccsystems.dashboard.manage;

import java.util.ArrayList;
import java.util.List;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Window;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class NavigationController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationController.class);
    Page dashboardContainer = NavigationController.this.getPage();

    @Wire
    private Listbox dashboardListbox;

    @WireVariable
    private DashboardService dashboardService;
    @WireVariable
    private AuthenticationService authenticationService;

    private ListModelList<Dashboard> dashboardModel = new ListModelList<Dashboard>();
    
    private EventListener<SelectEvent<Listitem, Dashboard>> navItemSelectListener = event -> {
        showSelectedDashboard(event.getSelectedObjects().iterator().next());
    };

    private ListitemRenderer<Dashboard> navRenderer = (listitem, dashboard, index) -> {
        Listcell iconChild = new Listcell();
        iconChild.setIconSclass("glyphicon glyphicon-stats");
        listitem.appendChild(iconChild);
        listitem.setLabel(dashboard.getName());
        listitem.setAttribute(Constants.DASHBOARD, dashboard);
    };
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        dashboardModel.addAll(getDashboards());
        dashboardListbox.setModel(dashboardModel);
        dashboardListbox.setItemRenderer(navRenderer);
        dashboardListbox.addEventListener(Events.ON_SELECT, navItemSelectListener);
        
        // Selecting first dashboard
        comp.addEventListener("onLoading", event -> {
           select(dashboardModel.iterator().next()); 
        });
        if (!dashboardModel.isEmpty()) {
            Events.postEvent("onLoading", comp, null);
        }
        
        // Listener to be invoked from 'Dashboard Config Controller'
        // Adds new dashboard to the list and selects it
        this.getSelf().addEventListener(Constants.ON_ADD_DASHBOARD, event -> {
            Dashboard dashboard = (Dashboard) event.getData();
            dashboardModel.add(dashboard);
            select(dashboard);
        });
        
        // Removes Dashboard
        this.getSelf().addEventListener(Constants.ON_DELTE_DASHBOARD, event -> {
            Dashboard dashboard = (Dashboard) event.getData();
            int index = dashboardModel.indexOf(dashboard);
            dashboardService.deleteDashboard(dashboard.getId());
            dashboardModel.remove(dashboard);
            Include include = (Include) getSelf().getFellow("container");
            include.setSrc(null);
            select(index);
        });
    }

    private void select(Dashboard dashboard) {
        List<Dashboard> selectionList = new ArrayList<Dashboard>();
        selectionList.add(dashboard);
        dashboardModel.setSelection(selectionList);
        showSelectedDashboard(dashboard);
    }

    /**
     * Selects the dashbord specified. Upon failing selects the last dahboard available
     * @param index
     */
    private void select(int index) {
        if(dashboardModel.size() > index) {
            select(dashboardModel.get(index));
        } else if (!dashboardModel.isEmpty()) {
            select(index < 0 ? 0 : index - 1);
        }
    }

    private List<Dashboard> getDashboards() {
        List<Dashboard> dashboardList = dashboardService.getDashboards(authenticationService.getUserCredential().getId(),
                authenticationService.getUserCredential().getApplicationId());
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("dashboardList {}", dashboardList);
        }
        return dashboardList;
    }

    @Listen("onClick = #addDashboard")
    public void onAddDashboard() {
        final Window window = (Window) Executions.createComponents("dashboard/config.zul", this.getSelf(), null);
        window.doModal();
    }

    private void showSelectedDashboard(Dashboard dashboard) {
        Include include = (Include) getSelf().getFellow("container");
        include.setSrc(null);
        include.setDynamicProperty(Constants.ACTIVE_DASHBOARD, dashboard);
        include.setSrc("dashboard/container.zul");
    }
    
}
