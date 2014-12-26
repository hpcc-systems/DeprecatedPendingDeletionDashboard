package org.hpccsystems.dashboard.service;

import java.util.List;
import java.util.Map;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.entity.widget.ChartdataJSON;
import org.hpccsystems.dashboard.entity.widget.Widget;

import com.mysql.jdbc.Field;

public interface WSSQLService {
    List<String> getDistinctValues(Field field, HPCCConnection connection);
    Map<String, Number> getMinMax(Field field, HPCCConnection connection);
    ChartdataJSON getChartdata(Widget widget, HPCCConnection connection);
}
