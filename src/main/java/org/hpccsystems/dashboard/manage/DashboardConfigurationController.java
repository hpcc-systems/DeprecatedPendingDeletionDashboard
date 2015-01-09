package org.hpccsystems.dashboard.manage;

import java.util.Map;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.DashboardService;
import org.hpccsystems.dashboard.util.HipieSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
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
    @Wire
    private Combobox connectionList;
    @Wire
    private Label message;
    
    private Dashboard dashboard;
    
    ListModelList<String> connectionModel = new ListModelList<String>();
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
       
        	 parent = this.getSelf().getParent();       
        	 if(! (parent instanceof Vbox)){     
        		 dashboard = (Dashboard) Executions.getCurrent().getArg().get(Constants.DASHBOARD);
                 nameTextbox.setValue( dashboard.getName());
                  
                  if(dashboard.getVisiblity()==0) {
                      visiblityRadiogroup.setSelectedIndex(0);
                  } else {
                      visiblityRadiogroup.setSelectedIndex(1);
                  }
                  connectionList.setValue(dashboard.getHpccId());
                  
        	 }    
        	 //Get Hipie's available hpcc connections
        	  getHpccConnections();
    }

    private void getHpccConnections() {
        Map<String,HPCCConnection> connections = HipieSingleton.getHipie().getHpccManager()
                .getConnections();
        connectionModel.addAll(connections.keySet());
        connectionList.setModel(connectionModel);
        connectionList.setItemRenderer(new ComboitemRenderer<String>() {

            @Override
            public void render(Comboitem comboitem, String label, int index)
                    throws Exception {
                comboitem.setLabel(label);
            }
        });
    }

    @Listen("onClick = #configOkButton")
    public void onClickOkButton() {
    	  
        if(nameTextbox.getText() == null || nameTextbox.getText().isEmpty()){
            Clients.showNotification(Labels.getLabel("emptyDashboardName"), "error", nameTextbox, "end_center", 5000, true);            
            return;
        }else if(connectionList.getSelectedItem() == null){
            Clients.showNotification(Labels.getLabel("chooseConnection"), "error", connectionList, "end_center", 5000, true);            
            return;
        }   
        
        //Creating new dashboard
        if(parent instanceof Vbox){
            Dashboard dashboard = new Dashboard();
            dashboard.setName(nameTextbox.getText());
            dashboard.setApplicationId(authenticationService.getUserCredential().getApplicationId());
            dashboard.setVisiblity(Integer.parseInt(visiblityRadiogroup.getSelectedItem().getValue().toString()));
            dashboard.setHpccId(connectionList.getSelectedItem().getLabel());
            //inserts dashboard into DB
            dashboardService.insertDashboard(dashboard, authenticationService.getUserCredential().getId());
            Events.postEvent(Constants.ON_ADD_DASHBOARD, parent, dashboard);            
        }else{
        	//Editing dashboard
        	if( ! dashboard.getName().equals(nameTextbox.getValue()) &&
                    ! validateDashboardName()) {
                return;
            }
        	  dashboard.setName(nameTextbox.getValue());
              dashboard.setVisiblity(Integer.parseInt(visiblityRadiogroup.getSelectedItem().getValue().toString()));
              dashboard.setHpccId(connectionList.getSelectedItem().getLabel());
              //updates dashboard into DB
              dashboardService.updateDashboard(dashboard);
        }       
        this.getSelf().detach();
    }
    
    
    private boolean validateDashboardName() {
        if(nameTextbox.getValue() == null || nameTextbox.getValue().trim().length() < 1){
            Clients.showNotification(Labels.getLabel("emptyDashboardName"),"error", nameTextbox, "end_center", 3000, true);
            return false;
        }else if(dashboardService.getDashboards(authenticationService.getUserCredential().getId(), 
                authenticationService.getUserCredential().getApplicationId())
                .contains(nameTextbox.getValue().trim())){
            Clients.showNotification(Labels.getLabel("nameExists"),"error", nameTextbox, "end_center", 3000, true);
            return false;
        }
        return true;
    }
    
}

