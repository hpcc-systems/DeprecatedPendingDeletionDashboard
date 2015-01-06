package org.hpccsystems.dashboard.services;

import java.rmi.RemoteException;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.hpccsystems.dashboard.entity.Group;
import org.springframework.dao.DataAccessException;

/**
 * Class to refer either MbsService or DBService
 * to manage groups
 *
 */
public interface ConditionalGroupService {
    
    /**
     * Service to get list of Dashboard groups from MBS
     * @return
     * @throws ServiceException
     * @throws RemoteException
     */
    public List<Group> getGroups() throws DataAccessException, ServiceException, RemoteException ;
    /**
     * Service to get list of Dashboard groups from MBS for an user
     * @return List<Group>
     * @throws ServiceException 
     * @throws RemoteException 
     */
    public List<Group> getGroups(String user_id) throws DataAccessException, RemoteException, ServiceException;

}
