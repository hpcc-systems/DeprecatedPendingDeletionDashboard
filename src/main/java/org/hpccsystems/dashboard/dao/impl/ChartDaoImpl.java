package org.hpccsystems.dashboard.dao.impl;

import java.util.List;

import javax.sql.DataSource;

import org.hpccsystems.dashboard.common.Queries;
import org.hpccsystems.dashboard.dao.ChartDao;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.hpccsystems.dashboard.rowmapper.ChartRowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class ChartDaoImpl implements ChartDao {
    
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int addPlugin(String name, String description, String configData,
            String userId, int category, boolean isPlugin)throws DataAccessException {
        jdbcTemplate.update(Queries.INSERT_PLUGIN, new Object[] { 
                name,
                description,
                configData,
                userId,
                category,
                isPlugin
        });    
        return jdbcTemplate.queryForInt("select last_insert_id()"); 
    }

    @Override
    public List<ChartDetails> getCharts() throws DataAccessException {
        return jdbcTemplate.query(Queries.GET_CHARTS,new ChartRowMapper());
    }

    @Override
    public void deletePlugin(int pluginId) throws DataAccessException {
        jdbcTemplate.update(Queries.DELETE_CHART, new Object[]{ pluginId });
    }

    @Override
    public List<ChartDetails> getPlugins() throws DataAccessException {
        return jdbcTemplate.query(Queries.GET_PLUGINS, new ChartRowMapper());
    }

}
