package com.smartstore.backend.repository;

import com.smartstore.backend.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNull();

    List<Category> findByParentId(Long parentId);

    @Query("SELECT c.id FROM Category c WHERE c.parent.id IN :parentIds")
    List<Long> findChildIdsByParentIds(@Param("parentIds") List<Long> parentIds);

    @Query("SELECT c.id FROM Category c WHERE c.parent.id = :parentId")
    List<Long> findChildIdsByParentId(@Param("parentId") Long parentId);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE (:parentId IS NULL AND c.parent IS NULL AND c.name = :name) OR (:parentId IS NOT NULL AND c.parent.id = :parentId AND c.name = :name)")
    boolean existsByNameAndParentId(@Param("name") String name, @Param("parentId") Long parentId);

    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE (:parentId IS NULL AND c.parent IS NULL AND c.name = :name AND c.id != :excludeId) OR (:parentId IS NOT NULL AND c.parent.id = :parentId AND c.name = :name AND c.id != :excludeId)")
    boolean existsByNameAndParentIdAndIdNot(@Param("name") String name, @Param("parentId") Long parentId, @Param("excludeId") Long excludeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Category c WHERE c.id IN :ids")
    void deleteByIdsInBatch(@Param("ids") List<Long> ids);
}