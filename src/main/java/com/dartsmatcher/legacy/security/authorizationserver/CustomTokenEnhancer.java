package com.dartsmatcher.legacy.security.authorizationserver;

import com.dartsmatcher.legacy.security.authorizationserver.userdetails.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

public class CustomTokenEnhancer implements TokenEnhancer {
	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken oAuth2AccessToken, OAuth2Authentication oAuth2Authentication) {
		Map<String, Object> info = new HashMap<>();

		Authentication authenticationToken = oAuth2Authentication.getUserAuthentication();
		UserPrincipal userPrincipal = (UserPrincipal) authenticationToken.getPrincipal();

		info.put("sub", userPrincipal.getUsername());
		((DefaultOAuth2AccessToken) oAuth2AccessToken).setAdditionalInformation(info);

		return oAuth2AccessToken;
	}
}
