package com.smartstore.smartstore.service;

import com.smartstore.smartstore.entity.Category;
import com.smartstore.smartstore.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Constructor Injection
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Get all categories
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Get one category by id
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Category not found with id: " + id));
    }

    // Create a new category
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException(
                    "A category with this name already exists: " + category.getName());
        }
        return categoryRepository.save(category);
    }

    // Update an existing category
    @Transactional
    public Category updateCategory(Long id, Category updatedData) {
        Category existingCategory = getCategoryById(id);

        // If the name is changing, make sure the new name isn't already taken
        if (!existingCategory.getName().equals(updatedData.getName())
                && categoryRepository.existsByName(updatedData.getName())) {
            throw new IllegalArgumentException(
                    "A category with this name already exists: " + updatedData.getName());
        }

        existingCategory.setName(updatedData.getName());
        existingCategory.setDescription(updatedData.getDescription());

        return categoryRepository.save(existingCategory);
    }

    // Delete a category
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }
}