package com.estore.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.net.URI;

/**
 * {@link WebSecurityConfig}
 *
 * @author Dmytro Trotsenko on 5/20/23
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final ReactiveUserDetailsService reactiveUserDetailsService;

    private static final String[] WHITELIST_URLS = {"/", "/catalog", "/webjars/swagger-ui/**", "/bus/v3/api-docs/**"};

    /**
     * Password encoder bean is designed to password encryption
     *
     * @return encrypted password
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        var userAuthManager = new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService);
        userAuthManager.setPasswordEncoder(passwordEncoder());
        return userAuthManager;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveAuthenticationManager authenticationManager) {
        return http
                .authenticationManager(authenticationManager)
                .authorizeExchange()
                .pathMatchers(WHITELIST_URLS).permitAll()
                .pathMatchers("/login", "/registration").permitAll()
                .pathMatchers( "/admin/**", "/catalog/addProduct/**").hasAuthority("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/products/**").hasAuthority("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/products/**").hasAuthority("ADMIN")
                .anyExchange().authenticated()
                .and()
                .httpBasic()
                .and()
                .formLogin()
                .loginPage("/login")
                .authenticationFailureHandler((exchange, authentication) -> new DefaultServerRedirectStrategy()
                        .sendRedirect(exchange.getExchange(), URI.create("/login?error=true")))
                .and()
                .csrf().disable()
                .build();
    }

}
