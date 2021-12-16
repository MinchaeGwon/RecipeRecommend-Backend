package com.recipe.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipe.dto.CommentDto;
import com.recipe.entity.Account;
import com.recipe.entity.Comment;
import com.recipe.entity.Post;
import com.recipe.entity.RecommendComment;
import com.recipe.repository.CommentRepository;
import com.recipe.repository.PostRepository;
import com.recipe.repository.RecommendCommtRepository;

@Service
public class CommentService {
	@Autowired
	private CommentRepository commtRepo;

	@Autowired
	private RecommendCommtRepository recommRepo;

	@Autowired
	private PostRepository postRepo;

	// 특정 게시글의 모든 댓글 가져오기
	@Transactional
	public List<CommentDto.ListResponse> getAllComment(Long post_id, Account account) {
		Post post = postRepo.findById(post_id).orElseThrow(EntityNotFoundException::new);
		List<Comment> list = commtRepo.findByPost(post);

		List<CommentDto.ListResponse> result = new ArrayList<>();

		for (Comment commt : list) {
			boolean isAuthor = false;
			if (account != null && account.getId() == commt.getAccount().getId()) {
				isAuthor = true;
			}

			String createAt = commt.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
			boolean isRecommended = isRecommended(commt, account);
			int cntOfRecommend = recommRepo.countByComment(commt);

			result.add(new CommentDto.ListResponse(commt.getId(), commt.getContent(), cntOfRecommend,
					commt.getAccount().getImageUrl(), commt.getAccount().getNickname(), isAuthor, isRecommended,
					createAt));
		}

		return result;
	}

	// size만큼 베스트 댓글 가져오기
	@Transactional
	public List<CommentDto.ListResponse> getBestComment(Long post_id, Account account, Long size) {
		Post post = postRepo.findById(post_id).orElseThrow(EntityNotFoundException::new);
		List<RecommendComment> best = recommRepo.findBestComment(post.getId(), size);

		List<CommentDto.ListResponse> result = new ArrayList<>();

		for (RecommendComment recommend : best) {
			Comment commt = recommend.getComment();

			boolean isAuthor = false;
			if (account != null && account.getId() == commt.getAccount().getId()) {
				isAuthor = true;
			}

			String createAt = post.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
			boolean isRecommended = isRecommended(commt, account);
			int cntOfRecommend = recommRepo.countByComment(commt);

			result.add(new CommentDto.ListResponse(commt.getId(), commt.getContent(), cntOfRecommend,
					commt.getAccount().getImageUrl(), commt.getAccount().getNickname(), isAuthor, isRecommended,
					createAt));
		}

		return result;
	}

	// 댓글 작성하기
	@Transactional
	public Long save(CommentDto info, Account account) {
		Post post = postRepo.findById(info.getPost_id()).orElseThrow(EntityNotFoundException::new);

		return commtRepo.save(Comment.builder().content(info.getContent()).post(post).account(account).build()).getId();
	}

	// 댓글 수정하기
	@Transactional
	public Long update(Long comment_id, CommentDto.update update) {
		Comment commt = commtRepo.findById(comment_id).orElseThrow(EntityNotFoundException::new);
		commt.update(update.getContent());
		return commtRepo.save(commt).getId();
	}

	// 댓글 삭제
	@Transactional
	public void delete(Long id) {
		commtRepo.deleteById(id);
	}

	// 댓글 추천 -> 이미 추천한 상태가 아닐 경우에만 추천
	@Transactional
	public boolean addRecommend(Long comment_id, Account account) {
		System.out.println("nickname: " + account.getNickname());
		Comment commt = commtRepo.findById(comment_id).orElseThrow(EntityNotFoundException::new);

		boolean isExist = recommRepo.existsByCommentAndAccount(commt, account);

		if (isExist) {
			return false;
		}

		RecommendComment recommend = RecommendComment.builder().comment(commt).account(account).build();
		recommRepo.save(recommend);

		return true;
	}

	// 댓글 추천 취소
	@Transactional
	public boolean deleteRecommend(Long comment_id, Account account) {
		Comment commt = commtRepo.findById(comment_id).orElseThrow(EntityNotFoundException::new);
		int delete = recommRepo.deleteByCommentAndAccount(commt, account);

		if (delete == 1) {
			return true;
		} else {
			return false;
		}
	}

	// 사용자가 특정 댓글을 추천했는지
	public boolean isRecommended(Comment comment, Account account) {
		return account == null ? false : recommRepo.existsByCommentAndAccount(comment, account);
	}
}
