package org.hpccsystems.dashboard.entity;
 
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement 
public class XYConfiguration extends ChartConfiguration {
    private Boolean enableXGrouping;
    
    @XmlElement
    public Boolean getEnableXGrouping() {
        return enableXGrouping;
    }
    public void setEnableXGrouping(Boolean enableXGrouping) {
        this.enableXGrouping = enableXGrouping;
    }
}
