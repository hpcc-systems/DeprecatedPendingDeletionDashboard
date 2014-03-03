package org.hpccsystems.dashboard.services;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is model for UserCredential.
 *
 */
public class UserCredential  implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String applicationId;
	
	private String userId;
	private String userName;
	
	Set<String> roles = new HashSet<String>();

	public UserCredential(final String userId, final String userName, final String applicationId) {
		this.userId = userId;
		this.userName = userName;
		this.setApplicationId(applicationId);
	}
	
	public UserCredential() {
		this.userId = "anonymous";
		this.userName = "Anonymous";
		roles.add("anonymous");
	}

	public boolean isAnonymous() {
		return hasRole("anonymous") || "anonymous".equals(userId);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(final String userName) {
		this.userName = userName;
	}
	
	public boolean hasRole(final String role){
		return roles.contains(role);
	}
	
	public void addRole(final String role){
		roles.add(role);
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

}