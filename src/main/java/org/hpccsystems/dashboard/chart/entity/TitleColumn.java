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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TitleColumn other = (TitleColumn) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "TitleColumn [label=" + label + ", name=" + name + ", value="
                + value + "]";
    }
    
    
}
