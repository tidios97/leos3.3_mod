package eu.europa.ec.leos.integration.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

class TokenJson {

	private String accessToken;
	private String tokenType;
	private String refreshToken;
	private Long expiresIn;
	private String scope;
	private String state;

	@JsonCreator
	public TokenJson(@JsonProperty("access_token") String accessToken,
			@JsonProperty("refresh_token") String refreshToken, @JsonProperty("token_type") String tokenType,
			@JsonProperty("scope") String scope, @JsonProperty("state") String state,
			@JsonProperty("expires_in") long expiresIn) {
		this.accessToken = accessToken;
		this.tokenType = tokenType;
		this.refreshToken = refreshToken;
		this.expiresIn = expiresIn;
		this.scope = scope;
		this.state = state;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public Long getExpiresIn() {
		return expiresIn;
	}

	public String getScope() {
		return scope;
	}

	public String getState() {
		return state;
	}

}