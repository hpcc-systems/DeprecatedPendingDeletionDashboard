package org.hpccsystems.dashboard.services;

import java.util.List;
import java.util.Set;

import org.hpccsystems.dashboard.entity.Group;
import org.hpccsystems.dashboard.entity.User;

public interface DBGroupService {

    public List<User> getGroupUsers(Group selectdGroup);
    public List<User> getAllUser();
    public void addUser(Set<User> selectedUsers,Group group);
    public void addgroup(Group newGroup);
    public void removeUser(Group selectedGroup, User user);
}
