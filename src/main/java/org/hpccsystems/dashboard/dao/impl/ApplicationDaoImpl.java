package org.hpccsystems.dashboard.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.ApplicationDao;
import org.hpccsystems.dashboard.entity.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.zkoss.util.resource.Labels;

/**
 * Dao class to do Application related DB hits
 *
 */
public class ApplicationDaoImpl implements ApplicationDao {
    
    private static final Log LOG = LogFactory.getLog(ApplicationDaoImpl.class);
    
    private JdbcTemplate jdbcTemplate;
    
    public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    /**
     * Method to get Applications from DB
     * @return Application
     */
    public List<Application> retrieveApplicationIds()  throws DataAccessException{
        
        String sql = Queries.GET_APPLICATIONS; 
     
        List<Application> applications = new ArrayList<Application>();
         
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList(sql);
        for (Map<String,Object> row : rows) {
            Application application = new Application();
            application.setAppId(row.get("dash_app_id").toString());
            application.setAppName(row.get("dash_app_name").toString());
            applications.add(application);
        }
        
        if(LOG.isDebugEnabled()){
            LOG.debug(Labels.getLabel("selectedApplicationList") + applications);
        }
        
        return applications;

    }
}
