package org.hpccsystems.dashboard.service.impl;

import java.util.List;
import java.util.Map;

import org.hpcc.HIPIE.utils.HPCCConnection;
import org.hpccsystems.dashboard.entity.widget.ChartdataJSON;
import org.hpccsystems.dashboard.entity.widget.Widget;
import org.hpccsystems.dashboard.service.WSSQLService;

import com.mysql.jdbc.Field;

public class WSSQLServiceImpl implements WSSQLService{

    private static ChartdataJSON parseResponse(List<String> columns, String responseXML){
        //TODO Implemented by Senthil
        return null;
    };
    
    @Override
    public List<String> getDistinctValues(Field field, HPCCConnection connection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Number> getMinMax(Field field, HPCCConnection connection) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ChartdataJSON getChartdata(Widget widget, HPCCConnection connection) {
        // TODO Auto-generated method stub
        return null;
    }

}
