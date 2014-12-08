package org.hpccsystems.dashboard.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hpccsystems.dashboard.dao.UserDao;
import org.hpccsystems.dashboard.entity.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("userDao")
public class UserDaoImpl implements UserDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class); 
    
    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Autowired
    public void setDataSourceToJdbcTemplate(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
	@Override
	public List<Application> getAllApplications() {
        List<Application> applications = new ArrayList<Application>();
        
        List<Map<String, Object>> rows = getJdbcTemplate().queryForList("SELECT dash_app_id,dash_app_name FROM dash_application");
        for (Map<String,Object> row : rows) {
            Application application = new Application(
            		row.get("dash_app_id").toString(), 
            		row.get("dash_app_name").toString());
            applications.add(application);
        }
        
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Applications - {}", applications);
        }
        
        return applications;
	}

}
