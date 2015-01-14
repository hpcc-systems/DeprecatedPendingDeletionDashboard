package org.hpccsystems.dashboard.util;

public class DashboardUtil {
    
    public static String removeSpaceSplChar(String str){
        return  str.replaceAll("[^a-zA-Z0-9]+", "");
    }

}
