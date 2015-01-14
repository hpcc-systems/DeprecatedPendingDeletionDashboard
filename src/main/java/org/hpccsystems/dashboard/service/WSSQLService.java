package org.hpccsystems.dashboard.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.entity.widget.ChartdataJSON;
import org.hpccsystems.dashboard.entity.widget.Field;
import org.hpccsystems.dashboard.entity.widget.Filter;
import org.hpccsystems.dashboard.entity.widget.Widget;


public interface WSSQLService {
    List<String> getDistinctValues(Field field, HPCCConnection connection, String fileName, List<Filter> filters) throws Exception;
    Map<String, BigDecimal> getMinMax(Field field, HPCCConnection connection, String fileName, List<Filter> filters) throws Exception;
    ChartdataJSON getChartdata(Widget widget, HPCCConnection connection) throws Exception;
}
