package com.dartsmatcher.legacy.security.authorizationserver.clientdetails;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.ClientDetails;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "client_details")
@TypeAlias("ClientDetailsImpl")
public class ClientDetailsImpl implements ClientDetails {

	private String clientId;
	private String clientSecret;
	private String scope;
	private String authorizedGrantTypes;
	private String authorities;
	private int accessTokenValidity;
	private int refreshTokenValidity;

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public Set<String> getResourceIds() {
		return null;
	}

	@Override
	public boolean isSecretRequired() {
		return true;
	}

	@Override
	public String getClientSecret() {
		return clientSecret;
	}

	@Override
	public boolean isScoped() {
		return false;
	}

	@Override
	public Set<String> getScope() {
		if (scope == null) return null;
		return new HashSet<>(Arrays.asList(scope.split(",")));
	}

	@Override
	public Set<String> getAuthorizedGrantTypes() {
		if (authorizedGrantTypes == null) return null;
		return new HashSet<>(Arrays.asList(authorizedGrantTypes.split(",")));
	}

	@Override
	public Set<String> getRegisteredRedirectUri() {
		return null;
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		if (authorities == null) return null;
		String[] authoritiesValues = authorities.split(",");
		ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		for(String authority : authoritiesValues) {
			grantedAuthorities.add(new SimpleGrantedAuthority(authority));
		}
		return grantedAuthorities;
	}

	@Override
	public Integer getAccessTokenValiditySeconds() {
		return accessTokenValidity;
	}

	@Override
	public Integer getRefreshTokenValiditySeconds() {
		return refreshTokenValidity;
	}

	@Override
	public boolean isAutoApprove(String s) {
		return false;
	}

	@Override
	public Map<String, Object> getAdditionalInformation() {
		return null;
	}
}
