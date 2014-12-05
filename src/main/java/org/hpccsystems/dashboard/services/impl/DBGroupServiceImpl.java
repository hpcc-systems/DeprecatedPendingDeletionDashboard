package org.hpccsystems.dashboard.services.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.dao.GroupDao;
import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.services.ConditionalGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

public class DBGroupServiceImpl implements ConditionalGroupService{
    
    private static final Log LOG = LogFactory.getLog(DBGroupServiceImpl.class);
    private GroupDao groupDao;        
    @Autowired
    public void setGroupDao(final GroupDao groupDao) {
        this.groupDao = groupDao;
    }
    
    @Override
    public List<Group> getGroups() throws DataAccessException {
        try{
            return groupDao.getGroups();
        }catch(DataAccessException ex){
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
    }

    @Override
    public List<Group> getGroups(String userId) throws DataAccessException {    
        try{
            return groupDao.getGroups(userId);
        }catch(DataAccessException ex){
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
        
    }

}
