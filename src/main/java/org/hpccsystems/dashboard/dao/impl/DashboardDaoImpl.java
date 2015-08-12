package org.hpccsystems.dashboard.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.DashboardDao;
import org.hpccsystems.dashboard.entity.Dashboard;
import org.hpccsystems.dashboard.rowmapper.DashboardRowMapper;
import org.hpccsystems.dashboard.services.UserCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * DashboardDaoImpl is implementation class for DashboardDao.
 *
 */
public class DashboardDaoImpl implements DashboardDao {

    private static final Log LOG = LogFactory.getLog(DashboardDaoImpl.class);

    private JdbcTemplate jdbcTemplate;

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Fetching DashboardMenuPages details from dashboard_details table.
     * 
     * @param application
     * @return List<DashboardMenu>
     * @throws DataAccessException
     */
    public List<Dashboard> fetchDashboardDetails(final String applicationId,
            final String userId, List<String> dashboardIdList,
            final String sourceId) throws DataAccessException {
        List<Dashboard> dashboardList = null;
        StringBuilder sqlBuffer = new StringBuilder();

        sqlBuffer.append(Queries.RETRIEVE_DASHBOARD_DETAILS).append("'")
                .append(applicationId).append("'").append("and user_id='")
                .append(userId).append("'");
        // Regular Dashboard flow
        if (sourceId == null && dashboardIdList == null) {
            sqlBuffer.append(" order by sequence");
        } else if (sourceId != null && dashboardIdList == null) {
            // API flow- for single dashboard
            sqlBuffer.append(" and source_id = '").append(sourceId).append("'");
        } else if (dashboardIdList != null) {
            // API flow- for list of dashboards
            sqlBuffer.append(Queries.DASHBOARD_IN_CLAUSE).append("( '");
            int count = 1;
            for (String dashboardId : dashboardIdList) {
                sqlBuffer.append(dashboardId).append("'");
                if (count != dashboardIdList.size()) {
                    sqlBuffer.append(",'");
                }
                count++;
            }
            sqlBuffer.append(")").append(" order by last_updated_date desc");
        }

        LOG.info("retrieveDashboardDetails() Query -->" + sqlBuffer);
        dashboardList = getJdbcTemplate().query(sqlBuffer.toString(),
                new DashboardRowMapper());
        return dashboardList;
    }

    public void addDashboardDetails(final Dashboard dashboard,final String applicationId, final String sourceId,final String userId)
            throws DataAccessException {
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dashboard_name", dashboard.getName());
        parameters.put("user_id", userId);
        parameters.put("application_Id", applicationId);
        parameters.put("last_updated_date", dashboard.getLastupdatedDate());
        parameters.put("column_count", dashboard.getColumnCount());
        parameters.put("sequence", dashboard.getSequence());
        parameters.put("source_id", dashboard.getSourceId());
        parameters.put("visibility", dashboard.getVisibility());
        parameters.put("common_filter", dashboard.getHasCommonFilter());
        parameters.put("show_localfilter", dashboard.showLocalFilter());
        parameters.put("lock_charttitle", dashboard.lockChartTitle());
        
        Number newId = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
                        .withTableName("dashboard_details")
                        .usingGeneratedKeyColumns("dashboard_id")
                        .executeAndReturnKey(parameters);
        
        dashboard.setDashboardId(newId.intValue());
        
    }

    public int deleteDashboard(final Integer dashboardId, final UserCredential user)
            throws DataAccessException {
        int rowsdeleted = 0;
        rowsdeleted = getJdbcTemplate().update(
                Queries.DELETE_DASHBOARD_WIDGETS, new Object[] { dashboardId });

        rowsdeleted = getJdbcTemplate().update(Queries.DELETE_ACL_PUBLIC,
                new Object[] { dashboardId });
        
        // TODO : need to disable this userId null check, when circuit passes the user details
        if(user == null) {
            rowsdeleted = getJdbcTemplate().update(
                    Queries.API_DELETE_DASHBOARD, new Object[] { dashboardId
                            
                    });
        } else if(user.isSuperUser()){
            rowsdeleted = getJdbcTemplate().update(Queries.DELETE_DASHBOARD_ADMIN,
                    new Object[] { dashboardId});
        } else {
            String userId = user.getUserId();
            rowsdeleted = getJdbcTemplate().update(Queries.DELETE_DASHBOARD,
                    new Object[] { dashboardId, userId });
        }
        
        return rowsdeleted;
    }

