package org.hpccsystems.dashboard.services;

import java.util.List;

import org.hpccsystems.dashboard.entity.Application;

/**
 *  Service class,has abstract methods for Application related services
 *
 */
public interface ApplicationService {
	
	/**
	 * Call to get available Apllications
	 * @return Application
	 */
	List<Application> retrieveApplicationIds();

}
