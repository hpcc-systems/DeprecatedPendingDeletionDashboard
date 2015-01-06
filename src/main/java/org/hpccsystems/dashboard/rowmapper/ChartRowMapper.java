package org.hpccsystems.dashboard.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hpccsystems.dashboard.chart.utils.XMLConverter;
import org.hpccsystems.dashboard.common.Constants;
import org.hpccsystems.dashboard.entity.ChartDetails;
import org.springframework.jdbc.core.RowMapper;

public class ChartRowMapper implements RowMapper<ChartDetails> {
    
    private static final Log LOG = LogFactory.getLog(ChartRowMapper.class);

    @Override
    public ChartDetails mapRow(ResultSet rs, int rowNum) throws SQLException {
        ChartDetails chart = new ChartDetails();
        chart.setId(rs.getInt("id"));
        chart.setName(rs.getString("name"));
        chart.setDescription(rs.getString("description"));
        try{
        chart.setConfiguration(XMLConverter.makeXYConfigurationObject(rs.getString("configuration")));
        }catch(Exception ex){
            LOG.error(Constants.EXCEPTION, ex);
            throw new SQLException(ex.getMessage()) ;
            
        }
        chart.setCategory(rs.getInt("category"));
        chart.setIsPlugin(rs.getBoolean("isplugin"));
        return chart;
    }

}
