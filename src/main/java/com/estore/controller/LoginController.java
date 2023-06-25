package com.estore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

/**
 * {@link LoginController} is a class for making controller for Thymeleaf.
 *
 * @author Dmytro Trotsenko on 5/31/23
 */

@Controller
@RequestMapping("/login")
public class LoginController {

    @GetMapping
    public Mono<String> login(@RequestParam(required = false) Boolean error, Model model) {
        model.addAttribute("loginErr", error);
        return Mono.just("login");
    }

}
