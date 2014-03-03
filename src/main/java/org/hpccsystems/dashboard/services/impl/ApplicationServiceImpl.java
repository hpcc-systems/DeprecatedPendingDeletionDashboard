package org.hpccsystems.dashboard.services.impl;

import java.util.List;

import org.hpccsystems.dashboard.dao.ApplicationDao;
import org.hpccsystems.dashboard.entity.Application;
import org.hpccsystems.dashboard.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
/**
 * 
 * Service class to define Application related services
 *
 */
@Service("applicationService") 
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ApplicationServiceImpl implements ApplicationService {
	
	
	ApplicationDao applicationDao;

	public ApplicationDao getApplicationDao() {
		return applicationDao;
	}

	@Autowired
	public void setApplicationDao(ApplicationDao applicationDao) {
		this.applicationDao = applicationDao;
	}

	public List<Application> retrieveApplicationIds() throws Exception{
		try
		{
		return applicationDao.retrieveApplicationIds();
		}
		catch(DataAccessException ex)
		{
			throw ex;
		}
	}

}
