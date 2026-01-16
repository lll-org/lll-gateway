package top.lll44556.gulimall.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class FixedRedirectSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final URI landingUri = URI.create("http://home.lll44556.top/auth-landing");

    public FixedRedirectSuccessHandler() {
    }

//    public FixedRedirectSuccessHandler(
//            @Value("${app.auth.landing-uri:http://localhost:5173/auth-landing}")
//            String landingUri
//    ) {
//        this.landingUri = URI.create(landingUri);
//    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        var exchange = webFilterExchange.getExchange();
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(landingUri);
        return exchange.getResponse().setComplete();
    }
}
