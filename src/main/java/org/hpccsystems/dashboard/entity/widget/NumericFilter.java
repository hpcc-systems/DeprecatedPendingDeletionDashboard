package org.hpccsystems.dashboard.entity.widget;

import java.math.BigDecimal;

import org.hpccsystems.dashboard.Constants;

public class NumericFilter extends Filter {
    private BigDecimal minValue;
    private BigDecimal maxValue;
    
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
    public String generateFilterSQL(String fileName) {
        StringBuilder numFilterSql=new StringBuilder();
        numFilterSql.append(fileName)
            .append(Constants.DOT)
            .append(this.getColumn())
            .append(" <= ")
            .append(maxValue)
            .append(" AND ")
            .append(fileName)
            .append(Constants.DOT)
            .append(this.getColumn())
            .append(" >= ")
            .append(minValue);
        
        return numFilterSql.toString();
    }

    @Override
    public boolean hasValues() {
        return minValue != null || maxValue != null;
    }
}
