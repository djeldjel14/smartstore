package com.smartstore.backend.controller;

import tools.jackson.databind.json.JsonMapper;
import com.smartstore.backend.dto.CategoryCreateRequest;
import com.smartstore.backend.dto.CategoryUpdateRequest;
import com.smartstore.backend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/categories - إنشاء فئة جديدة وترجيع 201 CREATED")
    void createCategory_Returns201Created() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("المشروبات الغازية", "مشروبات متنوعة", null);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("المشروبات الغازية")))
                .andExpect(jsonPath("$.parentId", nullValue()));
    }

    @Test
    @DisplayName("POST /api/categories - إرجاع 400 BAD_REQUEST عند إرسال اسم فارغ")
    void createCategory_BlankName_Returns400() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("   ", "وصف", null);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("name")));
    }

    @Test
    void createCategory_MissingName_Returns400() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"وصف\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("VALIDATION_ERROR")))
                .andExpect(jsonPath("$.fieldErrors[0].field", is("name")));
    }

    @Test
    void createCategory_OverlongUnicodeName_Returns400() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("🍎".repeat(101), null, null);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field", is("name")))
                .andExpect(jsonPath("$.fieldErrors[0].message",
                        is("Category name must contain between 1 and 100 Unicode code points.")));
    }

    @Test
    void createCategory_OverlongUnicodeDescription_Returns400() throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest("Valid", "🍎".repeat(256), null);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field", is("description")))
                .andExpect(jsonPath("$.fieldErrors[0].message",
                        is("Description must not exceed 255 Unicode code points.")));
    }

    @Test
    void categoryEndpoints_CreateReadListChildrenAndParent() throws Exception {
        Long rootId = createCategory("المشروبات", null, null);
        Long childId = createCategory("Cola Zero", "  وصف  ", rootId);

        mockMvc.perform(get("/api/categories/" + childId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Cola Zero")))
                .andExpect(jsonPath("$.description", is("وصف")))
                .andExpect(jsonPath("$.parentId", is(rootId.intValue())));

        mockMvc.perform(get("/api/categories/" + rootId + "/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(childId.intValue())));

        mockMvc.perform(get("/api/categories/" + childId + "/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        mockMvc.perform(get("/api/categories/" + childId + "/parent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(rootId.intValue())));

        mockMvc.perform(get("/api/categories/" + rootId + "/parent"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(2)));

        mockMvc.perform(get("/api/categories").param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void categoryEndpoints_SearchAndUpdate() throws Exception {
        Long rootId = createCategory("Juice", "Fresh", null);
        createCategory("Other", null, null);

        mockMvc.perform(get("/api/categories/search")
                        .param("q", "  juice  ")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(rootId.intValue())));

        mockMvc.perform(put("/api/categories/" + rootId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryUpdateRequest("Other", "Updated", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")));

        mockMvc.perform(put("/api/categories/" + rootId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryUpdateRequest("Juice", "Updated", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Juice")))
                .andExpect(jsonPath("$.description", is("Updated")));
    }

    @Test
    void createCategory_DuplicateRootAndSiblingNames_Returns409() throws Exception {
        Long rootId = createCategory("Drinks", null, null);
        createCategory("Food", null, null);
        createCategory("Cola", null, rootId);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryCreateRequest("Drinks", null, null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryCreateRequest("Cola", null, rootId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")));
    }

    @Test
    void updateCategory_SelfParentIsBadRequestAndDescendantParentIsConflict() throws Exception {
        Long rootId = createCategory("Root", null, null);
        Long childId = createCategory("Child", null, rootId);

        mockMvc.perform(put("/api/categories/" + rootId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryUpdateRequest("Root", null, rootId))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/categories/" + rootId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryUpdateRequest("Root", null, childId))))
                .andExpect(status().isConflict());
    }

    @Test
    void categoryEndpoints_MissingCategoriesReturnNotFound() throws Exception {
        long missingId = Long.MAX_VALUE;

        mockMvc.perform(get("/api/categories/" + missingId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/categories/" + missingId + "/children"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/categories/" + missingId + "/parent"))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/categories/" + missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryUpdateRequest("Missing", null, null))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/categories/" + missingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory_UnknownParent_Returns404() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryCreateRequest("Orphan", null, Long.MAX_VALUE))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("GET /api/categories/tree - إرجاع شجرة الفئات الكاملة وترجيع 200 OK")
    void getCategoryTree_ReturnsTreeStructure() throws Exception {
        CategoryCreateRequest rootRequest = new CategoryCreateRequest("المشروبات", null, null);
        String rootResponseStr = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rootRequest)))
                .andReturn().getResponse().getContentAsString();

        Long rootId = objectMapper.readTree(rootResponseStr).get("id").asLong();

        CategoryCreateRequest childRequest = new CategoryCreateRequest("الكولا", null, rootId);
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(childRequest)));

        mockMvc.perform(get("/api/categories/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("المشروبات")))
                .andExpect(jsonPath("$[0].children", hasSize(1)))
                .andExpect(jsonPath("$[0].children[0].name", is("الكولا")));
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - حذف شجرة الفئات تكرارياً وترجيع 204 NO_CONTENT")
    void deleteCategory_Subtree_Returns204NoContent() throws Exception {
        Long rootId = createCategory("قسم محذوف", null, null);
        Long childId = createCategory("فرع محذوف", null, rootId);
        createCategory("فرع أعمق", null, childId);
        Long retainedId = createCategory("قسم محفوظ", null, null);

        mockMvc.perform(delete("/api/categories/" + rootId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories/" + rootId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/categories/" + childId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/categories/" + retainedId))
                .andExpect(status().isOk());
    }

    private Long createCategory(String name, String description, Long parentId) throws Exception {
        CategoryCreateRequest request = new CategoryCreateRequest(name, description, parentId);
        String response = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}