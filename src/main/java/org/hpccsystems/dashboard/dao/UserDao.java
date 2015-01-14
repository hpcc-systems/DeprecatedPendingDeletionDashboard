package org.hpccsystems.dashboard.dao;

import java.util.List;

import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.entity.UserCredential;

public interface UserDao {

	List<Application> getAllApplications();

	boolean validatePassword(String userId, String password);

	UserCredential getUserCredential(String userId);
}
