package org.hpccsystems.dashboard.services.impl;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.LDAPAuthenticationService;
import org.zkoss.util.resource.Labels;

public class LDAPAuthenticationServiceImpl implements LDAPAuthenticationService {
    
    private static final Log LOG = LogFactory.getLog(LDAPAuthenticationServiceImpl.class);

    @Override
    public User authenticate(String username, String credential) throws NamingException {
        
        User user = null; 
        
        DirContext context = null;
        boolean validUser;
       Map<String, String> env = getDirectoryContextEnvironment();
        if(LOG.isDebugEnabled()){
        LOG.debug("DirContext Envi Variable -->"+env);
        }
        try{
        context = new InitialDirContext((Hashtable<String, String>)env);
        } catch (NamingException e) {
              throw e;
          }
        validUser = authenticate(context, env, username, credential);
        if(validUser){
            user =new User();
            user.setUserId(username);
            user.setFullName(username);
            user.setValidUser(true);
        }
        
        if(LOG.isDebugEnabled()){
            LOG.debug("After authenticating.. User Object - " + user);
        }
        return user;
    }
    
     /**
      * Method to Search the LDAP user in JNDIRealm 
     * @param context
     * @param env
     * @param username
     * @param credential
     * @return boolean
     * @throws NamingException 
     */
    private boolean authenticate(DirContext context, Map<String, String> env,
             String username, String credential) throws NamingException {
        
        NamingEnumeration<SearchResult> results = null;
        DirContext ctx = null;
        try {
            SearchControls controls = new SearchControls();
            // Search Entire Subtree
            controls.setSearchScope(SearchControls.SUBTREE_SCOPE); 
             // Sets the maximum number of entries to be returned as a result of the search
            controls.setCountLimit(1);  
            // Sets the time limit of these SearchControls in milliseconds
            controls.setTimeLimit(5000); 
            
            StringBuilder searchBuilder = new StringBuilder();
            searchBuilder.append(Labels.getLabel("searchStringStart")).append(username)
            .append(Labels.getLabel("searchStringEnd"));

            results = context.search(Labels.getLabel("userBase"),searchBuilder.toString(), controls);
            if (results.hasMore()) {

                SearchResult result = (SearchResult) results.next();
                Attributes attrs = result.getAttributes();
                Attribute dnAttr = attrs.get(Labels.getLabel("distinguishedName"));
                String dn = (String) dnAttr.get();

                // User Exists, Validate the Password
                env.put(Context.SECURITY_PRINCIPAL, dn);
                env.put(Context.SECURITY_CREDENTIALS, credential);

                // Exception will be thrown onInvalid case
                ctx = new InitialDirContext((Hashtable<String, String>)env); 
                
                return true;
            } else {
                return false;
            }
            // Invalid Login
        } catch (AuthenticationException | NameNotFoundException e) { 
        	LOG.error(Constants.EXCEPTION,e);
            return false;
            // The base context was not found.
        } catch (SizeLimitExceededException e) {
        	throw e;

        } catch (NamingException e) {
        	throw e;

        } finally {
        	finallyBlock(results,context,ctx);
        }
    }


	/**
	 * Finally block - closes the resources
	 * @param results
	 * @param context
	 * @param ctx
	 */
	private void finallyBlock(NamingEnumeration<SearchResult> results,
			DirContext context, DirContext ctx) {
		if (results != null) {
			try {
				results.close();
			} catch (NamingException e) {
				LOG.error(Constants.EXCEPTION , e);
			}
		}
		if (context != null) {
			try {
				context.close();
			} catch (NamingException e) {
				LOG.error(Constants.EXCEPTION , e);
			}
		}
		if (ctx != null) {
			try {
				ctx.close();
			} catch (NamingException e) {
				LOG.error(Constants.EXCEPTION , e);
			}

		}

	}

	/**
     * Sets the LDAP context properties
     * @return Hashtable<String, String>
     * throws Exception
     */
    protected Map<String, String> getDirectoryContextEnvironment()  {        
    	Map<String, String> env = new Hashtable<String, String>();
        
        env.put(Context.INITIAL_CONTEXT_FACTORY, Labels.getLabel("ContextFactory"));
        env.put(Context.PROVIDER_URL, Labels.getLabel("ldapServerUrl"));

    
        // To get rid of the PartialResultException when using Active Directory
        env.put(Context.REFERRAL, Labels.getLabel("referral"));
    
        // Needed for the Bind (User Authorized to Query the LDAP server) 
        env.put(Context.SECURITY_AUTHENTICATION, Labels.getLabel("secAuthentication"));
        env.put(Context.SECURITY_PRINCIPAL, Labels.getLabel("connectionName"));
        env.put(Context.SECURITY_CREDENTIALS, Labels.getLabel("connectionPassword"));
        
        return env;
        
      }

}
