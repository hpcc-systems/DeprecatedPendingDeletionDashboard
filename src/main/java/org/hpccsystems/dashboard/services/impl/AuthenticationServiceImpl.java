package org.hpccsystems.dashboard.services.impl; 

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.dao.AuthenticationDao;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.AuthenticationService;
import org.hpccsystems.dashboard.services.GroupService;
import org.hpccsystems.dashboard.services.UserCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;

/**
 * AuthenticationServiceImpl is implementation class for AuthenticationService.
 *
 */
@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AuthenticationServiceImpl implements AuthenticationService,Serializable{
    private static final long serialVersionUID = 1L;
    
    private static final  Log LOG = LogFactory.getLog(AuthenticationServiceImpl.class); 
    
    AuthenticationDao authendicationDao;
    
    GroupService groupService;
    
    AuthenticationService  authenticationService;
    
    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }
    @Autowired
    public void setAuthendicationDao(AuthenticationDao authendicationDao) {
        this.authendicationDao = authendicationDao;
    }
    
    public UserCredential getUserCredential(){
        final Session sess = Sessions.getCurrent();
        
        UserCredential cre = (UserCredential)sess.getAttribute(Constants.USER_CREDENTIAL);
        if(cre==null){
            //new a anonymous user and set to session
            cre = new UserCredential();
            sess.setAttribute("userCredential",cre);
        }
        return cre;
    }
    
    public User authenticate(final String account, final String passWord) {
        
        User user = null;
        try {
            user = authendicationDao.authendicateUser(account,passWord);
        } catch (SQLException e) {
            LOG.error(Constants.EXCEPTION, e);
        }
        
        if(LOG.isDebugEnabled()){
            LOG.debug("After authenticating.. User Object - " + user);
        }
        return user;
    }

    public void logout(Object object)throws Exception {
        Sessions.getCurrent().invalidate();
        if(getUserCredential().hasRole(Constants.CIRCUIT_ROLE_VIEW_DASHBOARD)){
             Clients.evalJavaScript("window.open('','_self',''); window.close();");    
        }
        if(object!= null) {                
            try {
                authendicationDao.updateActiveFlag((User)object);
            } catch (final SQLException e) {
                throw e;
            }        
        }
    }

    @Override
    public boolean login(User user,String appId)  throws DataAccessException, RemoteException, ServiceException {
        boolean isLoginSuccessful = false;
        UserCredential credential = null;
        if(user != null){
            isLoginSuccessful = user.isValidUser();
            
            Boolean isSuperUser = false;
            try {
                List<String> groupCodes =groupService.getGroupCodes(user.getUserId());
                //Checking Super user
                isSuperUser = checkSuperUser(groupCodes,user.getUserId());    
            } catch (Exception e) {
                LOG.error(Constants.EXCEPTION, e);                
            } finally {
                credential = new UserCredential(user.getUserId(), user.getFullName(),appId, isSuperUser);
            }
            
            Sessions.getCurrent().setAttribute("userCredential",credential);
        }
        LOG.debug("User authenticated result .." + isLoginSuccessful);
        LOG.debug("User autheticated as SU - " + credential.isSuperUser());
        return isLoginSuccessful;
    }
    
    /**
     * Method checks whether the logged in person is Super user or not.
     * @param groupCodes
     * @param userId
     * @return boolean
     */
    private boolean checkSuperUser(List<String> groupCodes,String userId ) {
        
        boolean isSuperUser = false;    
        //Super user from Mbs service
        if("true".equals(Labels.getLabel("enableLDAP"))){
            if(groupCodes.contains(Constants.SUPER_USER_GROUP_CODE)){
                isSuperUser = true;
            }else{
                isSuperUser = false;
            }
        //Super user from DB
        }else{
            if("admin".equals(userId)) {
                isSuperUser = true;
            } else {
                isSuperUser = false;
            }
        }
        return isSuperUser;
    }
        
}
