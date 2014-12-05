package org.hpccsystems.dashboard.services.impl;

import org.hpccsystems.dashboard.services.ConditionalGroupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to return ConditionalGroupService bean/object to manage groups
 * based on LDAP/normal authentication
 *
 */
@Configuration 
public class GroupConfiguration  {
    
    @Value("${enableLDAP}")
    private String enableLDAP;
    
    @Bean(name="conditionalGroupService")
    public ConditionalGroupService  conditionalGroupService() {
        if("true".equals(enableLDAP)){
            return new MBSServiceImpl();
        }
        return new DBGroupServiceImpl();
    }

}
