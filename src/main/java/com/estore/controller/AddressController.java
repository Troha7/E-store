package com.estore.controller;

import com.estore.dto.request.AddressRequestDto;
import com.estore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;


/**
 * {@link AddressController} is a class for making controller for Thymeleaf.
 *
 * @author Dmytro Trotsenko on 5/31/23
 */

@Controller
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final UserService userService;

    @GetMapping
    public Mono<String> address(Model model) {
        model.addAttribute("addressDto", new AddressRequestDto());
        return Mono.just("main/address");
    }

    @PostMapping
    public Mono<String> addAddress(@Validated @ModelAttribute("addressDto") AddressRequestDto address, Errors errors, Model model) {
        if (errors != null && errors.hasErrors()) {
            return Mono.just("main/address");
        }
        return getAuthentication()
                .flatMap(auth -> userService.findByUsername(auth.getName()))
                .flatMap(user -> userService.addAddress(user.getId(), address))
                .map(order -> "redirect:/account")
                .onErrorResume(throwable -> {
                    model.addAttribute("err", throwable.getMessage());
                    return Mono.just("maim/account");
                });
    }

    private static Mono<Authentication> getAuthentication() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication);
    }

}
