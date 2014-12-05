package org.hpccsystems.dashboard.service;

public interface AuthenticationService {
	boolean authenticate(String userId, String pasword);
}
