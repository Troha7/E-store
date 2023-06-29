package com.estore.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import static org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME;

/**
 * {@link WebSecurityService}
 *
 * @author Dmytro Trotsenko on 6/29/23
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSecurityService {

    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Automatically authorization after registration
     *
     * @param username username
     * @param password password
     * @param exchange represents the current HTTP request and response
     */
    public Mono<Void> autoLogin(String username, String password, ServerWebExchange exchange) {
        log.info("Start auto login {}", username);
        return customUserDetailsService.findByUsername(username)
                .map(userDetails -> new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities()))
                .map(authToken -> {
                    SecurityContextImpl securityContext = new SecurityContextImpl();
                    securityContext.setAuthentication(authToken);
                    return securityContext;
                })
                .flatMap(context -> exchange.getSession()
                        .doOnNext(sesion -> sesion.getAttributes()
                                .put(DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME, context)))
                .flatMap(WebSession::changeSessionId)
                .doOnSuccess(res -> log.info("Auto login {} successful!", username));
    }

}
