package org.springframework.ai.qianfan.api.auth;

public class QianFanAccessToken {

	private static final Double FRACTION_OF_TIME_TO_LIVE = 0.8D;

	private final String accessToken;

	private final String refreshToken;

	private final Long expiresIn;

	private final String sessionKey;

	private final String sessionSecret;

	private final String scope;

	private final Long refreshTime;

	public QianFanAccessToken(AccessTokenResponse accessTokenResponse) {
		this.accessToken = accessTokenResponse.accessToken();
		this.refreshToken = accessTokenResponse.refreshToken();
		this.expiresIn = accessTokenResponse.expiresIn();
		this.sessionKey = accessTokenResponse.sessionKey();
		this.sessionSecret = accessTokenResponse.sessionSecret();
		this.scope = accessTokenResponse.scope();
		this.refreshTime = getCurrentTimeInSeconds() + (long) ((double) this.expiresIn * FRACTION_OF_TIME_TO_LIVE);
	}

	public String getAccessToken() {
		return this.accessToken;
	}

	public String getRefreshToken() {
		return this.refreshToken;
	}

	public Long getExpiresIn() {
		return this.expiresIn;
	}

	public String getSessionKey() {
		return this.sessionKey;
	}

	public String getSessionSecret() {
		return this.sessionSecret;
	}

	public Long getRefreshTime() {
		return this.refreshTime;
	}

	public String getScope() {
		return this.scope;
	}

	public synchronized boolean needsRefresh() {
		return getCurrentTimeInSeconds() >= this.refreshTime;
	}

	private long getCurrentTimeInSeconds() {
		return System.currentTimeMillis() / 1000L;
	}

}
