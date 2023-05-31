package com.estore.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

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
        return new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange()
                .pathMatchers("/", "/webjars/swagger-ui/**", "/bus/v3/api-docs/**").permitAll()
                .pathMatchers("/catalog").hasRole("ADMIN")
                .anyExchange().authenticated()
                .and()
                .httpBasic()
                .and()
                .formLogin()
//                .loginPage("/login")
                .and()
                .csrf().disable()
                .build();
    }

}
