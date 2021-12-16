package com.recipe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recipe.entity.Account;
import com.recipe.entity.Comment;
import com.recipe.entity.RecommendComment;

@Repository
public interface RecommendCommtRepository extends JpaRepository<RecommendComment, Long> {
	// 특정 댓글을 추천했는지 확인
	Boolean existsByCommentAndAccount(Comment comment, Account account);

	// 추천 취소 -> 해당 댓글과 회원이 일치하는 경우에만 취소
	int deleteByCommentAndAccount(Comment comment, Account account);

	// 추천 개수 가져오기
	int countByComment(Comment comment);

	// 베스트 댓글 가져오기
	@Query(value = "SELECT *, COUNT(*) AS cnt FROM recommend_comment rc, recipe_post p, recipe_comment c "
			+ "WHERE c.post_id = p.id AND rc.comment_id = c.id AND p.id = :postId "
			+ "GROUP BY rc.comment_id ORDER BY cnt DESC, rc.comment_id DESC LIMIT :size", nativeQuery = true)
	List<RecommendComment> findBestComment(@Param("postId") Long postId, @Param("size") Long size);
}
