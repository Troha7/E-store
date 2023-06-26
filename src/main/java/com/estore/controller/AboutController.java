package com.estore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * {@link AboutController} is a class for making controller for Thymeleaf.
 *
 * @author Dmytro Trotsenko on 5/14/23
 */

@Controller
@RequestMapping("about")
public class AboutController {

    @GetMapping
    public Mono<String> about() {
        return Mono.just("main/about");
    }

}

