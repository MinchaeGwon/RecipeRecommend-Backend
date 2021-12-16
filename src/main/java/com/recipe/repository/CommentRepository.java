package com.recipe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recipe.entity.Account;
import com.recipe.entity.Comment;
import com.recipe.entity.Post;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
	List<Comment> findByPost(Post post);
	List<Comment> findByAccount(Account account);
	
}
