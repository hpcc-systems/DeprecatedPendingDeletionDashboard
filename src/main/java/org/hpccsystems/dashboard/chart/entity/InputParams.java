package org.hpccsystems.dashboard.chart.entity;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InputParams {
    
    private Map<String, String> params;
    private boolean isCommonInput = false;
    
    public InputParams() {
    }
    
    public InputParams(Map<String, String> params) {
        this.params = params;
    }
    

    public InputParams(String key) {
    	params = new HashMap<String,String>();
    	params.put(key, null);
	}

	@XmlElement
    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @XmlElement
    public boolean getIsCommonInput() {
        return isCommonInput;
    }
    
    public void setIsCommonInput(boolean isCommonInput) {
        this.isCommonInput = isCommonInput;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
            builder.append("InputParams [params=").append(params).append( "]");
        return  builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((params == null) ? 0 : params.hashCode());
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
        InputParams other = (InputParams) obj;
        if (params == null) {
            if (other.params != null)
                return false;
        }else if(! params.keySet().iterator().next().equals(other.getParams().keySet().iterator().next()))
            //As each param has only one entry, taking first key
            return false;
        return true;
    }
    
    
}
