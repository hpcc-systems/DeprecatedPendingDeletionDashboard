package org.hpccsystems.dashboard.entity.widget;

import java.math.BigDecimal;

public class NumericFilter extends Filter {
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private static final String DOT=".";
    
    public NumericFilter() {
    }
    
    public NumericFilter(Field field) {
        super(field);
    }
    
    public BigDecimal getMinValue() {
        return minValue;
    }
    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }
    public BigDecimal getMaxValue() {
        return maxValue;
    }
    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }
    @Override
    public String generateFilterSQL() {
        StringBuilder numFilterSql=new StringBuilder();
        numFilterSql.append("[")
        .append(this.getFile())
        .append(DOT)
        .append(this.getColumn())
        .append(" <=")
        .append(maxValue)
        .append(" AND ")
        .append(this.getFile())
        .append(DOT)
        .append(this.getColumn())
        .append(" >=")
        .append(minValue)
        .append("]");
        
        return numFilterSql.toString();
    }

    @Override
    public boolean hasValues() {
        return minValue != null || maxValue != null;
    }
}
