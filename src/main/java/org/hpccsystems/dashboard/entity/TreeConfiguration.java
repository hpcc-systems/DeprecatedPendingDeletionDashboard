package org.hpccsystems.dashboard.entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement 
public class TreeConfiguration extends ChartConfiguration {

    private Integer maxLevels;
    private Integer minLevels;
    
    @XmlElement
    public Integer getMinLevels() {
        return minLevels;
    }
    public void setMinLevels(Integer minLevels) {
        this.minLevels = minLevels;
    }
    @XmlElement
    public Integer getMaxLevels() {
        return maxLevels;
    }
    public void setMaxLevels(Integer maxLevels) {
        this.maxLevels = maxLevels;
    }
}