    public void updateSidebarDetails(final List<Integer> dashboardIds)
            throws DataAccessException {
        String sql = Queries.UPDATE_SIDEBAR_DETAILS;
        getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement statement, int i)
                    throws SQLException {
                Integer dashboardId = dashboardIds.get(i);
                statement.setInt(1, i);
                statement.setInt(2, dashboardId);
            }

            public int getBatchSize() {
                return dashboardIds.size();
            }
        });
    }

    @Override
    public void updateDashboard(final Dashboard dashboard)
            throws DataAccessException {
        // updates dashboard Sequence & Column count,Name
        getJdbcTemplate().update(
                Queries.UPDATE_DASHBOARD,
                new Object[] { dashboard.getName(), dashboard.getColumnCount(),
                        dashboard.getSourceId(),
                        dashboard.getLastupdatedDate(),
                        dashboard.getVisibility(),
                        dashboard.getHasCommonFilter(),
                        dashboard.showLocalFilter(),
                        dashboard.isLockCommonFilter(),
                        dashboard.isLockaddCommonFilter(),
                        dashboard.lockChartTitle(),
                        dashboard.getDashboardId()
                });
    }

    @Override
    public List<Dashboard> retrieveDashboards(String applicationId,
            String userId, List<String> groupCodes) {
        Set<Dashboard> finalDashboards = new LinkedHashSet<Dashboard>();
        List<Dashboard> dashboardList = new ArrayList<Dashboard>();

        // fetches private dashboards
        dashboardList = getJdbcTemplate().query(Queries.GET_PRIVATE_DASHBOARDS,
                new DashboardRowMapper(),
                new Object[] { userId, applicationId });
        dashboardList = setRole(dashboardList, Constants.ROLE_ADMIN);

        finalDashboards.addAll(dashboardList);
        dashboardList.clear();

        if (groupCodes != null && !groupCodes.isEmpty()) {

            dashboardList = getAdminDashboard(groupCodes, dashboardList);
            finalDashboards.addAll(dashboardList);
            dashboardList.clear();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Admin boards --> " + dashboardList);
            }

            dashboardList = getContributorDashboard(groupCodes, dashboardList);
            finalDashboards.addAll(dashboardList);
            dashboardList.clear();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Contributor boards --> " + dashboardList);
            }

            dashboardList = getConsumerDashboard(groupCodes, dashboardList);
            finalDashboards.addAll(dashboardList);
            dashboardList.clear();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Consumer boards --> " + dashboardList);
            }

        }

        dashboardList.addAll(finalDashboards);
        finalDashboards = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("dashboardList size-->" + dashboardList.size());
        }
        return dashboardList;
    }

    /**
     * Sets the dashboard role as admin
     * 
     * @param dashboardList
     * @return dashboardList
     */
    private List<Dashboard> setRole(List<Dashboard> dashboardList, String role) {
        if (dashboardList != null) {
            for (Dashboard dashboard : dashboardList) {
                dashboard.setRole(role);
            }
        }
        return dashboardList;
    }

    /**
     * fetching dshboard_id which has role 'ADMIN'
     * 
     * @param groupCodes
     * @param dashboardList
     */
    private List<Dashboard> getAdminDashboard(List<String> groupCodes,
            List<Dashboard> dashboardList) {
        StringBuilder sql = new StringBuilder();
        sql.append(Queries.GET_ROLE_BASED_DASHBOARDS).append("( '");
        for (String groupCode : groupCodes) {
            sql.append(groupCode).append("'");
            sql.append(",'");
        }
        // removing last commas
        sql.replace(sql.length() - 2, sql.length(), "");
        sql.append(")").append(" AND a.role = '").append(Constants.ROLE_ADMIN)
                .append("' ").append(" order by d.sequence");

        if (LOG.isDebugEnabled()) {
            LOG.debug("ADMIN SQL -> " + sql.toString());
        }

        return getJdbcTemplate().query(sql.toString(),
                new DashboardRoleRowMapper());
    }

    /**
     * fetching dshboard_id which has role 'CONSUMER'
     * 
     * @param groupCodes
     * @param dashboardList
     */
    private List<Dashboard> getConsumerDashboard(List<String> groupCodes,
            List<Dashboard> dashboardList) {
        StringBuilder sql = new StringBuilder();
        sql.append(Queries.GET_ROLE_BASED_DASHBOARDS).append("( '");
        for (String groupCode : groupCodes) {
            sql.append(groupCode).append("'");
            sql.append(",'");
        }
        // removing last commas
        sql.replace(sql.length() - 2, sql.length(), "");
        sql.append(")").append(" AND a.role = '")
                .append(Constants.ROLE_CONSUMER).append("' ")
                .append(" order by d.sequence");

        if (LOG.isDebugEnabled()) {
            LOG.debug("CONSUMER SQL -> " + sql.toString());
        }

        return getJdbcTemplate().query(sql.toString(),
                new DashboardRoleRowMapper());
    }

    /**
     * fetching dshboard_id which has role 'CONTRIBUTOR'
     * 
     * @param groupCodes
     * @param dashboardList
     */
    private List<Dashboard> getContributorDashboard(List<String> groupCodes,
            List<Dashboard> dashboardList) {
        StringBuilder sql = new StringBuilder();
        sql.append(Queries.GET_ROLE_BASED_DASHBOARDS).append("( '");
        for (String groupCode : groupCodes) {
            sql.append(groupCode).append("'");
            sql.append(",'");
        }
        // removing last commas
        sql.replace(sql.length() - 2, sql.length(), "");
        sql.append(")").append(" AND a.role = '")
                .append(Constants.ROLE_CONTRIBUTOR).append("' ")
                .append(" order by d.sequence");

        if (LOG.isDebugEnabled()) {
            LOG.debug("CONTRIBUTOR SQL -> " + sql.toString());
        }
        dashboardList = getJdbcTemplate().query(sql.toString(),
                new DashboardRoleRowMapper());
        return dashboardList;

    }

    /**
     * Row mapper to get role from acl_public table
     * 
     */
    private class DashboardRoleRowMapper implements RowMapper<Dashboard> {

        @Override
        public Dashboard mapRow(ResultSet rs, int rowNum) throws SQLException {
            Dashboard dashboard = new Dashboard();
            dashboard.setDashboardId(rs.getInt("dashboard_id"));
            dashboard.setApplicationId("application_id");
            dashboard.setName(rs.getString("dashboard_name"));
            dashboard.setColumnCount(rs.getInt("column_count"));
            dashboard.setSourceId(rs.getString("source_id"));
            dashboard.setVisibility(rs.getInt("visibility"));
            dashboard.setLastupdatedDate(rs.getTimestamp("last_updated_date"));
            dashboard.setHasCommonFilter(rs.getBoolean("common_filter"));
            dashboard.setShowLocalFilter(rs.getBoolean("show_localfilter"));
            dashboard.setLockaddCommonFilter(rs.getBoolean("lock_add_commonfilter"));
            dashboard.setLockChartTitle(rs.getBoolean("lock_charttitle"));
            dashboard.setLockCommonFilter(rs.getBoolean("lock_commonfilter"));
            
            // Only when joining with acl_public table,to get 'role'
            dashboard.setRole(rs.getString("role"));
            return dashboard;

        }

    }

    @Override
    public List<Dashboard> getAllDashboard(String applnId) {
        // Fetches all dshboard for Super user
        List<Dashboard> dashboardList = getJdbcTemplate().query(
                Queries.GET_ALL_DASHBOARD, new DashboardRowMapper(),
                new Object[] { applnId });
        dashboardList = setRole(dashboardList, Constants.ROLE_ADMIN);
        return dashboardList;
    }

    @Override
    public Dashboard getDashboard(Integer dashboardId) {
        return getJdbcTemplate().queryForObject(Queries.GET_DASHBOARD,
                new DashboardRowMapper(), new Object[] { dashboardId });
    }

    @Override
    public List<String> getDashboardName(String userId, String applicationId) {
        return getJdbcTemplate().queryForList(Queries.GET_DASHBOARD_NAME,
                new Object[] { userId, applicationId }, String.class);
    }

    @Override
    public void saveFilterOrder(Integer dashboardId, String order) {
        LOG.debug("Updating oder " + order + " Dashboard id - " + dashboardId);
        
        getJdbcTemplate().update("UPDATE dashboard_details SET filter_order = ? WHERE dashboard_id = ?", new Object[] {order , dashboardId});
    }

    @Override
    public String getFilterOrder(Integer dashboardId) {
        return getJdbcTemplate().queryForObject("SELECT filter_order from dashboard_details WHERE dashboard_id = ?", String.class, new Object[]{dashboardId});
    }

}
