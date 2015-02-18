package org.hpccsystems.dashboard.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.controller.DashboardController;
import org.hpccsystems.dashboard.entity.User;
import org.hpccsystems.dashboard.exception.EncryptDecryptException;
import org.hpccsystems.dashboard.util.EncryptDecrypt;
import org.springframework.jdbc.core.RowMapper;

/**
 * Class to get mapped the User details from DB to User Object
 * @author 
 *
 */
public class UserRowMapper implements RowMapper<User> {
    private static final  Log LOG = LogFactory.getLog(UserRowMapper.class); 
    StringBuilder name = null;
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        name = new StringBuilder();
        name.append(StringUtils.capitalize(rs.getString("first_name")))
                .append(" ")
                .append(StringUtils.capitalize(rs.getString("last_name")));
        user.setFullName(name.toString());
        user.setPassword(decryptPassword(rs.getString("password")));
        user.setActiveFlag(rs.getString("active_flag"));
        return user;
    }
    
    private String decryptPassword(String encryptedPwd) {
        EncryptDecrypt decrypter = null;
            try {
                decrypter = new EncryptDecrypt("");
                //decrypt password
                return decrypter.decrypt(encryptedPwd);
            } catch (EncryptDecryptException e) {
                LOG.debug(Constants.EXCEPTION,e);
            }
          return null;
    }


    

    
}
