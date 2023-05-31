package com.estore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * {@link StoreController} is a class for making restful web service.
 *
 * @author Dmytro Trotsenko on 5/14/23
 */

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class StoreController {

    @GetMapping
    public Mono<String> home(Model model) {
        model.addAttribute("message", "Welcome to E-Store!");

        // Return the name of the Thymeleaf template to render
        return Mono.just("main/home");
    }
}

