package com.recipe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recipe.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

	// 카테고리, 브랜드 상관없이 가져오기
	// search로 검색, 최신 레시피 size만큼 가져오기
	@Query(value = "SELECT * FROM recipe_post r WHERE (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.id DESC LIMIT :size", nativeQuery = true)
	List<Post> findPost(@Param("search") String search, @Param("size") Long size);

	// search로 검색, startId부터 size만큼 가져오기
	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.id <= :startId AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.id DESC LIMIT :size", nativeQuery = true)
	List<Post> findPostByStartId(@Param("search") String search, @Param("startId") Long startId,
			@Param("size") Long size);

	
	// 특정 카테고리의 레시피 가져오기
	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.category_id = :categoryId AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at DESC, r.id DESC LIMIT :size", nativeQuery = true)
	List<Post> findCategoryPost(
			@Param("search") String search, @Param("categoryId") Long categoryId, @Param("size") Long size);

	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.id <= :startId AND r.category_id = :categoryId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at desc, r.id desc LIMIT :size", nativeQuery = true)
	List<Post> findCategoryPostByStartId(
			@Param("search") String search, @Param("categoryId") Long categoryId, 
			@Param("startId") Long startId, @Param("size") Long size);

	
	// 특정 카테고리, 브랜드의 레시피 가져오기
	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.category_id = :categoryId AND r.brand_id = :brandId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at DESC, r.id DESC LIMIT :size", nativeQuery = true)
	List<Post> findCategoryBrandPost(
			@Param("search") String search, @Param("categoryId") Long categoryId, 
			@Param("brandId") Long brandId, @Param("size") Long size);

	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.id <= :startId AND r.category_id = :categoryId AND r.brand_id = :brandId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at desc, r.id desc LIMIT :size", nativeQuery = true)
	List<Post> findCategoryBrandPostByStartId(
			@Param("search") String search, @Param("categoryId") Long categoryId, @Param("brandId") Long brandId,
			@Param("startId") Long startId, @Param("size") Long size);

	
	// 사용자가 작성한 레시피 가져오기
	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.account_id = :accountId AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at DESC, r.id DESC LIMIT :size", nativeQuery = true)
	List<Post> findAccountPost(
			@Param("accountId") Long accountId, @Param("search") String search, @Param("size") Long size);

	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.account_id = :accountId AND r.id <= :startId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at desc, r.id desc LIMIT :size", nativeQuery = true)
	List<Post> findAccountPostByStartId(
			@Param("accountId") Long accountId, @Param("search") String search,
			@Param("startId") Long startId, @Param("size") Long size);

	
	// 사용자가 작성한 레시피 중 특정 카테고리에 있는 것만 가져오기
	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.account_id = :accountId AND r.category_id = :categoryId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at DESC, r.id DESC LIMIT :size", nativeQuery = true)
	List<Post> findAccountCategoryPost(@Param("accountId") Long accountId,
			@Param("search") String search, @Param("categoryId") Long categoryId, @Param("size") Long size);

	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.account_id = :accountId AND r.id <= :startId AND r.category_id = :categoryId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at desc, r.id desc LIMIT :size", nativeQuery = true)
	List<Post> findAccountCategoryPostByStartId(@Param("accountId") Long accountId,
			@Param("search") String search, @Param("categoryId") Long categoryId, 
			@Param("startId") Long startId, @Param("size") Long size);

	
	// 사용자가 작성한 레시피 중 특정 카테고리, 브랜드에 있는 것만 가져오기
	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.account_id = :accountId AND r.category_id = :categoryId AND r.brand_id = :brandId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at DESC, r.id DESC LIMIT :size", nativeQuery = true)
	List<Post> findAccountCategoryBrandPost(@Param("accountId") Long accountId,
			@Param("search") String search, @Param("categoryId") Long categoryId, 
			@Param("brandId") Long brandId, @Param("size") Long size);

	@Query(value = "SELECT * FROM recipe_post r "
			+ "WHERE r.account_id = :accountId AND r.id <= :startId "
			+ "AND r.category_id = :categoryId AND r.brand_id = :brandId "
			+ "AND (r.title LIKE %:search% OR r.content LIKE %:search%) "
			+ "ORDER BY r.create_at desc, r.id desc LIMIT :size", nativeQuery = true)
	List<Post> findAccountCategoryBrandPostByStartId(@Param("accountId") Long accountId,
			@Param("search") String search, @Param("categoryId") Long categoryId, @Param("brandId") Long brandId,
			@Param("startId") Long startId, @Param("size") Long size);
	
	// size만큼 랜덤으로 레시피 가져오기
	@Query(value = "SELECT * FROM recipe_post ORDER BY rand() LIMIT :size", nativeQuery = true)
	List<Post> findRandomRecipe(@Param("size") Long size);
}
