package com.smartstore.backend.service;

import com.smartstore.backend.entity.Category;
import com.smartstore.backend.dto.CategoryCreateRequest;
import com.smartstore.backend.dto.CategoryResponse;
import com.smartstore.backend.dto.CategoryUpdateRequest;
import com.smartstore.backend.exception.CategoryNotFoundException;
import com.smartstore.backend.exception.DuplicateCategoryException;
import com.smartstore.backend.exception.InvalidHierarchyException;
import com.smartstore.backend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private Clock clock = Clock.fixed(Instant.parse("2026-11-10T10:00:00Z"), ZoneId.of("UTC"));

    @InjectMocks
    private CategoryService categoryService;

    private Category rootCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        rootCategory = new Category("المشروبات", "وصف المشروبات", null);
        setEntityId(rootCategory, 1L);

        childCategory = new Category("المشروبات الغازية", null, rootCategory);
        setEntityId(childCategory, 2L);
    }

    @Test
    @DisplayName("إنشاء فئة جذرية بنجاح وتطبيق التنظيف (Trimming)")
    void create_RootCategory_Success() {
        CategoryCreateRequest request = new CategoryCreateRequest("  المشروبات  ", "  وصف  ", null);

        when(categoryRepository.existsByNameAndParentId("المشروبات", null)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category cat = invocation.getArgument(0);
            setEntityId(cat, 1L);
            return cat;
        });

        CategoryResponse response = categoryService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("المشروبات");
        assertThat(response.description()).isEqualTo("وصف");
        assertThat(response.parentId()).isNull();
    }

    @Test
    @DisplayName("Unicode whitespace is trimmed while case, Arabic text, and internal spaces are preserved")
    void create_UnicodeWhitespace_TrimsOnlyBoundaries() {
        CategoryCreateRequest request = new CategoryCreateRequest(
                "\u00A0JuS  عصير\u202F",
                "\u0085وصف  داخلي\u3000",
                null
        );

        when(categoryRepository.existsByNameAndParentId("JuS  عصير", null)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            setEntityId(category, 1L);
            return category;
        });

        CategoryResponse response = categoryService.create(request);

        assertThat(response.name()).isEqualTo("JuS  عصير");
        assertThat(response.description()).isEqualTo("وصف  داخلي");
    }

    @Test
    @DisplayName("Update applies Unicode trimming and code point validation")
    void update_UnicodeText_NormalizesAndPreservesContent() {
        CategoryUpdateRequest request = new CategoryUpdateRequest("\u00A0JuS  عصير\u202F", "\u3000وصف\u0085", null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.existsByNameAndParentIdAndIdNot("JuS  عصير", null, 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.update(1L, request);

        assertThat(response.name()).isEqualTo("JuS  عصير");
        assertThat(response.description()).isEqualTo("وصف");
    }

    @Test
    @DisplayName("Unicode whitespace-only category names are rejected")
    void create_UnicodeWhitespaceOnlyName_ThrowsException() {
        CategoryCreateRequest request = new CategoryCreateRequest("\u00A0\u202F\u0085", null, null);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name must contain between 1 and 100 Unicode code points.");
    }

    @Test
    @DisplayName("Name and description limits count Unicode code points")
    void create_SupplementaryCharacters_UsesCodePointLength() {
        String validName = "🍎".repeat(100);
        String validDescription = "🍎".repeat(255);
        CategoryCreateRequest request = new CategoryCreateRequest(validName, validDescription, null);

        when(categoryRepository.existsByNameAndParentId(validName, null)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            setEntityId(category, 1L);
            return category;
        });

        CategoryResponse response = categoryService.create(request);

        assertThat(response.name()).isEqualTo(validName);
        assertThat(response.description()).isEqualTo(validDescription);
        assertThatThrownBy(() -> categoryService.create(
                new CategoryCreateRequest("🍎".repeat(101), null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name must contain between 1 and 100 Unicode code points.");
        assertThatThrownBy(() -> categoryService.create(
                new CategoryCreateRequest("Valid", "🍎".repeat(256), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description must not exceed 255 Unicode code points.");
    }

    @Test
    @DisplayName("Unicode whitespace-only descriptions are normalized to null")
    void create_UnicodeWhitespaceOnlyDescription_IsNull() {
        CategoryCreateRequest request = new CategoryCreateRequest("Valid", "\u00A0\u202F\u0085", null);

        when(categoryRepository.existsByNameAndParentId("Valid", null)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            setEntityId(category, 1L);
            return category;
        });

        assertThat(categoryService.create(request).description()).isNull();
    }

    @Test
    @DisplayName("رفض إنشاء فئة باسم مكرر تحت نفس الأب")
    void create_DuplicateName_ThrowsException() {
        CategoryCreateRequest request = new CategoryCreateRequest("المشروبات", null, null);

        when(categoryRepository.existsByNameAndParentId("المشروبات", null)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(DuplicateCategoryException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("رفض تعيين الفئة كأب لنفسها عند التحديث")
    void update_SelfParenting_ThrowsException() {
        CategoryUpdateRequest request = new CategoryUpdateRequest("المشروبات", null, 1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));

        assertThatThrownBy(() -> categoryService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be its own parent");
    }

    @Test
    @DisplayName("كشف ومنع الحلقة الدائرية عند إعادة الهيكلة (Cycle Detection)")
    void update_CycleDetected_ThrowsException() {
        // محاولة جعل الفئة الأب (rootCategory ID=1) ابناً لفرعها (childCategory ID=2)
        CategoryUpdateRequest request = new CategoryUpdateRequest("المشروبات", null, 2L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(rootCategory));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(childCategory));

        assertThatThrownBy(() -> categoryService.update(1L, request))
                .isInstanceOf(InvalidHierarchyException.class)
                .hasMessageContaining("proposed parent is a descendant");
    }

    @Test
    @DisplayName("الحذف التكراري التصاعدي للأعماق المفتوحة (Iterative Deletion)")
    void deleteSubtreeIteratively_Success() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.findChildIdsByParentIds(List.of(1L))).thenReturn(List.of(2L));
        when(categoryRepository.findChildIdsByParentIds(List.of(2L))).thenReturn(List.of());

        categoryService.deleteSubtreeIteratively(1L);

        verify(categoryRepository).deleteByIdsInBatch(List.of(2L));
        verify(categoryRepository).deleteByIdsInBatch(List.of(1L));
    }

    @Test
    @DisplayName("فشل الحذف عند التمرير لمعرف غير موجود")
    void deleteSubtreeIteratively_NotFound_ThrowsException() {
        when(categoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categoryService.deleteSubtreeIteratively(99L))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    private void setEntityId(Category category, Long id) {
        try {
            var field = Category.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(category, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}