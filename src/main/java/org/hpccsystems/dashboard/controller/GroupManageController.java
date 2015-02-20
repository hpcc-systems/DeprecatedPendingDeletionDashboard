package org.hpccsystems.dashboard.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.UserService;
import org.hpccsystems.dashboard.services.impl.DBGroupServiceImpl;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Textbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class GroupManageController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final  Log LOG = LogFactory.getLog(GroupManageController.class);
    
    
    @Wire
    private Textbox oldPwd;
    @Wire
    private Textbox newPwd;
    @Wire
    private Textbox confirmPwd;
    @Wire
    private Panel pwdPanel;
    @Wire
    private Panel groupPanel;
    @Wire
    private Listbox groupListbox;
    @Wire
    private Listbox groupUserListbox;
    @Wire
    private Listbox userListbox;
    @Wire
    private Textbox groupname;
    
    @WireVariable
    private AuthenticationService authenticationService;
    @WireVariable
    private UserService userService;
    @WireVariable
    private DBGroupServiceImpl  dbGroupService;
    
    private ListModelList<Group> groupModel = new ListModelList<Group>();
    private  ListModelList<User> groupUserModel =null;
    private ListModelList<User> userModel = new ListModelList<User>();
    
    EventListener<Event> showGroupusers = (event)->{
        Group selectdGroup = (Group)((Listitem)event.getTarget()).getValue();
        List<User> groupUsers = dbGroupService.getGroupUsers(selectdGroup);
        userModel.clearSelection();
        groupUserModel = new ListModelList<User>();
        groupUserModel.addAll(groupUsers);
        groupUserListbox.setModel(groupUserModel);
        groupUserListbox.setItemRenderer((listitem,userr,index) ->{
            User user =(User)userr;
            listitem.setLabel(user.getFullName());
            listitem.setValue(user);
        });
        
    };
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        //Group management is enabled only for the super user
        if(authenticationService.getUserCredential().isSuperUser()){
            groupPanel.setVisible(true);
            groupModel.addAll(dbGroupService.getGroups());
            groupListbox.setModel(groupModel);
            groupListbox.setItemRenderer((listitem,groupp,index) ->{
                Group group =(Group)groupp;
                listitem.setLabel(group.getName());
                listitem.setValue(group);
                listitem.addEventListener(Events.ON_CLICK, showGroupusers);
            });
            
           
            List<User> allUsers = dbGroupService.getAllUser();
            userModel.setMultiple(true);
            userModel.addAll(allUsers);
            userListbox.setModel(userModel);
            userListbox.setItemRenderer((listitem,userr,index) ->{
                User user =(User)userr;
                listitem.setLabel(user.getFullName());
                listitem.setValue(user);
            });
        }
    }

    @Listen("onClick = #changePwdBtn")
    public void onChangePassword(){
        if(validatePassword()){
            User user = new User();
            user.setId(authenticationService.getUserCredential().getUserId());
            user.setPassword(newPwd.getText().trim());
            try {
                userService.resetPassword(user);
                Clients.showNotification(
                        Labels.getLabel("pwdResetSuccess"),
                        Clients.NOTIFICATION_TYPE_INFO, pwdPanel, Constants.POSITION_CENTER, 3000, true);
            } catch (EncryptDecryptException e) {
                LOG.debug(Constants.EXCEPTION,e);
                Clients.showNotification(
                        Labels.getLabel("pwdResetFailed"),
                        Clients.NOTIFICATION_TYPE_ERROR, pwdPanel, Constants.POSITION_CENTER, 3000, true);
                return;
            }
        }
    }

    private boolean validatePassword() {
        if(oldPwd.getText().isEmpty()){
            Clients.showNotification(
                    Labels.getLabel("provideValidPassword"),
                    Clients.NOTIFICATION_TYPE_ERROR, oldPwd, Constants.POSITION_END_CENTER, 3000, true);
            return false;
        }else if(newPwd.getText().isEmpty()){
            Clients.showNotification(
                    Labels.getLabel("provideValidPassword"),
                    Clients.NOTIFICATION_TYPE_ERROR, newPwd, Constants.POSITION_END_CENTER, 3000, true);
            return false;
        }else if(confirmPwd.getText().isEmpty()){
            Clients.showNotification(
                    Labels.getLabel("reEnterNewPassword"),
                    Clients.NOTIFICATION_TYPE_ERROR, confirmPwd, Constants.POSITION_END_CENTER, 3000, true);
            return false;
        }else {
            //validates old password
            User user = authenticationService.authenticate(authenticationService.getUserCredential().getUserId(), oldPwd.getText().trim());
            if(user == null){
                Clients.showNotification(
                        Labels.getLabel("provideValidPassword"),
                        Clients.NOTIFICATION_TYPE_ERROR, oldPwd, Constants.POSITION_END_CENTER, 3000, true);
                return false;
            }else if(!newPwd.getText().equals(confirmPwd.getText())){
                 Clients.showNotification(
                         Labels.getLabel("passwordsNotMatch"),
                         Clients.NOTIFICATION_TYPE_ERROR, pwdPanel, Constants.POSITION_CENTER, 3000, true);
                 return false;
             }
        }
       
        return true;
    }
    
    @Listen("onClick = #adduserBtn")
    public void onAddUser(){
       Group selectedGroup = new ArrayList<Group>(groupModel.getSelection()).get(0);
      
       List<User> selectedUsers = new ArrayList<User>();
        
        userListbox.getSelectedItems().stream().forEach(item ->{
            selectedUsers.add(item.getValue());
        });
        
        if(!selectedUsers.isEmpty()){
            Collection<User> uniqueUsers = CollectionUtils.subtract(selectedUsers, groupUserModel);
            groupUserModel.addAll(uniqueUsers);
            dbGroupService.addUser(new HashSet<User>(uniqueUsers),selectedGroup);
        }else{
            Clients.showNotification(
                    Labels.getLabel("noUserselected"),
                    Clients.NOTIFICATION_TYPE_WARNING, userListbox, Constants.POSITION_CENTER, 3000, true);
            return;
        }
    }
    
    @Listen("onClick = #addGroupBtn")
    public void addGroup(){
         if(groupname.getText().isEmpty()){
            Clients.showNotification(
                    Labels.getLabel("groupNameEmpty"),
                    Clients.NOTIFICATION_TYPE_WARNING, groupname, Constants.POSITION_END_CENTER, 3000, true);
            return;
        }else{
            Group newGroup = new Group();
            newGroup.setCode(groupname.getText().trim());
            newGroup.setName(groupname.getText().trim());
            dbGroupService.addgroup(newGroup);
            groupModel.add(newGroup);
            groupname.setText("");
        }
    }
}
