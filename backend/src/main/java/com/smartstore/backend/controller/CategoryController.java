package com.smartstore.backend.controller;

import com.smartstore.backend.dto.*;
import com.smartstore.backend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<?> getCategories(
            @RequestParam(defaultValue = "false") boolean all,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        if (all) {
            return ResponseEntity.ok(categoryService.getAllFlatList());
        }
        return ResponseEntity.ok(categoryService.getAll(pageable));
    }

    @GetMapping("/tree")
    public ResponseEntity<List<CategoryTreeResponse>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getTree());
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResponse>> getDirectChildren(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getDirectChildren(id));
    }

    @GetMapping("/{id}/parent")
    public ResponseEntity<CategoryResponse> getParent(@PathVariable Long id) {
        return categoryService.getParent(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CategoryResponse>> searchByName(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.searchByName(q, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteSubtreeIteratively(id);
        return ResponseEntity.noContent().build();
    }
}