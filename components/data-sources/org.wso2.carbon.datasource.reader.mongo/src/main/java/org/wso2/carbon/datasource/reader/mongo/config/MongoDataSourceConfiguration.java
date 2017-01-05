package org.wso2.carbon.datasource.reader.mongo.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author jmalvarezf
 * 
 *         Configuration class for MongoDB.
 *
 */
@XmlRootElement(name = "configuration")
public class MongoDataSourceConfiguration {

	private String url;

	private String host;

	private String port;

	private ReplicaSetOptionsConfig replicaSetConfig;

	private Boolean withSSL;

	private String username;

	private String password;

	private String database;

	@XmlElement(name = "host")
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@XmlElement(name = "port")
	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	@XmlElement(name = "replicaSetOptions")
	public ReplicaSetOptionsConfig getReplicaSetConfig() {
		return replicaSetConfig;
	}

	public void setReplicaSetConfig(ReplicaSetOptionsConfig replicaSetConfig) {
		this.replicaSetConfig = replicaSetConfig;
	}

	@XmlElement(name = "database")
	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	@XmlElement(name = "url", nillable = false, required = true)
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@XmlElement(name = "withSSL")
	public Boolean getWithSSL() {
		return withSSL;
	}

	public void setWithSSL(Boolean withSSL) {
		this.withSSL = withSSL;
	}

	@XmlElement(name = "username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@XmlElement(name = "password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
