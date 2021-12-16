package com.recipe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recipe.entity.Account;
import com.recipe.entity.Post;
import com.recipe.entity.RecommendPost;

@Repository
public interface RecommendPostRepository extends JpaRepository<RecommendPost, Long> {
	// 특정 게시글을 추천했는지 확인
	Boolean existsByPostAndAccount(Post post, Account account);

	// 추천 취소 -> 해당 게시글과 회원이 일치하는 경우에만 취소
	int deleteByPostAndAccount(Post post, Account account);
	
	// 추천 개수 가져오기
	int countByPost(Post post);
	
	// 베스트 레시피 가져오기
	@Query(value = "SELECT *, COUNT(*) AS cnt FROM recommend_recipe rp "
			+ "GROUP BY rp.post_id ORDER BY cnt DESC, rp.post_id DESC LIMIT :size ", nativeQuery = true)
	List<RecommendPost> findBestRecipe(@Param("size") Long size);
}
