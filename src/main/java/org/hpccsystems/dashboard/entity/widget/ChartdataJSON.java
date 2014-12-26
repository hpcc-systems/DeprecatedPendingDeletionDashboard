package org.hpccsystems.dashboard.entity.widget;

import java.util.List;

public class ChartdataJSON {
    List<String> columns;
    List<List<Object>> data;
    
    public List<String> getColumns() {
        return columns;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    public List<List<Object>> getData() {
        return data;
    }
    public void setData(List<List<Object>> data) {
        this.data = data;
    }
}
