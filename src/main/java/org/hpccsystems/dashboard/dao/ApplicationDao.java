package org.hpccsystems.dashboard.dao;

import java.util.List;

import org.hpccsystems.dashboard.entity.Application;
import org.springframework.dao.DataAccessException;

/**
 * Dao interface,has abstract methods for Application related DB hits
 *
 */
public interface ApplicationDao {
	
	List<Application> retrieveApplicationIds() throws DataAccessException;

}
