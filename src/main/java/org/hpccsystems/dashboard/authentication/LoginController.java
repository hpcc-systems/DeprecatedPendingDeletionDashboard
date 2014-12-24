package org.hpccsystems.dashboard.authentication;

import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.UserCredential;
import org.hpccsystems.dashboard.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class LoginController extends SelectorComposer<Component>{

	private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

	@Wire
	private Listbox apps;
	
	@Wire
	private Textbox account;
	@Wire
	private Textbox password;
	@Wire
	private Label message;
	
	@WireVariable
	private AuthenticationService authenticationService;
	
	private ListModelList<Application> applications;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Auth Service - {}", authenticationService);
			LOGGER.debug("Appls list UI - {}", comp.getFirstChild().getClass());
			LOGGER.debug("Appls list UI - {}", apps);
		}
		
		applications =  new ListModelList<Application>(authenticationService.getAllApplications());
		apps.setModel(applications);
	}
	
	@Listen("onClick=#login; onOK=#loginWin")
    public void doLogin() {

        final String accountString = account.getText();
        final String passwordString = password.getText();
        
        // Validations
        if(apps.getSelectedItem() == null){
            message.setValue(Labels.getLabel("selectAppln"));
            return;
        }else if(accountString == null || passwordString== null || accountString.isEmpty() || passwordString.isEmpty()){
            message.setValue(Labels.getLabel("enterCredential"));
            return;
        }	
        
        Boolean isAuthenticated = authenticationService.authenticate(accountString, passwordString);
        
        if (isAuthenticated) {            
            message.setValue(Labels.getLabel("validUser") +" "+ accountString);
            UserCredential userCredential = new UserCredential();
            userCredential.setApplicationId(applications.getSelection().iterator().next().getId());
            userCredential.setId(accountString);
            userCredential.setName(accountString);
            authenticationService.setUserCredential(userCredential);
        } else {
            message.setValue(Labels.getLabel("invalidUser"));
            return;
        }

        LOGGER.info("Logged in {} Sucessfully", accountString);
        Executions.sendRedirect("/");
    }
}
