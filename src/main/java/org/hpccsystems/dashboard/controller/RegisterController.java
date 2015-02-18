package org.hpccsystems.dashboard.controller;

import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.UserService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Textbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class RegisterController  extends SelectorComposer<Component>{
    private static final long serialVersionUID = 1L;
    
    @Wire
    private Textbox fname;
    @Wire
    private Textbox lname;
    @Wire
    private Textbox userid;
    @Wire
    private Textbox password;
    @Wire
    private Textbox confirmPassword;
    
    @WireVariable
    UserService userService;
    
    @Listen("onClick = #register")
    public void register() {
        //Validations
        if(password.getText().isEmpty()) {
            shoeFieldValidation("Provide a valid password", password);
            return;
        }
        if(password.getText().equals(confirmPassword.getText())) {
            shoeFieldValidation("Passwords doesn't match", confirmPassword);
            return;
        }
        if(userService.userExist(userid.getText())) {
            shoeFieldValidation("User already exsists", userid);
        }
        
        User user = new User();
        user.setAccount(userid.getText());
        user.setPassword(password.getText());
        user.setFirstName(fname.getText());
        user.setLastName(lname.getText());
        
        if(!userService.addUser(user)) {
            Clients.showNotification("Registration failed", Clients.NOTIFICATION_TYPE_ERROR, getSelf(), "middle_center", 0, true);
        }
    }

    private void shoeFieldValidation(String message, Component component) {
        Clients.showNotification(message, Clients.NOTIFICATION_TYPE_ERROR, component, "end_center", 3000, true);
    }
}
