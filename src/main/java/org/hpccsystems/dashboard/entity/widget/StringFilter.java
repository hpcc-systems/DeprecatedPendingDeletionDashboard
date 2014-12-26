package org.hpccsystems.dashboard.entity.widget;

import java.util.List;

public class StringFilter extends Filter {

    private List<String> values;
    
    public List<String> getValues() {
        return values;
    }
    public void setValues(List<String> values) {
        this.values = values;
    }
    @Override
    public String generateFilterSQL() {
        // TODO Auto-generated method stub
        return null;
    }
}
