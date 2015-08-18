package org.hpccsystems.dashboard.chart.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class InputParams {
    
    private List<InputParam> inputParams;

    public List<InputParam> getInputParams() {
        return inputParams;
    }

    @XmlElement
    public void setInputParams(List<InputParam> inputParams) {
        this.inputParams = inputParams;
    }

    @Override
    public String toString() {
        return "InputParams [inputParams=" + inputParams + "]";
    }
    
}
