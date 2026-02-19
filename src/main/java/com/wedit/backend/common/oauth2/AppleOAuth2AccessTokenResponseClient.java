package com.wedit.backend.common.oauth2;

import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

@Component
public class AppleOAuth2AccessTokenResponseClient
        implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final RestClientAuthorizationCodeTokenResponseClient delegate;

    public AppleOAuth2AccessTokenResponseClient(AppleClientSecretGenerator generator) {
        this.delegate = new RestClientAuthorizationCodeTokenResponseClient();

        DefaultOAuth2TokenRequestParametersConverter<OAuth2AuthorizationCodeGrantRequest> defaultConverter =
                new DefaultOAuth2TokenRequestParametersConverter<>();

        this.delegate.setParametersConverter(request -> {
            MultiValueMap<String, String> params = defaultConverter.convert(request);
            if ("apple".equals(request.getClientRegistration().getRegistrationId())) {
                params.set("client_secret", generator.generate());
            }
            return params;
        });
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {
        return delegate.getTokenResponse(request);
    }
}
