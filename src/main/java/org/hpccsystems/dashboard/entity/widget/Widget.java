package org.hpccsystems.dashboard.entity.widget;

import java.util.List;

public abstract class Widget {
    private String name;
    private Filter filter;
    
    public abstract List<String> getColumns();
    public abstract String generateSQL();
}
