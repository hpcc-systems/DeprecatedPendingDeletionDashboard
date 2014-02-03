package org.hpccsystems.dashboard.entity.chart;

import javax.xml.bind.annotation.XmlElement;

public class HpccConnection {
	String hostIp; 
	Integer port;
	String clusterName;
	
	String username;
	String password;
	Boolean isSSL;
	Boolean allowInvalidCerts;
	public HpccConnection(String hostIp, Integer port, String clusterName,
			String username, String password, Boolean isSSL,
			Boolean allowInvalidCerts) {
		super();
		this.hostIp = hostIp;
		this.port = port;
		this.clusterName = clusterName;
		this.username = username;
		this.password = password;
		this.isSSL = isSSL;
		this.allowInvalidCerts = allowInvalidCerts;
	}
	public HpccConnection() {
		super();
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HpccConnection [hostIp=").append(hostIp)
				.append(", port=").append(port).append(", clusterName=")
				.append(clusterName).append(", username=").append(username)
				.append(", password=").append(password).append(", isSSL=")
				.append(isSSL).append(", allowInvalidCerts=")
				.append(allowInvalidCerts).append("]");
		return builder.toString() ;
	}
	
	
	@XmlElement
	public String getHostIp() {
		return hostIp;
	}
	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}
	
	@XmlElement
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	
	@XmlElement
	public String getClusterName() {
		return clusterName;
	}
	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
	
	@XmlElement
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
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
		return isSSL;
	}
	public void setIsSSL(Boolean isSSL) {
		this.isSSL = isSSL;
	}
	
	@XmlElement
	public Boolean getAllowInvalidCerts() {
		return allowInvalidCerts;
	}
	public void setAllowInvalidCerts(Boolean allowInvalidCerts) {
		this.allowInvalidCerts = allowInvalidCerts;
	}
}
