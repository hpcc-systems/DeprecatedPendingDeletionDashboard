package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.List;

import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;

public class Pie extends Widget{
    private Attribute weight;
    private Measure label;
    
    @Override
    public String generateSQL() {
        // TODO Implement
        return null;
    }

    @Override
    public List<String> getColumns() {
        // TODO Auto-generated method stub
        return null;
    }

    public Attribute getWeight() {
        return weight;
    }

    public void setWeight(Attribute weight) {
        this.weight = weight;
    }

    public Measure getLabel() {
        return label;
    }

    public void setLabel(Measure label) {
        this.label = label;
    }
}
