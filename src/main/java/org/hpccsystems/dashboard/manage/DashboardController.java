package org.hpccsystems.dashboard.manage;

import java.util.HashMap;
import org.hpccsystems.dashboard.Constants;
import org.hpccsystems.dashboard.authentication.LoginController;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.hpccsystems.dashboard.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Include;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import org.zkoss.zul.Messagebox.ClickEvent;

public class DashboardController extends SelectorComposer<Component>{
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
    @WireVariable
    private AuthenticationService authenticationService;
    @WireVariable
    private DashboardService dashboardService;
    private Dashboard dashboard;
    
    @Listen("onClick = #addWidget")
    public void onAddWidget() {
        //TODO Remove instantiation
        dashboard = new Dashboard();
        dashboard.setHpccId("dev-dashboard");
        
        Window window = (Window)Executions.createComponents(
                "widget/config.zul", null, new HashMap<String, Object>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put(Constants.WIDGET_CONFIG, new WidgetConfiguration(dashboard));
                    }
                });
        
        window.doModal();
    }
    
    /**
     * deleteDashboard() is used to delete the selected Dashboard in the sidebar page.
     */
    @Listen("onClick = #deleteDashboard")
    public void deleteDashboard() {
        try{
         // ask confirmation before deleting dashboard
         EventListener<ClickEvent> clickListener = new EventListener<Messagebox.ClickEvent>() {
             public void onEvent(ClickEvent event) {
                 
                 if(Messagebox.Button.YES.equals(event.getButton())) {
                    final Listbox navBar  = (Listbox) Selectors.iterable(DashboardController.this.getSelf().getPage(), "#dashboardListbox").iterator().next();
                       
                    navBar.getSelectedItem().setVisible(false);
                       
                       final Include include = (Include) Selectors.iterable(DashboardController.this.getSelf().getPage(), "#dashboardInclude")
                               .iterator().next();
                       if(LOGGER.isDebugEnabled()){
                           LOGGER.debug("Setting first visible Nav item as active");
                       }
                       
                       Listitem navitem;
                       Boolean isSelected = false;
                       for (Component component : navBar.getChildren()) {
                           navitem = (Listitem) component;
                           if(navitem.isVisible()){
                               //Selecting first visible Item
                               if(!isSelected){
                                   navitem.setSelected(true);
                                   Events.sendEvent(Events.ON_CLICK, navitem, null);
                                   isSelected = !isSelected;
                               }
                           }
                       }
                       
                       if( !isSelected ) {
                           final Component component2 = include.getParent();
                           include.detach();
                           final Include newInclude = new Include("/dashboard/container.zul");
                           newInclude.setId("dashboardInclude");
                           component2.appendChild(newInclude);
                           Clients.evalJavaScript("showPopUp()");
                       }                       
                       dashboardService.deleteDashboard(dashboard.getId());
                 }

               } 
           };
           Messagebox.show(Labels.getLabel("deletedashboard"), Labels.getLabel("deletedashboardtitle"), new Messagebox.Button[]{
               Messagebox.Button.YES, Messagebox.Button.NO }, Messagebox.QUESTION, clickListener);
        }catch(DataAccessException ex){
            Clients.showNotification(Labels.getLabel("unableToDeleteDashboard"), Constants.ERROR_NOTIFICATION, this.getSelf(), Constants.POSITION_CENTER, 3000, true);
            LOGGER.error("Exception while deleting Dashboard in DashboardController", ex);
            return;
        }catch(Exception ex){
            Clients.showNotification(Labels.getLabel("unableToDeleteDashboard"), Constants.ERROR_NOTIFICATION, this.getSelf(), Constants.POSITION_CENTER, 3000, true);
            LOGGER.error("Exception while deleting Dashboard in DashboardController", ex);
            return;            
        }
  }    
}
