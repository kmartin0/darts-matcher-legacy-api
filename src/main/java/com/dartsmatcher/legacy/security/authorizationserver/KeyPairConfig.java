package com.dartsmatcher.legacy.security.authorizationserver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;

@Configuration
public class KeyPairConfig {

	@Value("${security.oauth2.authorization.jwt.key-store}")
	Resource keystoreClasspath;

	@Value("${security.oauth2.authorization.jwt.key-store-password}")
	String keystorePassword;

	@Value("${security.oauth2.authorization.jwt.key-alias}")
	String keystoreAlias;

	@Bean
	public KeyPair keyPairBean() {
		KeyStoreKeyFactory ksFactory = new KeyStoreKeyFactory(keystoreClasspath, keystorePassword.toCharArray());
		return ksFactory.getKeyPair(keystoreAlias);
	}
}
