package org.hpccsystems.dashboard.chart.tree.entity;

/**
 * Pojo class to hold filter data for Tree Node
 * 
 */
public class TreeFilter {

    private String fileName;
    private String columnName;
    private Integer dataType;
    private String value;

    public TreeFilter() {

    }

    public TreeFilter(String fileName, String columnName, Integer dataType,
            String value) {
        this.fileName = fileName;
        this.columnName = columnName;
        this.dataType = dataType;
        this.value = value;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    @Override
    public String toString() {
        return "TreeFilter [fileName=" + fileName + ", columnName="
                + columnName + ", dataType=" + dataType + ", value=" + value
                + "]";
    }

}
