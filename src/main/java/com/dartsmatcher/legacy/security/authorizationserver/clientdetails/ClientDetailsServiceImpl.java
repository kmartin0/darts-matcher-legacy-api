package com.dartsmatcher.legacy.security.authorizationserver.clientdetails;

import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

@Service
public class ClientDetailsServiceImpl implements ClientDetailsService {

	private final ClientDetailsRepository clientDetailsRepository;

	public ClientDetailsServiceImpl(ClientDetailsRepository clientDetailsRepository) {
		this.clientDetailsRepository = clientDetailsRepository;
	}

	@Override
	public ClientDetails loadClientByClientId(String s) throws ClientRegistrationException {

		return clientDetailsRepository.findByClientId(s).orElseThrow(() -> new InvalidGrantException("Bad credentials"));
	}
}
