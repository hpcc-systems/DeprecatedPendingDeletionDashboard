/* 
    Description:
        ZK Essentials
    History:
        Created by dennis

Copyright (C) 2012 Potix Corporation. All Rights Reserved.
*/
package org.hpccsystems.dashboard.controller;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.ApplicationService;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.ConditionalGroupService;
import org.hpccsystems.dashboard.services.DashboardService;
import org.hpccsystems.dashboard.services.LDAPAuthenticationService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.zkoss.util.Locales;
import org.zkoss.util.resource.Labels;
import org.zkoss.web.Attributes;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;


/**
 * LoginController class is used to handle the login activities for Dashboard project
 *  and controller class for login.zul.
 *
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class LoginController extends SelectorComposer<Component> {
    
    private static final  Log LOG = LogFactory.getLog(LoginController.class);
    
    private static final long serialVersionUID = 1L;
    
    @WireVariable
    private DashboardService dashboardService;
    
    //wire components    
    @Wire
    Listbox language;
    @Wire Listitem listItemEnglish,listItemChinese;
    @Wire
    Textbox account;
    @Wire
    Textbox password;
    @Wire
    Label message;
    @Wire
    Listbox apps;
    
    @Wire
    Button login;
    @WireVariable
    AuthenticationService  authenticationService;
    
    @WireVariable
    LDAPAuthenticationService LDAPService;
    
    @WireVariable
    ApplicationService applicationService;
    
    @WireVariable
    private ConditionalGroupService  conditionalGroupService; 
        
    Map<String, String[]> args = null;
    
    @Override
    public void doAfterCompose(final Component comp) throws Exception {
        super.doAfterCompose(comp);
        args = Executions.getCurrent().getParameterMap();
        //setting default language as English
        Session session = Sessions.getCurrent();
        String lang = (String)session.getAttribute("lang");
        if(lang!=null && "Chinese".equalsIgnoreCase(lang)){
            listItemChinese.setSelected(true);
            listItemChinese.setValue("Chinese");
        }
        else{
            listItemEnglish.setSelected(true);
            listItemEnglish.setValue("English");
        }
        
        //Redirecting if the user is already logged in.
        if(!authenticationService.getUserCredential().isAnonymous()) {
            redirectHome();
            return;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling 'doAfterCompose' in LoginController");
            LOG.debug("dashboardService:loginctrler -->"+dashboardService);
        }
        try    {            
            final List<Application> applicationList = new ArrayList<Application>(applicationService.retrieveApplicationIds());
            final ListModelList<Application> appModel = new ListModelList<Application>(applicationList);
            apps.setModel(appModel);
        } catch(Exception ex) {
            Clients.showNotification(Labels.getLabel("unableToRetrieveApplications"), false);
            LOG.error(Constants.EXCEPTION, ex);
        }
    }


    private void redirectHome() {
        Session session = Sessions.getCurrent();
        if(LOG.isDebugEnabled()) {
            LOG.debug("Request path - " + session.getAttribute(Constants.REQUEST_PATH));
        }
        if(session.getAttribute(Constants.REQUEST_PATH) != null) {
            //When request has a predefined path
            Executions.sendRedirect(session.getAttribute(Constants.REQUEST_PATH).toString());
            session.removeAttribute(Constants.REQUEST_PATH);
        } else {
            Executions.sendRedirect("/demo/");
        }
    }
    
    
    // For Internalization

    @Listen("onSelect=#language")
    public void doSelect() {
        Session session = Sessions.getCurrent();
        String lang = language.getSelectedItem().getValue();
        if ("English".equalsIgnoreCase(lang)) {
            session.setAttribute("lang", "English");
            changeLocale("en");
        } else {
            session.setAttribute("lang", "Chinese");
            changeLocale("zh");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Current Language" + lang);
        }
    }

    private void changeLocale(String locale) {
        Session session = Sessions.getCurrent();
        Locale preferredLocale = Locales.getLocale(locale);
        session.setAttribute(Attributes.PREFERRED_LOCALE, preferredLocale);
        Executions.sendRedirect(null);
    }

    @Listen("onClick=#login")
    public void doLogin() throws IOException {
        Boolean isLoginSuccessful = false;
        User user = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Handling 'doLogin' in LoginController");
        }
        
        final String name = account.getValue();
        final String passWord = password.getValue();
        if(apps.getSelectedItem() == null){
        	message.setValue(Labels.getLabel("selectAppln"));
        	return;
        }else if(name == null || passWord == null || name.isEmpty() || passWord.isEmpty()){
        	message.setValue(Labels.getLabel("enterCredential"));
        	return;
        }
        
        final String applnId = apps.getSelectedItem().getValue().toString();
        final String applnName = apps.getSelectedItem().getLabel().trim();
        Map<String,Boolean> applnAccessMap = null;
        try {
            if("true".equals(Labels.getLabel("enableLDAP"))){
                user = LDAPService.authenticate(name, passWord);
                applnAccessMap = authorizeApplnAccess(name,applnName);    
            }else{
                user = authenticationService.authenticate(name, passWord);                
            }    
            isLoginSuccessful = authenticationService.login(user, applnId);
        }catch (RemoteException | ServiceException e) {
            LOG.error("Exception While authorizing user for the logged in Application"+e);
        }catch (Exception ex) {
            LOG.error(Constants.EXCEPTION, ex);
        }

        if (!isLoginSuccessful) {            
            message.setValue(Labels.getLabel("invalidUser"));
            return;
        } else {
            if ("true".equals(Labels.getLabel("enableLDAP"))) {
                //check whether user has access to view the application
                if (applnAccessMap.get(Constants.APPLN_ACCESS) != null
                        && !applnAccessMap.get(Constants.APPLN_ACCESS)) {
                    message.setValue(Labels.getLabel("notAuthorizedUser") + " "    + name);
                    return;
                }
                checkCreateAccess(applnAccessMap,name);                
            }else{
                message.setValue(Labels.getLabel("validUser") +" "+ name);
                manageCreateAccess();
            }
        }

        LOG.debug("Loged in. sending redirect...");
        redirectHome();
    }

    /**Checks for dashboard application create access
     * @param applnAccessMap
     * @param name
     */
    private void checkCreateAccess(Map<String, Boolean> applnAccessMap, String name) {
        //checks whether the LDAP user has Dashboard create access
        if(applnAccessMap.get(Constants.APPLN_CREATE_ACCESS) != null
                && applnAccessMap.get(Constants.APPLN_CREATE_ACCESS)){            
            manageCreateAccess();
        }        
    }


    /**
     * Adds dashboard create role
     */
    private void manageCreateAccess() {
        UserCredential credential = (UserCredential) Sessions.getCurrent().getAttribute("userCredential");                
        credential.addRole(Constants.DASHBOARD_CREATE_ROLE);                
    }


    /**
     * Verifies whether the user have been authorized for the selected application
     * @param userId
     * @param applnName
     * @return Map<String,Boolean>
     */
    private Map<String,Boolean> authorizeApplnAccess(String userId, String applnName) throws RemoteException,ServiceException{
        boolean hasApplnAccess = false;
        boolean hasApplnCreateAccess = false;
        Map<String,Boolean> applnAccessMap = new HashMap<String,Boolean>();
        
        StringBuilder applnAccessGroupCode = new StringBuilder(applnName);
        applnAccessGroupCode.append("_access");
            StringBuilder applnCreateAccessGroupCode = new StringBuilder(applnName);
            applnCreateAccessGroupCode.append("_create_dashboard");
        try {
            List<Group> groups = conditionalGroupService.getGroups(userId);
            for (Group group : groups) {
                if(applnAccessGroupCode.toString().equalsIgnoreCase(group.getName())){
                    hasApplnAccess = true;
                }
                if(applnCreateAccessGroupCode.toString().equalsIgnoreCase(group.getName())){
                    hasApplnCreateAccess = true;
                }
            }
            applnAccessMap.put(Constants.APPLN_ACCESS,hasApplnAccess);
            applnAccessMap.put(Constants.APPLN_CREATE_ACCESS,hasApplnCreateAccess);
            
        } catch (RemoteException | ServiceException e) {
            LOG.error("Exception While authorizing user for the logged in Application"+e);
            throw e;
        }
        return applnAccessMap;

    }    
    


    @Listen("onSelect = #apps")
    public void getApplicationId(){    
        authenticationService.getUserCredential().setApplicationId(apps.getSelectedItem().getValue().toString());        
    }
    
}
