package org.hpccsystems.dashboard.profile;

import java.util.Set;

import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.CommonInfoService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.hpccsystems.dashboard.services.UserInfoService;
import org.hpccsystems.dashboard.services.impl.AuthenticationServiceImpl;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;

/**
 * ProfileViewController is used to create a profile for Dashboard user 
 *  and controller class for profile_mvc.zul.
 *
 */
public class ProfileViewController extends SelectorComposer<Component>{
	private static final long serialVersionUID = 1L;

	//wire components
	@Wire
	Label account;
	@Wire
	Textbox fullName;
	@Wire
	Textbox email;
	@Wire
	Datebox birthday;
	@Wire
	Listbox country;
	@Wire
	Textbox bio;
	@Wire
	Label nameLabel;
	
	//services
	AuthenticationService authService = new AuthenticationServiceImpl();
	UserInfoService userInfoService = new UserInfoServiceImpl();
	
	@Override
	public void doAfterCompose(final Component comp) throws Exception{
		super.doAfterCompose(comp);
		
		final ListModelList<String> countryModel = new ListModelList<String>(CommonInfoService.getCountryList());
		country.setModel(countryModel);
		
		refreshProfileView();
	}
	
	
	@Listen("onClick=#saveProfile")
	public void doSaveProfile(){
		final UserCredential cre = authService.getUserCredential();
		final User user = userInfoService.findUser(cre.getAccount());
		if(user==null){
			return;
		}
		
		//apply component value to bean
		user.setFullName(fullName.getValue());
		user.setEmail(email.getValue());
		user.setBirthday(birthday.getValue());
		user.setBio(bio.getValue());
		
		final Set<String> selection = ((ListModelList)country.getModel()).getSelection();
		if(selection.isEmpty()){
			user.setCountry(null);
		}else{
			user.setCountry(selection.iterator().next());
		}
		
		nameLabel.setValue(fullName.getValue());
		
		userInfoService.updateUser(user);
		
		Clients.showNotification("Your profile is updated");
	}
	
	@Listen("onClick=#reloadProfile")
	public void doReloadProfile(){
		refreshProfileView();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void refreshProfileView() {
		final UserCredential cre = authService.getUserCredential();
		final User user = userInfoService.findUser(cre.getAccount());
		if(user==null){
			return;
		}
		//apply bean value to UI components
		account.setValue(user.getAccount());
		fullName.setValue(user.getFullName());
		email.setValue(user.getEmail());
		birthday.setValue(user.getBirthday());
		bio.setValue(user.getBio());
		((ListModelList)country.getModel()).addToSelection(user.getCountry());
		nameLabel.setValue(user.getFullName());
	}
}
