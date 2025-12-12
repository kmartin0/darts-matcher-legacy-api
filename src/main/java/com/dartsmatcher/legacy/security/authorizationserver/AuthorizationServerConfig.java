package com.dartsmatcher.legacy.security.authorizationserver;

import com.dartsmatcher.legacy.security.authorizationserver.clientdetails.ClientDetailsServiceImpl;
import com.dartsmatcher.legacy.security.authorizationserver.userdetails.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.security.KeyPair;
import java.util.Arrays;

@Configuration
@Import(AuthorizationServerEndpointsConfiguration.class)
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	private final UserDetailsServiceImpl userDetailsService;

	private final ClientDetailsServiceImpl clientDetailsService;

	private final AuthenticationManager authenticationManager;

	private final KeyPair keyPair;

	public AuthorizationServerConfig(UserDetailsServiceImpl userDetailsService, ClientDetailsServiceImpl clientDetailsService, AuthenticationManager authenticationManager, KeyPair keyPair) {
		this.userDetailsService = userDetailsService;
		this.clientDetailsService = clientDetailsService;
		this.authenticationManager = authenticationManager;
		this.keyPair = keyPair;
	}

	/**
	 * Creates an JwtAccessTokenConverter which encodes and decodes the jwt using our signing key.
	 * Also encodes/decodes the user data inside the token.
	 */
	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		// Adds the jwt signing key
		final JwtAccessTokenConverter accessTokenConverter = new JwtAccessTokenConverter();
		accessTokenConverter.setKeyPair(keyPair);

		return accessTokenConverter;
	}

	/**
	 * Creates a JwtTokenStore that provides support for verifying a jwt signature.
	 */
	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	/**
	 * Configure the authorization server to authenticate clients using ClientDetailsServiceImpl.
	 */
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.withClientDetails(clientDetailsService);
	}

	@Bean
	public TokenEnhancer tokenEnhancer() {
		return new CustomTokenEnhancer();
	}

	@Bean
	public TokenEnhancerChain tokenEnhancerChain() {
		TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
		enhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));

		return enhancerChain;
	}

	/**
	 * Configure the authorization server endpoints with a tokenStore, authenticationManager,
	 * accessTokenConverter and userDetailsService.
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.tokenStore(tokenStore())
				.accessTokenConverter(accessTokenConverter())
				.tokenEnhancer(tokenEnhancerChain())
				.authenticationManager(authenticationManager)
				.userDetailsService(userDetailsService)
				.reuseRefreshTokens(false);
	}
}
