package org.hpccsystems.dashboard.entity.widget;

import java.util.List;

public class ChartdataJSON {
    List<String> columns;
    List<List<Object>> data;
    
    /* Sample chart JSON to be supplied to visualization api for rendering the chart.
     * 
     * columns : ["Subject", "Year 1", "Year 2"]
     * 
     * data : [
            ["Geography", 75, 68],
            ["English", 45, 55],
            ["Math", 98, 92],
            ["Science", 66, 60]
        ]
     */
    
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
