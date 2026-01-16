package top.lll44556.gulimall.gateway.config;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@AllArgsConstructor
public class LogoutSuccessHandler implements ServerLogoutSuccessHandler {
    private static final URI DEFAULT_REDIRECT = URI.create("/");

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange,
                                      Authentication authentication) {

        var webExchange = exchange.getExchange();
        var request = webExchange.getRequest();
        var response = webExchange.getResponse();

        String redirect = request.getQueryParams().getFirst("redirect");

        URI target = validateRedirect(redirect);

        response.setStatusCode(HttpStatus.FOUND);
        response.getHeaders().setLocation(target);
        return response.setComplete();
    }

    /**
     * redirect 安全校验（非常重要）
     */
    private URI validateRedirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            return DEFAULT_REDIRECT;
        }

        // 只允许站内跳转（强烈推荐）
        if (redirect.startsWith("/")) {
            return URI.create(redirect);
        }

        // ❌ 禁止外部地址，防止 open redirect
        return DEFAULT_REDIRECT;
    }
}
