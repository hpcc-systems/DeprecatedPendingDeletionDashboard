package org.hpccsystems.dashboard.controller;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class DashboardConfigurationController extends SelectorComposer<Component>{

    private static final long serialVersionUID = 1L;
    private static final  Log LOG = LogFactory.getLog(DashboardConfigurationController.class); 

    @Wire
    Radiogroup layoutRadiogroup, visiblityRadiogroup;
    
    @Wire
    Textbox nameTextbox;
    @Wire
    Checkbox commonFiltersCheckbox;
    @Wire
    Hbox commonFilterHbox;
    @Wire
    private Hlayout layout;
    
    private Component parent;
    private Dashboard dashboard;
    private boolean isCommonFiltersEnabled= false;
    
    @WireVariable
    private DashboardService dashboardService;
    @WireVariable
    private AuthenticationService authenticationService;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
           
        parent = (Component) Executions.getCurrent().getArg().get(Constants.PARENT);    
                
        if(parent instanceof Window) {
            //Dashboard already created
            
            //removing single layout image, as cannot change to single layout from other layouts
            layoutRadiogroup.removeChild(layoutRadiogroup.getFirstChild());
            
            List<Radio> radioList = layoutRadiogroup.getItems();
        	
            dashboard = (Dashboard) Executions.getCurrent().getArg().get(Constants.DASHBOARD);
            
            commonFilterHbox.setVisible(true);
            nameTextbox.setValue(dashboard.getName());
            
            if(dashboard.getVisibility().equals(Constants.VISIBLITY_PRIVATE)) {
                visiblityRadiogroup.setSelectedIndex(0);
            } else {
                visiblityRadiogroup.setSelectedIndex(1);
            }
            
            if(dashboard.getHasCommonFilter()){
                commonFiltersCheckbox.setChecked(true);
                isCommonFiltersEnabled = true;
            }
            
            try {
                radioList.get(dashboard.getColumnCount() - 1).setSelected(true);
            } catch (ArrayIndexOutOfBoundsException e) {
                LOG.error(Constants.EXCEPTION, e);
                Clients.showNotification(Labels.getLabel("noWidgetException"), "info", getSelf(), "middle_center", 3000, true);
            }   
            //To disable layout change and common filter features for single/full page layout dashboard
            if(dashboard.getPortletList().size() == 1 && dashboard.getPortletList().get(0).getIsSinglePortlet()){
                layout.setVisible(false);
                commonFilterHbox.setVisible(false);
            }
        	
        } else {
        	
        	List<Radio> radioList = layoutRadiogroup.getItems();
            //Creating a new Dashboard             
            //Setting two column layout as default
            radioList.get(2).setSelected(true);
        }
        
    }
    
    
    
    @Listen("onClick = #dashConfigDoneButton")
    public void done() {
        if(parent instanceof Window) {
        	
            if( ! dashboard.getName().equals(nameTextbox.getValue()) &&
                    ! validateDashboardName()) {
                return;
            }
            
            //Changing configuration of existing board            
            dashboard.setName(nameTextbox.getValue());
            dashboard.setHasCommonFilter(commonFiltersCheckbox.isChecked());
            
            //Removing Common HpccConnection object from session, if Common filters are disabled
            if(!commonFiltersCheckbox.isChecked()) {
                Sessions.getCurrent().setAttribute(Constants.HPCC_CONNECTION, null);
            }
            dashboard.setColumnCount(Integer.parseInt(layoutRadiogroup.getSelectedItem().getValue().toString()));
            dashboard.setVisibility(Integer.parseInt(visiblityRadiogroup.getSelectedItem().getValue().toString()));
            Events.sendEvent("onLayoutChange", parent, isCommonFiltersEnabled);
            this.getSelf().detach();
        } else {
            //Creating new Board
            boolean siglePortletEnabled = false;
            if(validateDashboardName()) {
                dashboard = new Dashboard();
                // Setting ADMIN role as the dashboard is being created
                dashboard.setRole(Constants.ROLE_ADMIN);
                dashboard.setName(nameTextbox.getValue());
                dashboard.setColumnCount(Integer.parseInt(layoutRadiogroup.getSelectedItem().getValue().toString()));
                dashboard.setVisibility(Integer.parseInt(visiblityRadiogroup.getSelectedItem().getValue().toString()));
                dashboard.setLastupdatedDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
    
                dashboard.setHasCommonFilter(commonFiltersCheckbox.isChecked());
                
                //Deciding Columns and rows
                Integer panelCount = null;
                panelCount = dashboard.getColumnCount();
                if(dashboard.getColumnCount() == 0){
               	 panelCount = 1;
               	 //for single layout,column & row will be one
               	 dashboard.setColumnCount(1);
               	siglePortletEnabled = true;
               }
                
                if(LOG.isDebugEnabled()){
                    LOG.debug("Creating A New Dashboard.. and adding panels");
                }
                for(int i=1; i <= dashboard.getColumnCount(); i++){
                    for(int j=1; j <= panelCount/dashboard.getColumnCount() ; j++){
                    	
                        final Portlet portlet = new Portlet();
                        
                        //generating portlet id
                        portlet.setColumn(i - 1);
                        portlet.setWidgetState(Constants.STATE_EMPTY);
                        if(panelCount == 1 && siglePortletEnabled){
                        	portlet.setIsSinglePortlet(true);
                    	}
                        dashboard.getPortletList().add(portlet);
                    }
                }
                Events.sendEvent("onCloseDialog", parent, dashboard);
                this.getSelf().detach();
            }
        }
        
        
    }
    
    
    /**
     * Validates dashboard name and Displays appropriate error message in UI
     * @return boolean
     *     true if Dashboard name entered is valid, false otherwise
     */
    private boolean validateDashboardName() {
        if(nameTextbox.getValue() == null || nameTextbox.getValue().trim().length() < 1){
            Clients.showNotification(Labels.getLabel("emptyDashboardName"),"error", nameTextbox, "end_center", 3000, true);
            return false;
        }else if(dashboardService.getDashboardName(authenticationService.getUserCredential().getUserId(), 
                authenticationService.getUserCredential().getApplicationId())
                .contains(nameTextbox.getValue().trim())){
            Clients.showNotification(Labels.getLabel("nameExists"),"error", nameTextbox, "end_center", 3000, true);
            return false;
        }
        return true;
    }

    @Listen("onCheck = #commonFiltersCheckbox")
    public void commonFilerToggle() {
       if( parent instanceof Window && 
                dashboard.hasLiveChart() && dashboard.getCommonHpccConnection() == null) {
            commonFiltersCheckbox.setChecked(false);
            Clients.showNotification(Labels.getLabel("noCommonHpccData"),
                    "warning", commonFiltersCheckbox, "end_center", 5000, true);
            return;
        }            
        if(!commonFiltersCheckbox.isChecked()){
            EventListener<ClickEvent> removeAllGlobalFilters = new EventListener<ClickEvent>() {
                @Override
                public void onEvent(ClickEvent event) throws Exception {
                    //message box to confirm, removing common filter
                    if (Messagebox.Button.YES.equals(event.getButton())) {    
                        isCommonFiltersEnabled = false;    
                        dashboard.setHasCommonFilter(false);
                    }else if(Messagebox.Button.NO.equals(event.getButton())) {
                        dashboard.setHasCommonFilter(true);
                    }
                }
            };
                    
            Map<String, String> params = new HashMap<String, String>();
            params.put("sclass", "panel");

            Messagebox.show(
                    Constants.REMOVE_GLOBAL_FILTERS, 
                    Constants.REMOVE_GLOBAL_FILTERS_TITLE, 
                    new Messagebox.Button[]{
                            Messagebox.Button.YES, 
                            Messagebox.Button.NO },
                    new String[] {
                        "Yes", "No"
                    },
                    Messagebox.QUESTION,
                    Messagebox.Button.YES,
                    removeAllGlobalFilters, 
                    params
                    );
        }
    }
}
