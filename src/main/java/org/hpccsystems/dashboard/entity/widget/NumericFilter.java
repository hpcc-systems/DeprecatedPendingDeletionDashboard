package org.hpccsystems.dashboard.entity.widget;

public class NumericFilter extends Filter {
    private Number minValue;
    private Number maxValue;
    private static final String DOT=".";
    
    public NumericFilter() {
    }
    
    public NumericFilter(Field field) {
        super(field);
    }
    
    public Number getMinValue() {
        return minValue;
    }
    public void setMinValue(Number minValue) {
        this.minValue = minValue;
    }
    public Number getMaxValue() {
        return maxValue;
    }
    public void setMaxValue(Number maxValue) {
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
}
