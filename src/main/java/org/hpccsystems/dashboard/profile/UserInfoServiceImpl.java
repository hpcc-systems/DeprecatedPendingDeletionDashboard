package org.hpccsystems.dashboard.profile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.services.UserInfoService;

/**
 * UserInfoServiceImpl is implementation class for UserInfoService.
 *
 */
public class UserInfoServiceImpl implements UserInfoService,Serializable{
    private static final long serialVersionUID = 1L;
    
    
    static protected List<User> userList = new ArrayList<User>();  
    static{
        userList.add(new User("anonymous","1234","Anonymous","anonumous@your.com"));
        userList.add(new User("admin","1234","Admin","admin@your.com"));
        userList.add(new User("zkoss","1234","ZKOSS","info@zkoss.org"));
    }
    
    /** synchronized is just because we use static userList 
     * in this demo to prevent concurrent access **/
    public User findUser(final String account){
        synchronized(this){
        for(int i=0;i<userList.size();i++){
            final User usr = userList.get(i);
            if(account.equals(usr.getAccount())){
                return User.clone(usr);
            }
          }
        }    
        return null;
    }
    
     /** synchronized is just because we use static userList 
      *   in this demo to prevent concurrent access
     **/
    public User updateUser(final User user){
        synchronized(this){
        User usr=null;
        for(int i=0;i<userList.size() ;i++){
            usr = userList.get(i);
            if(usr.getAccount().equals(usr.getAccount())){
                usr = User.clone(usr);
                userList.set(i,usr);
                return usr;
            }
         }
        }    
        throw new RuntimeException("user not found "+user.getAccount());
    }
}
