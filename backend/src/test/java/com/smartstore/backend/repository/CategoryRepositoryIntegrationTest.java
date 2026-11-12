package com.smartstore.backend.repository;

import com.smartstore.backend.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("التحقق من تفرد أسماء الجذور عبر العمود المشتق Virtual Generated Column")
    void uniqueConstraint_RootNames_EnforcedByDatabase() {
        LocalDateTime now = LocalDateTime.now();

        Category root1 = new Category("المشروبات", "الوصف الأول", null);
        root1.setCreatedAt(now);
        root1.setUpdatedAt(now);
        categoryRepository.saveAndFlush(root1);

        Category root2 = new Category("المشروبات", "الوصف الثاني", null);
        root2.setCreatedAt(now);
        root2.setUpdatedAt(now);

        assertThatThrownBy(() -> categoryRepository.saveAndFlush(root2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("السماح بنفس الاسم تحت آباء مختلفين")
    void uniqueConstraint_SameNameUnderDifferentParents_Allowed() {
        LocalDateTime now = LocalDateTime.now();

        Category root1 = new Category("المشروبات", null, null);
        root1.setCreatedAt(now);
        root1.setUpdatedAt(now);
        categoryRepository.save(root1);

        Category root2 = new Category("الأغذية", null, null);
        root2.setCreatedAt(now);
        root2.setUpdatedAt(now);
        categoryRepository.save(root2);

        Category child1 = new Category("عروض خاصة", null, root1);
        child1.setCreatedAt(now);
        child1.setUpdatedAt(now);

        Category child2 = new Category("عروض خاصة", null, root2);
        child2.setCreatedAt(now);
        child2.setUpdatedAt(now);

        categoryRepository.save(child1);
        categoryRepository.saveAndFlush(child2);

        assertThat(child1.getId()).isNotNull();
        assertThat(child2.getId()).isNotNull();
    }

    @Test
    @DisplayName("التحقق من البحث المتوافق مع الترميز وتجميع اللغات (utf8mb4_0900_ai_ci)")
    void findByNameContainingIgnoreCase_CollationTest() {
        LocalDateTime now = LocalDateTime.now();
        Category cat = new Category("Jus d'orange", "عصير برتقال", null);
        cat.setCreatedAt(now);
        cat.setUpdatedAt(now);
        categoryRepository.saveAndFlush(cat);

        List<Category> results = categoryRepository.findByNameContainingIgnoreCase("jus", null).getContent();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Jus d'orange");
    }
}