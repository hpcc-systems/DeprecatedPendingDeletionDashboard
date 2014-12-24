package org.hpccsystems.dashboard.manage;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Window;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class NavigationController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(NavigationController.class);

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        this.getSelf().addEventListener(Constants.ON_ADD_DASHBOARD, new EventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                addDashbordToNavbar((Dashboard)event.getData());
            }
        });
    }

    /**Adds and selects the new dashboard into the side navbar.
     * @param dashboard
     */
    protected void addDashbordToNavbar(Dashboard dashboard) {
        System.out.println("dashboard -->"+dashboard);
    }

    @Listen("onClick = #addDashboard")
    public void onAddDashboard() {
        
        final Window window = (Window) Executions.createComponents(
                "/dashboard/dashboard_config.zul", this.getSelf(),
                null);
        window.doModal();
    }

}
