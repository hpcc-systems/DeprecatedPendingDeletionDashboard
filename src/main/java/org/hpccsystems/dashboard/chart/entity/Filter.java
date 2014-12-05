package org.hpccsystems.dashboard.chart.entity;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Filter {
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Filter [fileName=").append(fileName).append(", column=")
                .append(column).append(", type=").append(type)
                .append(", values=").append(values).append(", StartValue=")
                .append(StartValue).append(", EndValue=").append(EndValue)
                .append(", isCommonFilter=").append(isCommonFilter)
                .append(", dateFilterFormat=").append(currentDateFormat)
                .append("]");
        return buffer.toString();
    }

    private String fileName;
    private String column;
    
    private Integer type;

    /**
     *  Present only got String Filter
     */
    private List<String> values;
    
    /**
     * Present only for Numeric Filter
     */
    private BigDecimal StartValue;
    private BigDecimal EndValue;
    
    private boolean isCommonFilter = false;
    
    /**
     * refers the format of current date filter
     */
    private String currentDateFormat;
    
    @XmlAttribute
    public String getCurrentDateFormat() {
        return currentDateFormat;
    }

    public void setCurrentDateFormat(String dateFilterFormat) {
        this.currentDateFormat = dateFilterFormat;
    }

    @XmlAttribute
    public Integer getType() {
        return type;
    }
    
    public void setType(Integer filterType) {
        this.type = filterType;
    }
    
    @XmlElement
    public List<String> getValues() {
        return values;
    }
    
    public void setValues(List<String> list) {
        this.values = list;
    }
    
    @XmlElement
    public BigDecimal getStartValue() {
        return StartValue;
    }
    
    public void setStartValue(BigDecimal startValue) {
        StartValue = startValue;
    }
    
    @XmlElement
    public BigDecimal getEndValue() {
        return EndValue;
    }
    
    public void setEndValue(BigDecimal endValue) {
        EndValue = endValue;
    }
    
    @XmlElement
    public String getColumn() {
        return column;
    }
    
    public void setColumn(String column) {
        this.column = column;
    }
    
    @XmlElement
    public boolean getIsCommonFilter() {
        return isCommonFilter;
    }
    
    public void setIsCommonFilter(boolean isGlobalFilter) {
        this.isCommonFilter = isGlobalFilter;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Filter){
            Filter filter= (Filter) obj;
            if(this.column.equals(filter.getColumn())
                    &&  this.fileName.equals(filter.getFileName())){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return this.column.hashCode();
    }

    @XmlElement
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
}
