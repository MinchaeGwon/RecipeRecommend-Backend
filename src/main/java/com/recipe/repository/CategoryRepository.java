package com.recipe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recipe.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

}
