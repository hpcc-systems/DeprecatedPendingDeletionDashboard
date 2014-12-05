package org.hpccsystems.dashboard.services.impl;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.dao.GroupDao;
import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.services.ConditionalGroupService;
import org.hpccsystems.dashboard.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

public class GroupServiceImpl implements GroupService {
    private static final Log LOG = LogFactory.getLog(GroupServiceImpl.class);

    private GroupDao groupDao;    
    private ConditionalGroupService  conditionalGroupService; 
    
    @Autowired
    public void setConditionalGroupService(ConditionalGroupService  conditionalGroupService) {
        this.conditionalGroupService = conditionalGroupService;
    }

    @Autowired
    public void setGroupDao(final GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public void addGroup(final int dashboardId, final Group group) {
        try {
            groupDao.addGroup(dashboardId, group);
        } catch (DataAccessException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
    }

    public void deleteGroup(final int dashboardId, final Group group) {
        try {
            groupDao.deleteGroup(dashboardId, group);
        } catch (DataAccessException ex) {
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
    }

    @Override
    public List<String> getGroupCodes(String userId)  throws DataAccessException, RemoteException, ServiceException {        
        List<String> groupCodes = new ArrayList<String>();        
        for (Group group : conditionalGroupService.getGroups(userId)) {
            groupCodes.add(group.getCode());
        }
        
        return groupCodes;
    }

    @Override
    public List<Group> getGroups(Integer dashboardId) throws DataAccessException, SQLException {
        return groupDao.selectGroups(dashboardId);
    }
    
    @Override
    public void updateGroupRole(final int dashboardId, final Group group)throws DataAccessException{
        try{
            groupDao.updateGroupRole(dashboardId,group);
        }catch(DataAccessException ex){
            LOG.error(Constants.EXCEPTION, ex);
            throw ex;
        }
    }
}
