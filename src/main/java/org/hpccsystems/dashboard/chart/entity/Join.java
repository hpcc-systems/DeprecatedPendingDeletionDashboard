package org.hpccsystems.dashboard.chart.entity;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Join {
    private String type;
    private String condition;
    private String firstFileColumn;
    private String secondFileColumn;
    
    public Join() {}
    
    public Join(String type, String condition, String firstFileColumn, String secondFileColumn) {
        this.type = type;
        this.condition = condition;
        this.firstFileColumn = firstFileColumn;
        this.secondFileColumn = secondFileColumn;
    }
    
    @XmlAttribute
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    
    @XmlAttribute
    public String getCondition() {
        return condition;
    }
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    @XmlAttribute
    public String getFirstFileColumn() {
        return firstFileColumn;
    }
    public void setFirstFileColumn(String firstFileColumn) {
        this.firstFileColumn = firstFileColumn;
    }
    
    @XmlAttribute
    public String getSecondFileColumn() {
        return secondFileColumn;
    }
    public void setSecondFileColumn(String secondFileColumn) {
        this.secondFileColumn = secondFileColumn;
    }
    
    /**
     * @return
     *     the SQL for the join condition
     *     null - if join object is not formed entirely
     */
    public String getSql() {
        if(type != null && condition != null && firstFileColumn != null && secondFileColumn != null) {
            StringBuilder builder = new StringBuilder(firstFileColumn);
            builder.append(condition)
                .append(secondFileColumn);
            
            return builder.toString();
        }
        
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result
                + ((firstFileColumn == null) ? 0 : firstFileColumn.hashCode());
        result = prime
                * result
                + ((secondFileColumn == null) ? 0 : secondFileColumn.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     *  
     *  Method is overridden for equal join only
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Join other = (Join) obj;
        if(other.getCondition().equals(this.getCondition())) {
            if(this.firstFileColumn.equals(other.getFirstFileColumn()) &&
                    this.secondFileColumn.equals(other.getSecondFileColumn())) {
                return true;
            } else if (this.secondFileColumn.equals(other.getFirstFileColumn()) &&
                    this.firstFileColumn.equals(other.getSecondFileColumn())) {
                return true;
            }
        } else {
            if(this.firstFileColumn.equals(other.getFirstFileColumn()) &&
                    this.secondFileColumn.equals(other.getSecondFileColumn()) && 
                    this.getCondition().equals(other.getCondition())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Join [type=" + type + ", condition=" + condition
                + ", firstFileColumn=" + firstFileColumn
                + ", secondFileColumn=" + secondFileColumn + "]";
    }
    
    
    
}
