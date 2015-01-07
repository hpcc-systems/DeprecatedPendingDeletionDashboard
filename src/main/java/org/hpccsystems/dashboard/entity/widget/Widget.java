package org.hpccsystems.dashboard.entity.widget;

import java.util.ArrayList;
import java.util.List;
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
    
}
