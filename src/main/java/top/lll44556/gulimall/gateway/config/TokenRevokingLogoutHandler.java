package top.lll44556.gulimall.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class TokenRevokingLogoutHandler implements ServerLogoutHandler {

    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient;
    private final URI revocationEndpointUri = URI.create("http://oauth2-server:10070/oauth2/revoke");
    private final String clientId;
    private final String clientSecret;

    public TokenRevokingLogoutHandler(
            ReactiveOAuth2AuthorizedClientService authorizedClientService,
            WebClient webClient,
            @Value("${spring.security.oauth2.client.registration.oidc-client.client-id}")
            String clientId,
            @Value("${spring.security.oauth2.client.registration.oidc-client.client-secret}")
            String clientSecret
    ) {
        this.authorizedClientService = authorizedClientService;
        this.webClient = webClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public Mono<Void> logout(WebFilterExchange exchange, Authentication authentication) {
        if (!(authentication instanceof OAuth2AuthenticationToken oAuth2AuthenticationToken)) {
            return Mono.empty();
        }
        String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
        String principalName = oAuth2AuthenticationToken.getName();
        return authorizedClientService.loadAuthorizedClient(registrationId, principalName)
                .flatMap(client -> revokeIfPossible(client)
                        .then(authorizedClientService.removeAuthorizedClient(registrationId, principalName)))
                .onErrorResume(e -> authorizedClientService.removeAuthorizedClient(registrationId, principalName))
                .onErrorResume(e -> Mono.empty())
                .then();
    }


    private Mono<Void> revokeIfPossible(OAuth2AuthorizedClient client) {
        // 优先撤 refresh token（最有用）
        if (client.getRefreshToken() == null) {
            // 没有 refresh token，就别硬撤了（可选：撤 access token）
            return Mono.empty();
        }

        String token = client.getRefreshToken().getTokenValue();

        return webClient.post()
                .uri(revocationEndpointUri)
                .headers(h -> h.setBasicAuth(clientId, clientSecret))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("token", token)
                        .with("token_type_hint", "refresh_token"))
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}
