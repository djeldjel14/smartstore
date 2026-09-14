package com.smartstore.smartstore.controller;

import com.smartstore.smartstore.dto.ApiResponse;
import com.smartstore.smartstore.dto.CategoryDTO;
import com.smartstore.smartstore.entity.Category;
import com.smartstore.smartstore.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@Slf4j
public class CategoryRestController {

    private final CategoryService categoryService;

    public CategoryRestController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        log.debug("Fetching all categories");
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(categoryDTOs));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable Long id) {
        log.debug("Fetching category: {}", id);
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(new CategoryDTO(category)));
    }
}
