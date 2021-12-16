package com.recipe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.recipe.entity.Account;
import com.recipe.entity.Post;
import com.recipe.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
	List<Tag> findByPostAndAccount(Post post, Account account);
	List<Tag> findByPost_IdAndAccount(Long id, Account account);
	Boolean existsByPostAndAccount(Post post, Account account);
	int deleteByTagnameAndPost_Id(String tagname, Long id);
	Optional<Tag> findByTagnameAndPost_Id(String tagname, Long id);
}
