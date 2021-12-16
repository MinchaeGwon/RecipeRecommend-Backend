package com.recipe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recipe.entity.Account;
import com.recipe.entity.Post;
import com.recipe.entity.View;

@Repository
public interface ViewRepository extends JpaRepository<View, Long> {
	// 특정 게시글을 조회했는지 확인
	Boolean existsByPostAndAccount(Post post, Account account);
	
	// 조회수 가져오기
	int countByPost(Post post);
	
	List<View> findByPost(Post post);
	
	Optional<View> findByPostAndAccount(Post post, Account account);
}
