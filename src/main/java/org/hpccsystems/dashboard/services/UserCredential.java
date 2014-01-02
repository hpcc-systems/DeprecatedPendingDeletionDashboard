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
	
	String account;
	String name;
	
	Set<String> roles = new HashSet<String>();

	public UserCredential(final String account, final String name) {
		this.account = account;
		this.name = name;
	}

	public UserCredential() {
		this.account = "anonymous";
		this.name = "Anonymous";
		roles.add("anonymous");
	}

	public boolean isAnonymous() {
		return hasRole("anonymous") || "anonymous".equals(account);
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(final String account) {
		this.account = account;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
	
	public boolean hasRole(final String role){
		return roles.contains(role);
	}
	
	public void addRole(final String role){
		roles.add(role);
	}

}
