package org.hpccsystems.dashboard.util;

import java.util.HashSet;
import java.util.Set;

import org.hpccsystems.dashboard.chart.entity.Filter;

public class DashboardUtil {    
    /**
     * Checks whether a column is numeric
     * @param column
     * @param dataType
     * @return
     */
    public static boolean checkNumeric(final String dataType) {
        boolean numericColumn = false;
            if(dataType.contains("integer")    || 
                    dataType.contains("real") || 
                    dataType.contains("decimal") ||  
                    dataType.contains("unsigned"))    {
                numericColumn = true;
            }
        return numericColumn;
    }   
   
    
    /**Checks for real/double data
     * @param dataType
     * @return boolean
     */
    public static boolean checkRealValue(final String dataType) {
        boolean realcColumn = false;
            if(dataType.contains("real")    || 
                    dataType.contains("decimal")){
                realcColumn = true;
            }
        return realcColumn;
    }
    
    public static boolean checkForMultipleDatasetFilter(final Set<Filter> chartDataFltr,String dataSetName) {
        Set<String> setToCheckMultiple=new HashSet<String>();
        setToCheckMultiple.add(dataSetName);
        for (Filter fltrObj : chartDataFltr) {
            setToCheckMultiple.add(fltrObj.getFileName());
        }
        return setToCheckMultiple.size()>1?true:false;
    }

}
