package com.estore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * {@link HomeController} is a class for making controller for Thymeleaf.
 *
 * @author Dmytro Trotsenko on 5/14/23
 */

@Controller
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public Mono<String> home(Model model) {
        model.addAttribute("message", "Welcome to E-Store!");
        return Mono.just("main/home");
    }

}

