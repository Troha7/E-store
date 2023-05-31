package com.estore.security;

import com.estore.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        log.info("Start find User by username");
        Set<GrantedAuthority> roles = new HashSet<>();

        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("username=" + username + " wasn't found")))
                .doOnError(user -> log.error("username={} wasn't found", username))
                .map(userEntity -> {
                    roles.add(new SimpleGrantedAuthority(userEntity.getRole().name()));
                    User user = new User(userEntity.getUsername(), userEntity.getPassword(), roles);
                    log.info("User {} has been found", user);
                    return user;
                });
    }
}
