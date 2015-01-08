package org.hpccsystems.dashboard.entity.widget;

import java.util.Iterator;
import java.util.List;

import org.hpccsystems.dashboard.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringFilter extends Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringFilter.class);

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
        .append(" IN (");
        
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
        stringSql.append(")");
        return stringSql.toString();
    }

    
    @Override
    public boolean hasValues() {
        return values != null && !values.isEmpty();
    }
    
    @Override
    public String getHipieFilterQuery(Filter filter,int index,String chartName) {

        StringBuilder sql = new StringBuilder();
        sql.append(getFilterName(filter,index,chartName)).append(" IN (");

        Iterator<String> valueIterator = ((StringFilter) filter).getValues()
                .iterator();
        while (valueIterator.hasNext()) {
            sql.append("'").append(valueIterator.next());
            if (valueIterator.hasNext()) {
                sql.append("', ");
            } else {
                sql.append("' ");
            }
        }
        sql.append(")");
        if(LOGGER.isDebugEnabled()){
            LOGGER.debug("Hipie filter query -->"+sql);
        }
        
        return sql.toString();

    }
}
