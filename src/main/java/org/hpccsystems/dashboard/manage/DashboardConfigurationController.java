package org.hpccsystems.dashboard.manage;

import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DashboardConfigurationController extends
        SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(DashboardConfigurationController.class);
    
    private Component parent;
    @Wire
    private Textbox nameTextbox;
    @WireVariable
    private DashboardService dashboardService;
    @WireVariable
    private AuthenticationService authenticationService;
    @Wire
    private Radiogroup visiblityRadiogroup;
    
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
       
        parent = this.getSelf().getParent();
    }

    @Listen("onClick = #configOkButton")
    public void onClickOkButton() {
        
        //Creating new dashboard
        if(parent instanceof Vbox){
            Dashboard dashboard = new Dashboard();
            dashboard.setName(nameTextbox.getText());
            dashboard.setApplicationId(authenticationService.getUserCredential().getApplicationId());
            dashboard.setVisiblity(Integer.parseInt(visiblityRadiogroup.getSelectedItem().getValue().toString()));
            //inserts dashboard into DB
            dashboardService.insertDashboard(dashboard, authenticationService.getUserCredential().getId());
            Events.postEvent(Constants.ON_ADD_DASHBOARD, parent, dashboard);            
        }else{
          //Editing dashboard
            
        }
       
        this.getSelf().detach();
    }
}
