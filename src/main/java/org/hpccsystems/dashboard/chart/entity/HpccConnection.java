package org.hpccsystems.dashboard.chart.entity;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.google.gson.annotations.SerializedName;

public class HpccConnection {
    private String name;
    
    private String serverHost;
    private Integer serverPort;
    private  Integer wssqlPort;
    private  Integer wsEclPort;
    private String userName;
    private String password;
    private Boolean isHttps;
    private Boolean allowInvalidCerts;
    private String datasource;

	@SerializedName("clusterName")
    String clusterType;

    @XmlElement
    public Integer getWsEclPort() {
        return wsEclPort;
    }

    public void setWsEclPort(Integer wsEclPort) {
        this.wsEclPort = wsEclPort;
    }

    public HpccConnection() {
        super();
    }


    @XmlElement
    public Integer getEspPort() {
        return serverPort;
    }

    public void setEspPort(Integer espPort) {
        this.serverPort = espPort;
    }

    @XmlElement
    public Integer getWssqlPort() {
        return wssqlPort;
    }

    public void setWssqlPort(Integer wssqlPort) {
        this.wssqlPort = wssqlPort;
    }

    @XmlElement
    public String getHostIp() {
        return serverHost;
    }

    public void setHostIp(String hostIp) {
        this.serverHost = hostIp;
    }

    @XmlElement
    public String getUsername() {
        return userName;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    @XmlElement
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlElement
    public Boolean getIsSSL() {
        return isHttps;
    }

    public void setIsSSL(Boolean isSSL) {
        this.isHttps = isSSL;
    }

    @XmlElement
    public Boolean getAllowInvalidCerts() {
        return allowInvalidCerts;
    }

    public void setAllowInvalidCerts(Boolean allowInvalidCerts) {
        this.allowInvalidCerts = allowInvalidCerts;
    }

    @XmlElement
    public String getClusterType() {
        return clusterType;
    }

    public void setClusterType(String clustertype) {
        this.clusterType = clustertype;
    }
    
    @XmlElement
    public String getDatasource() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = datasource;
	}

	@XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("HpccConnection [hostIp=").append(serverHost)
        .append(", espPort=").append(serverPort).append(", wssqlPort=")
        .append(", clusterType=").append(clusterType)
        .append(", userName=").append(userName).append(", password=")
        .append(password).append(", isSSL=").append(isHttps)
        .append(", allowInvalidCerts=").append(allowInvalidCerts)
        .append(", roxiePort=").append(wsEclPort)
        .append("]");
        return builder.toString();
    }
}
