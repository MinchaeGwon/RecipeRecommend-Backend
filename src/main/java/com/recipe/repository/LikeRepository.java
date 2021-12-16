package com.recipe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recipe.entity.Account;
import com.recipe.entity.LikeRecipe;
import com.recipe.entity.Post;

@Repository
public interface LikeRepository extends JpaRepository<LikeRecipe, Long> {
	// 특정 레시피를 추천했는지 확인
	Boolean existsByPostAndAccount(Post post, Account account);

	// 찜하기 삭제 -> 해당 게시글과 회원이 일치하는 경우에만 삭제
	int deleteByPostAndAccount(Post post, Account account);

	
	// 사용자의 찜한 레시피 가져오기
	@Query(value = "SELECT * FROM like_recipe l, recipe_post r "
			+ "WHERE l.post_id = r.id AND l.account_id = :accountId AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at DESC, r.id DESC LIMIT :size", nativeQuery = true)
	List<LikeRecipe> findLikePost(
			@Param("accountId") Long accountId, @Param("search") String search, @Param("size") Long size);

	@Query(value = "SELECT * FROM like_recipe l, recipe_post r "
			+ "WHERE l.post_id = r.id AND l.account_id = :accountId AND r.id <= :startId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at desc, r.id desc LIMIT :size", nativeQuery = true)
	List<LikeRecipe> findLikePostByStartId(
			@Param("accountId") Long accountId, @Param("search") String search,
			@Param("startId") Long startId, @Param("size") Long size);

	
	// 사용자가 찜한 레시피 중 특정 카테고리에 있는 것만 가져오기
	@Query(value = "SELECT * FROM like_recipe l, recipe_post r "
			+ "WHERE l.post_id = r.id AND l.account_id = :accountId AND r.category_id = :categoryId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at DESC, r.id DESC LIMIT :size", nativeQuery = true)
	List<LikeRecipe> findLikeCategoryPost(@Param("accountId") Long accountId,
			@Param("search") String search, @Param("categoryId") Long categoryId, @Param("size") Long size);

	@Query(value = "SELECT * FROM like_recipe l, recipe_post r "
			+ "WHERE l.post_id = r.id AND l.account_id = :accountId AND r.id <= :startId AND r.category_id = :categoryId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at desc, r.id desc LIMIT :size", nativeQuery = true)
	List<LikeRecipe> findLikeCategoryPostByStartId(@Param("accountId") Long accountId,
			@Param("search") String search, @Param("categoryId") Long categoryId, 
			@Param("startId") Long startId, @Param("size") Long size);
	
	
	// 사용자가 찜한 레시피 중 특정 카테고리, 브랜드에 있는 것만 가져오기
	@Query(value = "SELECT * FROM like_recipe l, recipe_post r "
			+ "WHERE l.post_id = r.id AND l.account_id = :accountId AND r.category_id = :categoryId AND r.brand_id = :brandId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at DESC, r.id DESC LIMIT :size", nativeQuery = true)
	List<LikeRecipe> findLikeCategoryBrandPost(@Param("accountId") Long accountId,
			@Param("search") String search, @Param("categoryId") Long categoryId, 
			@Param("brandId") Long brandId, @Param("size") Long size);

	@Query(value = "SELECT * FROM like_recipe l, recipe_post r "
			+ "WHERE l.post_id = r.id AND l.account_id = :accountId AND r.id <= :startId "
			+ "AND r.category_id = :categoryId AND r.brand_id = :brandId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at desc, r.id desc LIMIT :size", nativeQuery = true)
	List<LikeRecipe> findLikeCategoryBrandPostByStartId(@Param("accountId") Long accountId,
			@Param("search") String search, @Param("categoryId") Long categoryId, @Param("brandId") Long brandId,
			@Param("startId") Long startId, @Param("size") Long size);
	
}
