package org.hpccsystems.dashboard.service;

import java.util.List;

import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.UserCredential;

public interface AuthenticationService {
	
	List<Application> getAllApplications();
	
	boolean authenticate(String userId, String password);
	
	void setUserCredential(UserCredential userCredential);
	
	UserCredential getUserCredential();
}
