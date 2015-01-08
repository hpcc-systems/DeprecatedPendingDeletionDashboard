package org.hpccsystems.dashboard.entity.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.hpcc.HIPIE.dude.InputElement;
import org.hpcc.HIPIE.dude.VisualElement;

public abstract class Widget {
    private String name;
    private String logicalFile;
    private List<Filter> filters;
    private String title;
    private ChartConfiguration chartConfiguration;  

	public abstract boolean isConfigured();

	public abstract List<String> getColumns();

	public abstract List<String> getSQLColumns();

	public abstract String generateSQL();

	public abstract VisualElement generateVisualElement();

	public abstract List<InputElement> generateInputElement();

	public abstract Map<String, String> getInstanceProperties();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public String getLogicalFile() {
		return logicalFile;
	}

	public void setLogicalFile(String logicalFile) {
		this.logicalFile = logicalFile;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public ChartConfiguration getChartConfiguration() {
		return chartConfiguration;
	}

	public void setChartConfiguration(ChartConfiguration chartConfiguration) {
		this.chartConfiguration = chartConfiguration;
	}

    public void addFilter(Filter filter) {
        if(filters == null) {
            filters = new ArrayList<Filter>();
        }
        filters.add(filter);
    }
    public void removeFilter(Filter filter) {
        filters.remove(filter);
    }
    
    public String getFilterQuery(StringBuilder sql){
        Iterator<Filter> filters=this.getFilters().iterator();
        while(filters.hasNext()){
            sql.append(filters.next().generateFilterSQL(getLogicalFile()));
            if(filters.hasNext()){
                sql.append(" AND ");
            }
        }   
       return sql.toString();
   }
    
    public String getHipieFilterQuery(){
        StringBuilder query = new StringBuilder();
        ListIterator<Filter> filters= this.getFilters().listIterator();
        Filter filter = null;
        while(filters.hasNext()){
            filter = filters.next();           
            query.append(filter.getHipieFilterQuery(filter,filters.nextIndex(), this.getName()));
            if(filters.hasNext()){
                query.append(" AND ");
            }
        }   
        
       return query.toString();
    }
    
}

