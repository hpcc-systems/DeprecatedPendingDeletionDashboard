package org.hpccsystems.dashboard.chart.entity;

import java.util.List;

import org.hpccsystems.dashboard.common.Constants;

public class Field {
    
    String columnName;
    String dataType;
    
    private List<Field> children;
    
    public Field() {
    }
    
    public Field(String columnName, String datType){
        this.columnName = columnName;
        this.dataType = datType;
    }
    
    public String getColumnName() {
        return columnName;
    }
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }
    public String getDataType() {
        return dataType;
    }
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    @Override
    public int hashCode() {        
        return (columnName == null) ? 0 : columnName.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        
        if(obj instanceof Field) {
            Field other = (Field) obj;
            if (columnName == null) {
                if (other.columnName != null) {
                    return false;
                }
            } else if (!columnName.equals(other.columnName)){
                return false;
            }
            return true;
        } else {
            return obj.equals(this.columnName);
        }
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Field [columnName=").append(columnName)
                .append(", dataType=").append(dataType).append("]");
        return builder.toString() ;
    }

    public List<Field> getChildren() {
        return children;
    }

    public void setChildren(List<Field> children) {
        this.children = children;
    }
    
    public boolean isDatasetField() {
        return Constants.DATA_TYPE_DATASET_STRING.equals(dataType);
    }
}
