package org.hpccsystems.dashboard.entity.widget.charts;

import java.util.List;

import org.hpccsystems.dashboard.entity.widget.Attribute;
import org.hpccsystems.dashboard.entity.widget.Measure;
import org.hpccsystems.dashboard.entity.widget.Widget;

public class XYChart extends Widget{

    private Attribute attribute;
    private List<Measure> measure;
    private Attribute groupAttribute;
    
    @Override
    public List<String> getColumns() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String generateSQL() {
        // TODO Auto-generated method stub
        return null;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public List<Measure> getMeasure() {
        return measure;
    }

    public void setMeasure(List<Measure> measure) {
        this.measure = measure;
    }

    public Attribute getGroupAttribute() {
        return groupAttribute;
    }

    public void setGroupAttribute(Attribute groupAttribute) {
        this.groupAttribute = groupAttribute;
    }

}
