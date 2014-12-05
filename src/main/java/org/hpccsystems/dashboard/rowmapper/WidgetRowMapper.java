package org.hpccsystems.dashboard.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.hpccsystems.dashboard.entity.Portlet;
import org.springframework.jdbc.core.RowMapper;


/**
 * Class to get mapped the User details from DB to User Object
 *
 */
public class WidgetRowMapper implements RowMapper<Portlet> {

    public Portlet mapRow(ResultSet rs, int rowNum) throws SQLException {
        Portlet portlet = new Portlet();
        portlet.setId(rs.getInt("widget_id"));
        portlet.setName(rs.getString("widget_name"));
        portlet.setWidgetState(rs.getString("widget_state"));
        portlet.setColumn(rs.getInt("column_identifier"));
        portlet.setChartType(rs.getInt("chart_type"));
        portlet.setChartDataXML(rs.getString("chart_data"));
        portlet.setIsSinglePortlet(rs.getBoolean("single_widget"));
        return portlet;
    }

}
