package com.smartstore.smartstore.controller;

import com.smartstore.smartstore.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@Slf4j
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String home(Model model) {
        log.debug("Loading home page");
        model.addAttribute("recentProducts", productService.getRecentProducts());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
