package com.estore.controller;

import com.estore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * {@link AccountController} is a class for making controller for Thymeleaf.
 *
 * @author Dmytro Trotsenko on 5/14/23
 */

@Controller
@RequestMapping("account")
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    @GetMapping
    public Mono<String> account(Model model) {
        return getAuthentication()
                .flatMap(auth -> userService.findByUsername(auth.getName()))
                .doOnNext(user -> model.addAttribute("user", user))
                .then(Mono.just("main/account"));
    }

    private static Mono<Authentication> getAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication);
    }

}

