package top.lll44556.gulimall.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;

import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
@Slf4j
public class LoginFailureHandler implements ServerAuthenticationFailureHandler {
    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange,
                                              AuthenticationException ex) {

        ServerWebExchange exchange = webFilterExchange.getExchange();
        var req  = exchange.getRequest();
        var resp = exchange.getResponse();

        // 1) 打日志：别只打 message，要把堆栈也打出来
        log.warn("OAuth2 login FAILED: method={}, path={}, query={}, remote={}, xff={}, ua={}, errType={}, errMsg={}",
                req.getMethod(),
                req.getPath().value(),
                req.getURI().getRawQuery(),
                req.getRemoteAddress(),
                req.getHeaders().getFirst("X-Forwarded-For"),
                req.getHeaders().getFirst(HttpHeaders.USER_AGENT),
                ex.getClass().getName(),
                ex.getMessage(),
                ex
        );

        // 2) 返回 401 + JSON（排错友好）
        resp.setStatusCode(HttpStatus.UNAUTHORIZED);
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        resp.getHeaders().setCacheControl("no-store");
        resp.getHeaders().setPragma("no-cache");

        String body = "{\"code\":401,\"msg\":\"oauth2_login_failed\",\"detail\":\"" + safe(ex.getMessage()) + "\"}";
        DataBuffer buffer = resp.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return resp.writeWith(Mono.just(buffer));
    }


    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }
}
