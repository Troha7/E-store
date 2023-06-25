package com.estore.controller;

import com.estore.dto.request.UserRequestDto;
import com.estore.model.UserRole;
import com.estore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * {@link AdminController} is a class for making controller for Thymeleaf.
 *
 * @author Dmytro Trotsenko on 5/14/23
 */

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping
    public Mono<String> admin(Model model) {
        return userService.findAll().collectList()
                .doOnNext(users -> model.addAttribute("users", users))
                .doOnNext(users -> model.addAttribute("user", new UserRequestDto()))
                .doOnNext(users -> model.addAttribute("roles", UserRole.values()))
                .then(Mono.just("main/admin"));
    }

}

