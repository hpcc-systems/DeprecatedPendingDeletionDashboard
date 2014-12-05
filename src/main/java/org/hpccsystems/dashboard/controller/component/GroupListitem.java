package org.hpccsystems.dashboard.controller.component;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.services.GroupService;
import org.springframework.dao.DataAccessException;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkplus.spring.SpringUtil;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Popup;

public class GroupListitem extends Listitem{

    private static final long serialVersionUID = 1L;
    private static final  Log LOG = LogFactory.getLog(GroupListitem.class); 
    
    private int dashboardId;
    
    public GroupListitem(Group group, int dashboardId) {
        this.dashboardId = dashboardId;  
        
        final Listcell listCell = new Listcell();
        listCell.setLabel(group.getName());

        final Button deleteButton = new Button();
        deleteButton.setIconSclass("glyphicon glyphicon-remove");
        deleteButton.setZclass("img-btn");
        deleteButton.setStyle("float:right; background: none;");
        deleteButton.addEventListener(Events.ON_CLICK, deleteListener);
        listCell.appendChild(deleteButton);

        final Popup popup = new Popup();
        popup.setWidth("100px");
        popup.setZclass("popup");
        
        final Button button = new Button(Labels.getLabel(group.getRole()));
        button.setZclass("btn btn-xs");
        button.setStyle("font-size: 10px; float: right;");
        button.setPopup(popup);

        final Listbox listbox = new Listbox();
        listbox.setMultiple(false);
        listbox.appendItem(Labels.getLabel(Constants.ROLE_ADMIN), Constants.ROLE_ADMIN);
        listbox.appendItem(Labels.getLabel(Constants.ROLE_CONTRIBUTOR), Constants.ROLE_CONTRIBUTOR);
        listbox.appendItem(Labels.getLabel(Constants.ROLE_CONSUMER), Constants.ROLE_CONSUMER);

        listbox.addEventListener(Events.ON_SELECT, roleAssignListener);

        popup.appendChild(listbox);
        listCell.appendChild(popup);
        listCell.appendChild(button);
        listCell.setParent(this);
        this.setAttribute(Constants.GROUP, group);
    }
    
    private EventListener<SelectEvent<Component, Object>> roleAssignListener = new EventListener<SelectEvent<Component, Object>>() {

        @Override
        public void onEvent(final SelectEvent<Component, Object> event) throws Exception {
            final Listitem selectedItem = (Listitem) event.getSelectedItems().iterator().next();
            final Popup popup = (Popup) selectedItem.getParent().getParent();
            final Listcell listcell = (Listcell) popup.getParent();
            final Group group = (Group) listcell.getParent().getAttribute(Constants.GROUP);
            group.setRole(selectedItem.getValue().toString());
            
            try {
                GroupService groupService = (GroupService) SpringUtil.getBean(Constants.GROUP_SERVICE);
                groupService.updateGroupRole(dashboardId, group);
            } catch (DataAccessException ex) {
                Clients.showNotification("Unable to update Roles", Constants.ERROR_NOTIFICATION,
                        GroupListitem.this, "after_center", 3000, true);
                LOG.error(Constants.EXCEPTION, ex);
            }
            Button button = null;
            for (final Component component : listcell.getChildren()) {
                if (component instanceof Button) {
                    button = (Button) component;
                }
            }
            button.setLabel(selectedItem.getLabel());
            popup.close();
        }
    };
    
    private EventListener<Event> deleteListener = new EventListener<Event>() {
        public void onEvent(final Event event) {
            final Listitem listitem = (Listitem) event.getTarget().getParent().getParent();
            final Group group = (Group) listitem.getAttribute(Constants.GROUP);
            listitem.detach();
            
            try {
                GroupService groupService = (GroupService) SpringUtil.getBean(Constants.GROUP_SERVICE);
                groupService.deleteGroup(dashboardId, group);
            } catch (DataAccessException ex) {
                Clients.showNotification("Unable to delete Groups", "error",
                        GroupListitem.this, "after_center", 3000, true);
                LOG.error("Exception while deleting groups", ex);
            }
        }
    };

}
