package org.hpccsystems.dashboard.service;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.chart.entity.Widget;
import org.hpccsystems.dashboard.chart.entity.ChartdataJSON;

public interface WSSQLService {
    ChartdataJSON getChartdata(Widget chartConfig, HPCCConnection connection);
}
