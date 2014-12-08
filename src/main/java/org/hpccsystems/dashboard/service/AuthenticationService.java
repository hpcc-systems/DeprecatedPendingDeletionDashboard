package org.hpccsystems.dashboard.service;

import java.util.List;

import org.hpccsystems.dashboard.entity.Application;

public interface AuthenticationService {
	
	List<Application> getAllApplications();
	
	boolean authenticate(String userId, String pasword);
}
