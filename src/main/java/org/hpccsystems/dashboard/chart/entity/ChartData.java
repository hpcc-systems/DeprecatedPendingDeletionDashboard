package org.hpccsystems.dashboard.chart.entity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.hpccsystems.dashboard.common.Constants;

@XmlRootElement
public class ChartData {

    private Map<String, List<Field>> fields;
    private HpccConnection hpccConnection;
    private List<String> files;
    private boolean isFiltered;
    private Set<Filter> filters;
    private Set<Join> joins;

	/**
     * Checks the query, whether it has'GENERIC' key.
     * If it has, constructs query as 'PROJECTNAME_fetch_input_parameters'
     * to fetch the input parameters.
     * @return String
     */
    public String getInputParamQuery() {
    	String query = files.get(0);
    	StringBuilder builder = new StringBuilder();
    	builder.append(StringUtils.substringBefore(query, Constants.GENERIC))
    		.append(Constants.FETCH_INPUT_PARAM);
    	return builder.toString();
    }
    
	public boolean isGenericQuery() {
		return StringUtils.containsIgnoreCase(files.get(0), Constants.GENERIC);		 
	}

	private boolean isQuery;
    private List<InputParams> inputParams;
    
    @XmlTransient
    public Map<String, List<Field>> getFields() {
        return fields;
    }

    public void setFields(Map<String, List<Field>> fields) {
        this.fields = fields;
    }

    @XmlElement
    public HpccConnection getHpccConnection() {
        return hpccConnection;
    }

    public void setHpccConnection(HpccConnection hpccConnection) {
        this.hpccConnection = hpccConnection;
    }

    @XmlElement
    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    @XmlElement
    public boolean getIsFiltered() {
        return isFiltered;
    }

    public void setIsFiltered(boolean isFiltered) {
        this.isFiltered = isFiltered;
    }

    @XmlElement
    public Set<Filter> getFilters() {
        if (filters == null) {
            filters = new LinkedHashSet<Filter>();
        }

        return filters;
    }

    public void setFilters(Set<Filter> filters) {
        this.filters = filters;
    }

    @XmlElement
    public Set<Join> getJoins() {
        return joins;
    }

    public void setJoins(Set<Join> joins) {
        this.joins = joins;
    }


    @XmlElement
    public boolean getIsQuery() {
        return isQuery;
    }

    public void setIsQuery(boolean isQuery) {
        this.isQuery = isQuery;
    }

    
    @XmlElement
    public List<InputParams> getInputParams() {
        return inputParams;
    }

    public void setInputParams(List<InputParams> inputParams) {
        this.inputParams = inputParams;
    }

}
