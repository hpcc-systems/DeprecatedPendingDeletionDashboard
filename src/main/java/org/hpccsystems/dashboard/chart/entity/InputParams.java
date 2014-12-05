package org.hpccsystems.dashboard.chart.entity;

import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InputParams {
    
    private Map<String, String> params;
    
    public InputParams() {
    }
    
    public InputParams(Map<String, String> params) {
        this.params = params;
    }
    

    @XmlElement
    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
            builder.append("InputParams [params=").append(params).append( "]");
        return  builder.toString();
    }
    
}
