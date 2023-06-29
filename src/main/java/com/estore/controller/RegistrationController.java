package com.estore.controller;

import com.estore.dto.request.UserRequestDto;
import com.estore.security.WebSecurityService;
import com.estore.service.OrderService;
import com.estore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * {@link RegistrationController} is a class for making controller for Thymeleaf.
 *
 * @author Dmytro Trotsenko on 5/31/23
 */

@Controller
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;
    private final OrderService orderService;
    private final WebSecurityService webSecurityService;

    @GetMapping
    public Mono<String> registration(Model model) {
        model.addAttribute("userDto", new UserRequestDto());
        return Mono.just("registration");
    }

    @PostMapping
    public Mono<String> createUser(@Validated @ModelAttribute("userDto") UserRequestDto user, Errors errors, Model model, ServerWebExchange exchange) {
        if (errors != null && errors.hasErrors()) {
            return Mono.just("registration");
        }
        return userService.createUser(user)
                .flatMap(newUser -> orderService.create(newUser.getId()))
                .then(webSecurityService.autoLogin(user.getUsername(), user.getPassword(), exchange))
                .then(Mono.just("redirect:/"))
                .onErrorResume(throwable -> {
                    model.addAttribute("err", throwable.getMessage());
                    return Mono.just("registration");
                });
    }

}
