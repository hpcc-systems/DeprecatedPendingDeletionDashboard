package org.hpccsystems.dashboard;

import java.util.HashMap;
import java.util.Map;

import org.hpccsystems.dashboard.entity.widget.ChartConfiguration;

public class Constants {

	public static final String EXCEPTION = "EXCEPTION - {}";
	public static final String HIPIE_RAW_DATASET = "RawDataset";
	public static final String USER_CREDENTIAL = "userCredential";
    public static final String ON_ADD_DASHBOARD = "onAddDashboard";
    public static final String DASHBOARD = "dashboard";
    public static final Integer FILTER_MINIMUM = 0;
    public static final Integer FILTER_MAXIMUM = 1;
    public static final String UNABLE_TO_FETCH_DATA = "Unable to fetch Hpcc data";
    
    public static enum CHARTS {PIE, DONUT, LINE, BAR, COLUMN, US_MAP}; 

    public static final Map<CHARTS, ChartConfiguration> CHART_CONFIGURATIONS = new HashMap<CHARTS, ChartConfiguration>(){
        private static final long serialVersionUID = 1L;
        {
            put(CHARTS.PIE, new ChartConfiguration("pie", "pie.png", "pie"));
        }
    };
    
    public enum AGGREGATION {
        SUM ("SUM"),
        COUNT ("COUNT"),
        MIN ("MIN"),
        MAX ("MAX"),
        AVG ("AVG"),
        NONE ("NONE");

        private final String name;       

        private AGGREGATION(String s) {
            name = s;
        }
        public String toString(){
           return name;
        }

    }
}

