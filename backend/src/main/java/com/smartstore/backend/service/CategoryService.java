package com.smartstore.backend.service;

import com.smartstore.backend.entity.Category;
import com.smartstore.backend.dto.*;
import com.smartstore.backend.exception.CategoryNotFoundException;
import com.smartstore.backend.exception.DuplicateCategoryException;
import com.smartstore.backend.exception.InvalidHierarchyException;
import com.smartstore.backend.repository.CategoryRepository;
import com.smartstore.backend.validation.CategoryTextRules;
import com.smartstore.backend.validation.UnicodeText;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final Clock clock;

    public CategoryService(CategoryRepository categoryRepository, Clock clock) {
        this.categoryRepository = categoryRepository;
        this.clock = clock;
    }

    public CategoryResponse create(CategoryCreateRequest request) {
        String normalizedName = normalizeName(request.name());
        String normalizedDescription = normalizeDescription(request.description());

        Category parent = null;
        if (request.parentId() != null) {
            parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Parent category with ID " + request.parentId() + " not found."));
        }

        Long parentId = parent != null ? parent.getId() : null;
        if (categoryRepository.existsByNameAndParentId(normalizedName, parentId)) {
            throw new DuplicateCategoryException("A category with the name '" + normalizedName + "' already exists under this parent.");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        Category category = new Category(normalizedName, normalizedDescription, parent);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = findCategoryById(id);
        return mapToResponse(category);
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllFlatList() {
        return categoryRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getTree() {
        List<Category> allCategories = categoryRepository.findAll();
        Map<Long, CategoryTreeResponse> nodeMap = new HashMap<>();
        List<CategoryTreeResponse> roots = new ArrayList<>();

        for (Category cat : allCategories) {
            nodeMap.put(cat.getId(), new CategoryTreeResponse(
                    cat.getId(), cat.getName(), cat.getDescription(),
                    cat.getParent() != null ? cat.getParent().getId() : null,
                    cat.getCreatedAt(), cat.getUpdatedAt(), new ArrayList<>()
            ));
        }

        for (Category cat : allCategories) {
            CategoryTreeResponse currentNode = nodeMap.get(cat.getId());
            if (cat.getParent() == null) {
                roots.add(currentNode);
            } else {
                CategoryTreeResponse parentNode = nodeMap.get(cat.getParent().getId());
                if (parentNode != null) {
                    parentNode.children().add(currentNode);
                }
            }
        }

        return roots;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getDirectChildren(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category with ID " + id + " not found.");
        }
        return categoryRepository.findByParentId(id).stream().map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<CategoryResponse> getParent(Long id) {
        Category category = findCategoryById(id);
        return Optional.ofNullable(category.getParent()).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchByName(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank.");
        }
        return categoryRepository.findByNameContainingIgnoreCase(query.trim(), pageable).map(this::mapToResponse);
    }

    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        Category category = findCategoryById(id);

        String normalizedName = normalizeName(request.name());
        String normalizedDescription = normalizeDescription(request.description());

        Long newParentId = request.parentId();
        Category newParent = null;

        if (newParentId != null) {
            if (newParentId.equals(id)) {
                throw new IllegalArgumentException("A category cannot be its own parent.");
            }
            newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> new CategoryNotFoundException("Parent category with ID " + newParentId + " not found."));

            validateNoCycle(id, newParent);
        }

        if (categoryRepository.existsByNameAndParentIdAndIdNot(normalizedName, newParentId, id)) {
            throw new DuplicateCategoryException("A category with the name '" + normalizedName + "' already exists under the proposed parent.");
        }

        category.setName(normalizedName);
        category.setDescription(normalizedDescription);
        category.setParent(newParent);
        category.setUpdatedAt(LocalDateTime.now(clock));

        return mapToResponse(categoryRepository.save(category));
    }

    public void deleteSubtreeIteratively(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Category with ID " + id + " not found.");
        }

        List<List<Long>> levels = new ArrayList<>();
        List<Long> currentLevel = List.of(id);

        while (!currentLevel.isEmpty()) {
            levels.add(currentLevel);
            currentLevel = categoryRepository.findChildIdsByParentIds(currentLevel);
        }

        for (int i = levels.size() - 1; i >= 0; i--) {
            List<Long> levelIds = levels.get(i);
            categoryRepository.deleteByIdsInBatch(levelIds);
        }
    }

    private void validateNoCycle(Long categoryId, Category proposedParent) {
        Category ancestor = proposedParent;
        while (ancestor != null) {
            if (ancestor.getId().equals(categoryId)) {
                throw new InvalidHierarchyException("Cannot set parent: proposed parent is a descendant of this category.");
            }
            ancestor = ancestor.getParent();
        }
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category with ID " + id + " not found."));
    }

    private String normalizeName(String name) {
        String normalizedName = name == null ? "" : UnicodeText.trimWhitespace(name);
        int codePointLength = CategoryTextRules.codePointLength(normalizedName);
        if (codePointLength < CategoryTextRules.NAME_MIN_CODE_POINTS
                || codePointLength > CategoryTextRules.NAME_MAX_CODE_POINTS) {
            throw new IllegalArgumentException(
                    "Category name must contain between 1 and 100 Unicode code points.");
        }
        return normalizedName;
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String normalizedDescription = UnicodeText.trimWhitespace(description);
        if (normalizedDescription.isEmpty()) {
            return null;
        }
        if (CategoryTextRules.codePointLength(normalizedDescription)
                > CategoryTextRules.DESCRIPTION_MAX_CODE_POINTS) {
            throw new IllegalArgumentException("Description must not exceed 255 Unicode code points.");
        }
        return normalizedDescription;
    }

    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}