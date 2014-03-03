package org.hpccsystems.dashboard.services;

import org.hpccsystems.dashboard.entity.User;

/**
 * UserInfoService is used to find the update the Dashboard user's.
 *
 */
public interface UserInfoService  {

	/** find user by account **/
	 User findUser(String account);
	
	/** update user **/
	 User updateUser(User user);
}
