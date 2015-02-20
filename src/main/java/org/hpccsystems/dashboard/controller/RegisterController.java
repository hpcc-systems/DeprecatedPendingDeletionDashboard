package org.hpccsystems.dashboard.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.hpccsystems.dashboard.services.UserService;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
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
    private static final  Log LOG = LogFactory.getLog(RegisterController.class);
    
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
            showValidationError(Labels.getLabel("provideValidPassword"), password);
            return;
        }
        if(!password.getText().equals(confirmPassword.getText())) {
            showValidationError(Labels.getLabel("passwordsNotMatch"), confirmPassword);
            return;
        }
        if(userService.userExist(userid.getText())) {
            showValidationError(Labels.getLabel("userExists"), userid);
            return;
        }
        
        User user = new User();
        user.setAccount(userid.getText());
        user.setPassword(password.getText());
        user.setFirstName(fname.getText());
        user.setLastName(lname.getText());
        
        boolean isRegistered = false;
        try {
            isRegistered = userService.addUser(user);
        } catch (EncryptDecryptException e) {
            Clients.showNotification(Labels.getLabel("registrationFailed"), Clients.NOTIFICATION_TYPE_ERROR, getSelf(), "middle_center", 0, true);
        }
        
        if(isRegistered) {
            userid.setText(null);
            fname.setText(null);
            lname.setText(null);
            password.setText(null);
            confirmPassword.setText(null);
            
            //Redirects to login page
            Executions.sendRedirect("/login.zhtml");
        } else {
            Clients.showNotification(Labels.getLabel("registrationFailed"), Clients.NOTIFICATION_TYPE_ERROR, getSelf(), "middle_center", 0, true);
        }
    }

    private void showValidationError(String message, Component component) {
        Clients.showNotification(message, Clients.NOTIFICATION_TYPE_ERROR, component, "end_center", 3000, true);
    }
}
