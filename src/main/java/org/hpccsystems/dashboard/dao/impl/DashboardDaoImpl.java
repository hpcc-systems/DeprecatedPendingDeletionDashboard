package org.hpccsystems.dashboard.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.rowmapper.DashboardRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;


@Service("dashboardDao")
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DashboardDaoImpl implements DashboardDao {
    
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setJdbcTemplate(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void insertDashboard(Dashboard dashboard,String userId) {
       
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", dashboard.getName());
        parameters.put("user_id", userId);
        parameters.put("application_id", dashboard.getApplicationId());
        parameters.put("hpcc_id", dashboard.getHpccId());

        Number dashboardId = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
                .withTableName("dashboard")
                .usingGeneratedKeyColumns("id")
                .executeAndReturnKey(parameters);

        dashboard.setId(dashboardId.intValue());
        
    }

    @Override
    public List<Dashboard> getDashboards(String userId, String applicationId) {
        
        String sql="SELECT * FROM dashboard WHERE user_id = ? AND application_id = ?";
        return jdbcTemplate.query(sql, new Object[]{userId,applicationId}, new DashboardRowMapper());
    }
    
    public void deleteDashboard(final Integer dashboardId) throws DataAccessException {
       jdbcTemplate.update("delete from dashboard_details where dashboard_id=?", new Object[] { 
                dashboardId
        });
    }

}
