package org.hpccsystems.dashboard.dao;

import java.sql.SQLException;
import java.util.List;

import org.hpccsystems.dashboard.entity.Group;
import org.springframework.dao.DataAccessException;

public interface GroupDao {
    
    /** inserts dashboard group details into acl_public table.
     * @param dashboard_id
     * @param group
     * @throws DataAccessException
     */
    void addGroup(final int dashboard_id, final Group group)throws DataAccessException;
    
    /** deletes existing dashboard groups from acl_public table.
     * @param dashboard_id
     * @param group
     * @throws DataAccessException
     */
    void deleteGroup(final int dashboard_id, final Group group)throws DataAccessException;

    /**
     * Retrives Groups based on passed dashboarId
     * @param dashboardId
     * @return
     * @throws SQLException 
     * @throws DataAccessException 
     */
    List<Group> selectGroups(Integer dashboardId) throws DataAccessException, SQLException;
    

    /** update group roles into acl_public table.
     * @param dashboard_id
     * @param role
     * @throws DataAccessException
     */
    void updateGroupRole(final int dashboard_id, final Group group)throws DataAccessException;

    /**
     * @return List<Group>
     * @throws DataAccessException
     */
    List<Group> getGroups()throws DataAccessException ;

    /**
     * @param userId
     * @return List<Group>
     * @throws DataAccessException
     */
    List<Group> getGroups(String userId)throws DataAccessException;
}
