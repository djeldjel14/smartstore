package com.smartstore.smartstore.repository;

import com.smartstore.smartstore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Custom query method - Spring generates the SQL automatically
    // from the method name
    Optional<Category> findByName(String name);

    boolean existsByName(String name);
}