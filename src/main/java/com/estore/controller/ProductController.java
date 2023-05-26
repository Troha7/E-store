package com.estore.controller;

import com.estore.dto.request.ProductRequestDto;
import com.estore.dto.response.ProductResponseDto;
import com.estore.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Flux;

/**
 * {@link ProductController}
 *
 * @author Dmytro Trotsenko on 5/16/23
 */

@Controller
@RequestMapping("catalog")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String getProducts(final Model model) {
        final Flux<ProductResponseDto> all = productService.findAll();

        model.addAttribute("products", all);
        model.addAttribute("product", new ProductRequestDto());

        return"main/catalog";
    }

}
