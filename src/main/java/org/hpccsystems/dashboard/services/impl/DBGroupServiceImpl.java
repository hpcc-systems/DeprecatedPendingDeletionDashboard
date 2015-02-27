package org.hpccsystems.dashboard.services.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.dao.GroupDao;
import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.ConditionalGroupService;
import org.hpccsystems.dashboard.services.DBGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

public class DBGroupServiceImpl implements ConditionalGroupService,DBGroupService{
    
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

    @Override
    public List<User> getGroupUsers(Group selectdGroup) {
        return groupDao.getGroupUsers(selectdGroup);
    }

    @Override
    public List<User> getAllUser() {
        return groupDao.getAllUser();
    }
    @Override
    public void addUser(Set<User> selectedUsers,Group group) {
        groupDao.addUser(selectedUsers,group);
    }
    @Override
    public void addgroup(Group newGroup) {
         groupDao.addgroup(newGroup);
    }

    public void removeUser(Group selectedGroup, User user) {
        groupDao.removeUser(selectedGroup,user);
        
    }

}
