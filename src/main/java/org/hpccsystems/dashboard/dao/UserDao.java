package org.hpccsystems.dashboard.dao;

import java.util.List;

import org.hpccsystems.dashboard.entity.Application;

public interface UserDao {
	List<Application> getAllApplications();
}
