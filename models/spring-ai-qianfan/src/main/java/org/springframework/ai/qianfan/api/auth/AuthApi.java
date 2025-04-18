package org.springframework.ai.qianfan.api.auth;

public abstract class AuthApi {

	private final QianFanAuthenticator authenticator;

	private QianFanAccessToken token;

	protected AuthApi(String apiKey, String secretKey) {
		this.authenticator = QianFanAuthenticator.builder().apiKey(apiKey).secretKey(secretKey).build();
	}

	protected String getAccessToken() {
		if (this.token == null || this.token.needsRefresh()) {
			this.token = this.authenticator.requestToken();
		}
		return this.token.getAccessToken();
	}

}
