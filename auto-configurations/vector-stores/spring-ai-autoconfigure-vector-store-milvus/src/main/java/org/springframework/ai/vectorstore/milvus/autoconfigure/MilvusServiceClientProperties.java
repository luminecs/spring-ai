package org.springframework.ai.vectorstore.milvus.autoconfigure;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(MilvusServiceClientProperties.CONFIG_PREFIX)
public class MilvusServiceClientProperties {

	public static final String CONFIG_PREFIX = "spring.ai.vectorstore.milvus.client";

	protected boolean secure = false;

	private String host = "localhost";

	private int port = 19530;

	private String uri;

	private String token;

	private long connectTimeoutMs = 10000;

	private long keepAliveTimeMs = 55000;

	private long keepAliveTimeoutMs = 20000;

	private long rpcDeadlineMs = 0;

	private String clientKeyPath;

	private String clientPemPath;

	private String caPemPath;

	private String serverPemPath;

	private String serverName;

	private long idleTimeoutMs = TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS);

	private String username = "root";

	private String password = "milvus";

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUri() {
		return this.uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getConnectTimeoutMs() {
		return this.connectTimeoutMs;
	}

	public void setConnectTimeoutMs(long connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
	}

	public long getKeepAliveTimeMs() {
		return this.keepAliveTimeMs;
	}

	public void setKeepAliveTimeMs(long keepAliveTimeMs) {
		this.keepAliveTimeMs = keepAliveTimeMs;
	}

	public long getKeepAliveTimeoutMs() {
		return this.keepAliveTimeoutMs;
	}

	public void setKeepAliveTimeoutMs(long keepAliveTimeoutMs) {
		this.keepAliveTimeoutMs = keepAliveTimeoutMs;
	}

	public long getRpcDeadlineMs() {
		return this.rpcDeadlineMs;
	}

	public void setRpcDeadlineMs(long rpcDeadlineMs) {
		this.rpcDeadlineMs = rpcDeadlineMs;
	}

	public String getClientKeyPath() {
		return this.clientKeyPath;
	}

	public void setClientKeyPath(String clientKeyPath) {
		this.clientKeyPath = clientKeyPath;
	}

	public String getClientPemPath() {
		return this.clientPemPath;
	}

	public void setClientPemPath(String clientPemPath) {
		this.clientPemPath = clientPemPath;
	}

	public String getCaPemPath() {
		return this.caPemPath;
	}

	public void setCaPemPath(String caPemPath) {
		this.caPemPath = caPemPath;
	}

	public String getServerPemPath() {
		return this.serverPemPath;
	}

	public void setServerPemPath(String serverPemPath) {
		this.serverPemPath = serverPemPath;
	}

	public String getServerName() {
		return this.serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public boolean isSecure() {
		return this.secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public long getIdleTimeoutMs() {
		return this.idleTimeoutMs;
	}

	public void setIdleTimeoutMs(long idleTimeoutMs) {
		this.idleTimeoutMs = idleTimeoutMs;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
