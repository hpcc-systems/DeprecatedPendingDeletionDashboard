package org.hpccsystems.dashboard.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class ChartConfiguration {
    private String jsURL;
    private String imageURL;
    private Boolean enableFilter;
    private List<String> dependentJsURL;
    private List<String> dependentCssURL;
    private List<String> googlePackages;
    private String functionName;
    
    @XmlElement
    public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	@XmlElement
    public List<String> getGooglePackages() {
        return googlePackages;
    }
    public void setGooglePackages(List<String> googlePackages) {
        this.googlePackages = googlePackages;
    }
    
    public List<String> getDependentJsURL() {
        return dependentJsURL;
    }
    public void setDependentJsURL(List<String> dependentJsURL) {
        this.dependentJsURL = dependentJsURL;
    }
    
    @XmlElement
    public List<String> getDependentCssURL() {
        return dependentCssURL;
    }
    public void setDependentCssURL(List<String> dependentCssURL) {
        this.dependentCssURL = dependentCssURL;
    }
    
    @XmlElement
    public String getJsURL() {
        return jsURL;
    }
    public void setJsURL(String jsURL) {
        this.jsURL = jsURL;
    }
    
    @XmlElement
    public String getImageURL() {
        return imageURL;
    }
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }    
    
    @XmlElement
    public Boolean getEnableFilter() {
        return enableFilter;
    }
    public void setEnableFilter(Boolean enableFilter) {
        this.enableFilter = enableFilter;
    }
}
