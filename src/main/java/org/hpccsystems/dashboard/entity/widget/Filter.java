package org.hpccsystems.dashboard.entity.widget;


public abstract class Filter extends Field {
    public Filter() {
    }
    
    public Filter(Field field) {
        super(field);
    }
    
    public abstract String generateFilterSQL(String fileName);
    
    public abstract boolean hasValues();

    public abstract String getHipieFilterQuery(Filter filter,int index,String chartName);
    
    /**
     * Method to generate Contract Filter field name as 'Filter1_chartName'
     * @param filter
     * @param index
     * @param chartName
     * @return String
     */
    public String getFilterName(Filter filter,int index,String chartName) {
        StringBuilder filterName = new StringBuilder();
        filterName.append("Filter")
                .append(index + 1).append("_")
                .append(chartName);
        return filterName.toString();
    }
    
}