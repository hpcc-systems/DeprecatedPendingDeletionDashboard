package org.hpccsystems.dashboard.chart.entity;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Measure {

    private String column;
    private String aggregateFunction;
    private String displayYColumnName;
    private String fileName;
    private boolean isSecondary;

    public Measure() {

    }

    @XmlAttribute
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Creates aggregated Measure
     * 
     * @param columnName
     * @param aggregateFunction
     */
    public Measure(String columnName, String aggregateFunction) {
        this.column = columnName;
        this.aggregateFunction = aggregateFunction;
    }

    @XmlAttribute
    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    @XmlAttribute
    public String getAggregateFunction() {
        return aggregateFunction;
    }

    public void setAggregateFunction(String aggregateFunction) {
        this.aggregateFunction = aggregateFunction;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((column == null) ? 0 : column.hashCode());
        result = prime
                * result
                + ((aggregateFunction == null) ? 0 : aggregateFunction
                        .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Measure) {
            Measure arg = (Measure) obj;
            if(arg.getAggregateFunction() != null){
                return this.column.equals(arg.getColumn())
                        && this.aggregateFunction.equals(arg.getAggregateFunction());
            }else{
                return this.column.equals(arg.getColumn());
            }
        } else if (obj instanceof Filter) {
            Filter arg = (Filter) obj;
            return this.column.equals(arg.getColumn());
        } else {
            return this.column.equals(obj);
        }
    }

    public String getDisplayYColumnName() {
        return displayYColumnName;
    }

    public void setDisplayYColumnName(String displayYColumnName) {
        this.displayYColumnName = displayYColumnName;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Measure [column=").append(column)
                .append(", aggregateFunction=").append(aggregateFunction)
                .append(", displayYColumnName=").append(displayYColumnName)
                .append(", fileName=").append(fileName).append("]");

        return buffer.toString();
    }

    public boolean isSecondary() {
        return getIsSecondary();
    }

    @XmlAttribute
    public boolean getIsSecondary() {
        return isSecondary;
    }
    
    public void setIsSecondary(boolean isSecondary) {
        this.isSecondary = isSecondary;
    }

    public String getLabel() {
        return getDisplayYColumnName() != null ? getDisplayYColumnName() :
             getAggregateFunction() != null ? getColumn() + "_" + getAggregateFunction() :
                 getColumn();
    }
}
