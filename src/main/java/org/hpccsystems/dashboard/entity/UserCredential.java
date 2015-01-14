package org.hpccsystems.dashboard.entity;

import java.util.HashSet;
import java.util.Set;

public class UserCredential {
	
	private String applicationId;
	private String id;
	private String name;
	private Set<String> roles;
	
	
	public void addRole(String role) {
		if(roles == null) {
			roles = new HashSet<String>();
		}
		roles.add(role);
	}
	
	public boolean hasRole(String role) {
		if(roles == null) {
			return false;
		}
		return roles.contains(role);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
}
