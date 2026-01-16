//package top.lll44556.gulimall.gateway.filter;
//
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//
//
//@Component
//@Order(0)
//public class TokenRelayFilter implements GlobalFilter {
//
//    private final ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
//
//    public TokenRelayFilter(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
//        this.authorizedClientRepository = authorizedClientRepository;
//    }
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        return exchange.getPrincipal()
//                .filter(OAuth2AuthenticationToken.class::isInstance)
//                .cast(OAuth2AuthenticationToken.class)
//                .flatMap(auth -> authorizedClientRepository
//                        .loadAuthorizedClient(auth.getAuthorizedClientRegistrationId(), auth, exchange))
//                .map(client -> ((OAuth2AuthorizedClient) client).getAccessToken().getTokenValue())
//                .flatMap(token -> {
//                    var mutated = exchange.mutate().request(
//                            exchange.getRequest().mutate()
//                                    .header("Authorization", "Bearer " + token)
//                                    .build()
//                    ).build();
//                    return chain.filter(mutated);
//                })
//                .switchIfEmpty(chain.filter(exchange));
//    }
//}
