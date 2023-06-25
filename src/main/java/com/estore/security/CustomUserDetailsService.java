package com.estore.security;

import com.estore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link CustomUserDetailsService}
 *
 * @author Dmytro Trotsenko on 5/20/23
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    @Value("${superuser.username}")
    private String superuserUsername;
    @Value("${superuser.password}")
    private String superuserPassword;
    @Value("${superuser.role}")
    private String superuserRole;

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        log.info("Start find User by username");

        if (username.equals(superuserUsername)) {
            log.info("Superuser {} is log in", superuserUsername);
            return getSuperuser(superuserUsername, superuserPassword, superuserRole);
        }

        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("username=" + username + " wasn't found")))
                .doOnError(user -> log.error("username={} wasn't found", username))
                .map(userEntity -> {
                    Set<GrantedAuthority> roles = new HashSet<>();
                    roles.add(new SimpleGrantedAuthority(userEntity.getRole().name()));
                    User user = new User(userEntity.getUsername(), userEntity.getPassword(), roles);
                    log.info("User {} has been found", user);
                    return user;
                });
    }

    private Mono<UserDetails> getSuperuser(String username, String password, String role) {
        Set<GrantedAuthority> roles = new HashSet<>();
        roles.add(new SimpleGrantedAuthority(role));
        UserDetails adminUser = User.withUsername(username)
                .password(password)
                .authorities(roles)
                .build();
        return Mono.just(adminUser);
    }

}
