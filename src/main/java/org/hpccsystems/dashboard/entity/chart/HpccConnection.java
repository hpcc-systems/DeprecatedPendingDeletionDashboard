package org.hpccsystems.dashboard.entity.chart;

import javax.xml.bind.annotation.XmlElement;

public class HpccConnection {
	String serverHost; 
	Integer serverPort;
	String clusterName;
	
	String userName;
	String password;
	Boolean isHttps;
	Boolean allowInvalidCerts;
	public HpccConnection(String hostIp, Integer port, String clusterName,
			String username, String password, Boolean isSSL,
			Boolean allowInvalidCerts) {
		super();
		this.serverHost = hostIp;
		this.serverPort = port;
		this.clusterName = clusterName;
		this.userName = username;
		this.password = password;
		this.isHttps = isSSL;
		this.allowInvalidCerts = allowInvalidCerts;
	}
	public HpccConnection() {
		super();
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HpccConnection [hostIp=").append(serverHost)
				.append(", port=").append(serverPort).append(", clusterName=")
				.append(clusterName).append(", userName=").append(userName)
				.append(", password=").append(password).append(", isSSL=")
				.append(isHttps).append(", allowInvalidCerts=")
				.append(allowInvalidCerts).append("]");
		return builder.toString() ;
	}
	
	
	@XmlElement
	public String getHostIp() {
		return serverHost;
	}
	public void setHostIp(String hostIp) {
		this.serverHost = hostIp;
	}
	
	@XmlElement
	public Integer getPort() {
		return serverPort;
	}
	public void setPort(Integer port) {
		this.serverPort = port;
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
}
