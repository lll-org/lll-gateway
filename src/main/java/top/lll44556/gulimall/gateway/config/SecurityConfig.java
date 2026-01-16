package top.lll44556.gulimall.gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.WebSessionServerLogoutHandler;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public ServerRequestCache serverRequestCache() {
        return new WebSessionServerRequestCache();
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            FixedRedirectSuccessHandler fixedRedirectSuccessHandler,
            LogoutSuccessHandler LogoutSuccessHandler,
            TokenRevokingLogoutHandler tokenRevokingLogoutHandler
    ) throws Exception {

        DelegatingServerAuthenticationEntryPoint delegating = getDelegatingServerAuthenticationEntryPoint();

        http.authorizeExchange((authorize) ->
                authorize
                        .pathMatchers("/oauth2/**").permitAll()
                        .pathMatchers("/logout").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().authenticated()
        );
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.exceptionHandling(exceptions ->
                exceptions.authenticationEntryPoint(delegating));
        http.requestCache(spec -> spec.requestCache(NoOpServerRequestCache.getInstance()));
        http.oauth2Login(
                oAuth2LoginSpec ->
                        oAuth2LoginSpec
                        .authenticationSuccessHandler(fixedRedirectSuccessHandler)
                        .authenticationFailureHandler(new LoginFailureHandler())
        );

        DelegatingServerLogoutHandler delegatingServerLogoutHandler = new DelegatingServerLogoutHandler(
                new SecurityContextServerLogoutHandler(),   // 默认：清 SecurityContext
                new WebSessionServerLogoutHandler(),        // 默认：清 WebSession
                tokenRevokingLogoutHandler                 // 你的：先撤销 token / 删除 authorized client
        );

        http.logout(logoutSpec ->
                logoutSpec
                        .logoutUrl("/logout")
                        .logoutHandler(delegatingServerLogoutHandler)
                        .logoutSuccessHandler(LogoutSuccessHandler)
        );
        return http.build();
    }

    private static DelegatingServerAuthenticationEntryPoint getDelegatingServerAuthenticationEntryPoint() {
        ServerWebExchangeMatcher apiMatcher = ServerWebExchangeMatchers.pathMatchers("/api/**");
        HttpStatusServerEntryPoint apiEntryPoint = new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED);
        RedirectServerAuthenticationEntryPoint oauth2EntryPoint = new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/oidc-client");

        DelegatingServerAuthenticationEntryPoint delegating = new DelegatingServerAuthenticationEntryPoint(
                List.of(new DelegatingServerAuthenticationEntryPoint.DelegateEntry(apiMatcher, apiEntryPoint))
        );
        delegating.setDefaultEntryPoint(oauth2EntryPoint);
        return delegating;
    }


    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
        ReactiveOAuth2AuthorizedClientProvider oAuth2AuthorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .clientCredentials()
                .build();
        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager = new DefaultReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientRepository
        );
        authorizedClientManager.setAuthorizedClientProvider(oAuth2AuthorizedClientProvider);
        return authorizedClientManager;
    }
}

