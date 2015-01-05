package org.hpccsystems.dashboard.entity.widget;

import java.util.Iterator;
import java.util.List;

import org.hpccsystems.dashboard.Constants;

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
    public String generateFilterSQL(String fileName) {
        StringBuilder stringSql=new StringBuilder();
        stringSql.append(fileName)
        .append(Constants.DOT)
        .append(this.getColumn())
        .append("IN [");
        
        Iterator<String> valueIterator=values.iterator();
        while(valueIterator.hasNext()){
            stringSql.append("'")
            .append(valueIterator.next());
            if(valueIterator.hasNext()){
                stringSql.append("', ");
            }else{
                stringSql.append("' ");
            }
        }
        stringSql.append("]");
        return stringSql.toString();
    }

    @Override
    public boolean hasValues() {
        return values != null && !values.isEmpty();
    }
}
