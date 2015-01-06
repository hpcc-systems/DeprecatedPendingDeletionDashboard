package org.hpccsystems.dashboard.chart.tree.entity;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hpccsystems.dashboard.chart.entity.ChartData;


@XmlRootElement
public class TreeData extends ChartData{
    List<Level> levels;
    Map<String,String> rootValueMap;
    
    @XmlElement
    public Map<String, String> getRootValueMap() {
        return rootValueMap;
    }

    public void setRootValueMap(Map<String, String> rootValueMap) {
        this.rootValueMap = rootValueMap;
    }

    @XmlElement
    public List<Level> getLevels() {
        return levels;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }
}
