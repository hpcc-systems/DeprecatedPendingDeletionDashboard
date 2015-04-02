package org.hpccsystems.dashboard.chart.entity;



public class TitleColumn {

    private String label;
    private String name;
    private String value;
    
    public TitleColumn(String titleColumnLabel, String titleColumnName) {
        this.label = titleColumnLabel;
        this.name = titleColumnName;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return "TitleColumn [label=" + label + ", name=" + name + ", value="
                + value + "]";
    }
    
    
}
