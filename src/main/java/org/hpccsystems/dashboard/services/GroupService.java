package org.hpccsystems.dashboard.services;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.hpccsystems.dashboard.entity.Group;
import org.springframework.dao.DataAccessException;

public interface GroupService {
    
    /** inserts dashboard group details into acl_public table.
     * @param dashboardId
     * @param group
     * @throws DataAccessException
     */
    void addGroup(final int dashboardId, final Group group)throws DataAccessException;
    
    /** deletes existing dashboard groups from acl_public table.
     * @param dashboardId
     * @param group
     * @throws DataAccessException
     */
    void deleteGroup(final int dashboardId, final Group group)throws DataAccessException;

    List<Group> getGroups(Integer dashboardId) throws DataAccessException, DataAccessException, SQLException;
    
    /**
     * return the list of groupId of the user
     * @param userId
     * @return List<Integer>
     * @throws Exception
     */
    List<String> getGroupCodes(String userId) throws DataAccessException, RemoteException, ServiceException;
    
    /** update group roles into acl_public table.
     * @param dashboardId
     * @param role
     * @throws DataAccessException
     */
    void updateGroupRole(final int dashboardId, final Group group)throws DataAccessException;

}
