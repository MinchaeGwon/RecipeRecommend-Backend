package com.recipe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recipe.entity.Brand;
import com.recipe.entity.Category;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
	// 특정 카테고리의 모든 브랜드 가져오기
	List<Brand> findByCategory(Category category);
}
