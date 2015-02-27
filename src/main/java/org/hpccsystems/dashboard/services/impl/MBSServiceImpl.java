package org.hpccsystems.dashboard.services.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.commons.lang.StringUtils;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.ConditionalGroupService;

import com.lexisnexis.billingsystems.GroupRoleResult;
import com.lexisnexis.billingsystems.MBSWSResultGetGroupRoles;
import com.lexisnexis.billingsystems.MBSWSResultGetUserRolesLoginId;
import com.lexisnexis.billingsystems.UserRoleResult;
import com.lexisnexis.billingsystems.WS_riagauth11Locator;
import com.lexisnexis.billingsystems.WS_riagauth11Soap;

public class MBSServiceImpl implements ConditionalGroupService {
    

    @Override
    public List<Group> getGroups() throws ServiceException, RemoteException {
        List<Group> groups = new ArrayList<Group>();
        Group group;

        WS_riagauth11Locator locator = new WS_riagauth11Locator();
        
        WS_riagauth11Soap soap = locator.getWS_riagauth11Soap();
        
        MBSWSResultGetGroupRoles result = soap.getGroupRoles("svc_hpccdashboard@mbs", "asdF123$", "HPCCDASHDEV");
                                                                    
        GroupRoleResult[] resultsArray = result.getRoleData();            
        
        for (GroupRoleResult groupRoleResult : resultsArray) {
            //Ignoring Application access & creat_dashboard groups, Super group
            if (!StringUtils.containsIgnoreCase(groupRoleResult.getName(), "_access")
                    && !StringUtils.containsIgnoreCase(groupRoleResult.getName(), "create_dashboard")
                    && !Constants.SUPER_USER_GROUP_NAME.equalsIgnoreCase(groupRoleResult.getName())){
                group = new Group();
                group.setCode(groupRoleResult.getCode());
                group.setName(groupRoleResult.getName());
                groups.add(group);
            }
        }
        
        return groups;
    }

    @Override
    public List<Group> getGroups(String userId) throws RemoteException, ServiceException {
        
        WS_riagauth11Locator locator = new WS_riagauth11Locator();
        
        WS_riagauth11Soap soap = locator.getWS_riagauth11Soap();
        MBSWSResultGetUserRolesLoginId result = soap.getUserRolesLoginId(
                "svc_hpccdashboard@mbs", "asdF123$", userId + "@risk");
        
        UserRoleResult[] resultsArray = result.getRoleData();
        List<Group> groups = new ArrayList<Group>();
        Group group;
        
        for (UserRoleResult userRoleResult : resultsArray) {
            group = new Group();
            group.setCode(userRoleResult.getCode());
            group.setName(userRoleResult.getName());
            groups.add(group);
        }
        
        return groups;
    }

}
