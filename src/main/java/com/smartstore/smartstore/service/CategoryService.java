package com.smartstore.smartstore.service;

import com.smartstore.smartstore.entity.Category;
import com.smartstore.smartstore.exception.ResourceNotFoundException;
import com.smartstore.smartstore.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        log.debug("Fetching category with id: {}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));
    }

    public Category createCategory(Category category) {
        log.info("Creating category: {}", category.getName());
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException(
                    "A category with this name already exists: " + category.getName());
        }
        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", saved.getId());
        return saved;
    }

    public Category updateCategory(Long id, Category updatedData) {
        log.info("Updating category: {}", id);
        Category existingCategory = getCategoryById(id);

        if (!existingCategory.getName().equals(updatedData.getName())
                && categoryRepository.existsByName(updatedData.getName())) {
            throw new IllegalArgumentException(
                    "A category with this name already exists: " + updatedData.getName());
        }

        existingCategory.setName(updatedData.getName());
        existingCategory.setDescription(updatedData.getDescription());

        Category updated = categoryRepository.save(existingCategory);
        log.info("Category updated successfully: {}", id);
        return updated;
    }

    public void deleteCategory(Long id) {
        log.info("Deleting category: {}", id);
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
        log.info("Category deleted successfully: {}", id);
    }
}
