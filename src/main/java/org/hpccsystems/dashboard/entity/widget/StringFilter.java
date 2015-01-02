package org.hpccsystems.dashboard.entity.widget;

import java.util.List;

public class StringFilter extends Filter {

    private List<String> values;
    
    public StringFilter() {
    }
    
    public StringFilter(Field field) {
        super(field);
    }
    
    public List<String> getValues() {
        return values;
    }
    public void setValues(List<String> values) {
        this.values = values;
    }
    @Override
    public String generateFilterSQL() {
        StringBuilder stringSql=new StringBuilder();
        stringSql.append("IN [");
        values.stream().forEach(value->{
            stringSql.append("'")
            .append(value)
            .append("', ");
        });
        stringSql.substring(0, stringSql.length()-4);
        stringSql.append("]");
        return stringSql.toString();
    }
}
