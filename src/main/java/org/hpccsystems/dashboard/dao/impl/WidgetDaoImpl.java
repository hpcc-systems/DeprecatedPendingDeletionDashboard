package org.hpccsystems.dashboard.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.WidgetDao;
import org.hpccsystems.dashboard.entity.Portlet;
import org.hpccsystems.dashboard.rowmapper.WidgetRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 
/**
 * Dao class to do widget related DB hits
 * @author 
 * 
 */ 
public class WidgetDaoImpl implements WidgetDao{
    private static final  Log LOG = LogFactory.getLog(WidgetDaoImpl.class);
    
    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
    @Autowired
    public void setDataSourceToJdbcTemplate(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    public void initialize() {
        Validate.notNull(dataSource, "'dataSource' must be set!");
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }    
    
    public void addWidgetDetails(final Integer dashboardId,
            final List<Portlet> portlets) throws DataAccessException {

        String sql = Queries.INSERT_WIDGET_DETAILS;
        
        getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {            
            public void setValues(PreparedStatement statement, int i) throws SQLException {
                Portlet portlet = portlets.get(i);
                statement.setInt(1, dashboardId);
                statement.setString(2,portlet.getName() );
                statement.setString(3, portlet.getWidgetState());
                if (Constants.STATE_EMPTY.equals(portlet.getWidgetState())) {
                    statement.setInt(4, 0);
                } else {
                    statement.setInt(4, portlet.getChartType());
                }
                statement.setInt(5, portlet.getColumn());
                statement.setInt(6, i);
                statement.setString(7, portlet.getChartDataXML());   
                statement.setBoolean(8, portlet.getIsSinglePortlet());   
            }
            
            public int getBatchSize() {
                return portlets.size();
            }
        });
    
    
        
    }    
    
    public List<Portlet> retriveWidgetDetails(Integer dashboardId) throws DataAccessException{
            StringBuilder sqlBuffer = new StringBuilder();
            sqlBuffer.append(Queries.GET_WIDGET_DETAILS).append(dashboardId).append(" order by widget_sequence");
            return getJdbcTemplate().query(sqlBuffer.toString(),new WidgetRowMapper());
        }
    
    @Override
    public void deleteWidget(final Integer portletId) throws DataAccessException {
        
        getJdbcTemplate().update(Queries.DELETE_WIDGETS, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement statement) throws SQLException {
                statement.setInt(1, portletId);        
            }
        });
    }
    
    @Override
    public void updateWidgetSequence(final Integer dashboardId,final List<Portlet> portlets) throws DataAccessException {
        String sql = Queries.UPDATE_WIDGET_SEQUENCE;
        getJdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement statement, int i)throws SQLException {
                Portlet portlet = portlets.get(i);
                statement.setInt(1, portlet.getColumn());
                statement.setInt(2, i);
                statement.setInt(3, portlet.getId());
                statement.setInt(4,dashboardId);
                
            }
            public int getBatchSize() {
                return portlets.size();
                }
            
        });
    }
    @Override
    public void updateWidget(Portlet portlet) throws DataAccessException {
        if(LOG.isDebugEnabled()){
            LOG.debug("Updating portlet to DB " + portlet.toString());
        }
        
        if(portlet.getWidgetState().equals(Constants.STATE_LIVE_CHART)){
            //Updates Live chart data with state as 'Live'
            String updateQuery = Queries.UPADET_LIVE_CHART_DATA;
            getJdbcTemplate().update(updateQuery, new Object[] { 
                    portlet.getWidgetState(),
                    portlet.getChartType(),
                    portlet.getChartDataXML(),
                    portlet.getId()
            });
        }else if(portlet.getChartType() != null){
            //Updates widget state as 'Grayed'
            String addQuery = Queries.ADD_CHART_DATA;
            getJdbcTemplate().update(addQuery, new Object[] { 
                    portlet.getWidgetState(),
                    portlet.getChartType(),
                    portlet.getId()
            });
        }else {
            //Resets widget data/Clears chart data
            String clearQuery = Queries.CLEAR_CHART_DATA;
            getJdbcTemplate().update(clearQuery, new Object[] {
                    portlet.getName(),                    
                    portlet.getWidgetState(),
                    portlet.getChartType(),
                    portlet.getChartDataXML(),
                    portlet.getId()
            });
        }
        
    }
    @Override
    public void updateWidgetTitle(Portlet portlet) throws DataAccessException {
        String sql = Queries.UPADET_WIDGET_NAME;
        getJdbcTemplate().update(sql, new Object[] {
                portlet.getName(),                    
                portlet.getId()
        });
        
    }

    @Override
    public void addWidget(final Integer dashboardId, final Portlet portlet, final Integer sequence)  throws DataAccessException {

        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("dashboard_id", dashboardId);
        parameters.put("widget_name", portlet.getName());
        parameters.put("widget_state", portlet.getWidgetState());
        
        if (Constants.STATE_EMPTY.equals(portlet.getWidgetState())) {
            parameters.put("chart_type", 0);
        } else {
            parameters.put("chart_type", portlet.getChartType());
        }
       
        
        parameters.put("column_identifier", portlet.getColumn());
        parameters.put("widget_sequence",sequence);
        parameters.put("chart_data", portlet.getChartDataXML());
        parameters.put("single_widget",  portlet.getIsSinglePortlet());
        
        Number newId = new SimpleJdbcInsert(jdbcTemplate.getDataSource())
                        .withTableName("widget_details")
                        .usingGeneratedKeyColumns("widget_id")
                        .executeAndReturnKey(parameters);
        
        portlet.setId(newId.intValue());
        
    }
    
    @Override
    public int updateHpccPassword(List<Integer> dashboardIds, String hostIp,
            String username, String password) {
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE widget_details ")
            .append("set chart_data = UpdateXML(chart_data , '//password', ")
            .append(" :password ")
            .append(" ) where ExtractValue(chart_data, '//hostIp') = ")
            .append(" :hostIp ")
            .append(" and ExtractValue(chart_data, '//username') = ")
            .append(" :username ")
            .append(" and dashboard_id IN ")
            .append("( :dashboardIds )");
        
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());
        
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("dashboardIds", dashboardIds);
        parameters.addValue("password", "<password>" + password + "</password>" );
        parameters.addValue("username", username);
        parameters.addValue("hostIp", hostIp);
        
        return namedParameterJdbcTemplate.update(builder.toString(), parameters);
    }
    @Override
    public void addOrUpdateCommonInput(Integer dashboardId, String commonInputText,
            String userId) {
        String INSERT_UPDATE_COMMON_INPUT= "INSERT INTO dashboard_filters(dashboard_id,user_id,filter_data) values (?,?,?)  ON DUPLICATE KEY UPDATE filter_data=?";
        getJdbcTemplate().update(INSERT_UPDATE_COMMON_INPUT,new Object[] {
                dashboardId,                    
                userId,
                commonInputText ,
                commonInputText
        });
    }
}
