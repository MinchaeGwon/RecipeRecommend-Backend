package com.recipe.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recipe.dto.CommentDto;
import com.recipe.entity.Account;
import com.recipe.response.ErrorResponse;
import com.recipe.response.Response;
import com.recipe.response.SuccessListResponse;
import com.recipe.response.SuccessResponse;
import com.recipe.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommentController {

	@Autowired
	CommentService commtService;

	// 특정 게시글의 모든 댓글 가져오기
	@GetMapping("/recipe/{id}/comment")
	public ResponseEntity<?> getAllCommentById(@PathVariable("id") Long id, @AuthenticationPrincipal Account account) {
		
		List<CommentDto.ListResponse> commtList = new ArrayList<>();

		try {
			commtList = commtService.getAllComment(id, account);
			
			if (commtList.size() == 0) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("댓글 목록이 없습니다.", "404"));
			}
			
			return ResponseEntity.ok().body(new SuccessListResponse<List<CommentDto.ListResponse>>(commtList.size(), commtList));

		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 댓글 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("댓글을 가져오는 도중 오류가 발생했습니다.", "500"));
		}

	}

	// 특정 게시글의 베스트 댓글 가져오기
	@GetMapping("/recipe/{id}/comment/best")
	public ResponseEntity<?> getBestCommentById(@PathVariable("id") Long id, @AuthenticationPrincipal Account account,
			@RequestParam(value = "size", required = false) Long size) {

		List<CommentDto.ListResponse> commtList = new ArrayList<>();

		try {
			if (size == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("size가 null입니다.", "400"));
			}
			
			commtList = commtService.getBestComment(id, account, size);
			
			if (commtList.size() == 0) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("댓글 목록이 없습니다.", "404"));
			}
			
			return ResponseEntity.ok().body(new SuccessListResponse<List<CommentDto.ListResponse>>(commtList.size(), commtList));

		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 댓글 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("베스트 댓글을 가져오는 도중 오류가 발생했습니다.", "500"));
		}

	}

	// 특정 게시글 댓글 작성하기
	@PostMapping("/recipe/comment")
	public ResponseEntity<?> createComment(@RequestBody CommentDto info, @AuthenticationPrincipal Account account) {
		Long commentId;

		try {
			System.out.println("post_id: " + info.getPost_id());
			commentId = commtService.save(info, account);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 레시피 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("댓글 작성 실패", "500"));
		}

		return ResponseEntity.ok().body(new SuccessResponse<Long>(commentId));
	}

	// 특정 게시글의 댓글 수정하기
	@PutMapping("/recipe/comment/{id}")
	public ResponseEntity<?> updateComment(@PathVariable("id") Long id, @RequestBody CommentDto.update update) {
		try {
			commtService.update(id, update);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 레시피 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("댓글을 수정하는 도중 오류가 발생했습니다.", "500"));
		}

		return ResponseEntity.ok().body(new Response("댓글 수정 성공"));
	}

	// 특정 게시글의 댓글 삭제하기
	@DeleteMapping("/recipe/comment/{id}")
	public ResponseEntity<?> deleteComment(@PathVariable("id") Long id) {
		try {
			commtService.delete(id);
		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 댓글 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("댓글 삭제 실패", "500"));
		}

		return ResponseEntity.ok().body(new Response("댓글 삭제 성공"));
	}

	// 특정 댓글 추천
	@PostMapping("/recipe/comment/recommend/{id}")
	public ResponseEntity<?> addRecommend(@PathVariable("id") Long id, @AuthenticationPrincipal Account account) {
		boolean result;

		try {
			result = commtService.addRecommend(id, account);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 댓글 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("댓글을 추천하는 도중 오류가 발생했습니다.", "500"));
		}

		if (result) {
			return ResponseEntity.ok().body(new Response("댓글 추천 성공"));
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("이미 추천한 댓글입니다.", "400"));
	}

	// 댓글 추천 취소
	@DeleteMapping("/recipe/comment/recommend/{id}")
	public ResponseEntity<?> deleteRecommend(@PathVariable("id") Long id, @AuthenticationPrincipal Account account) {
		boolean result;

		try {
			result = commtService.deleteRecommend(id, account);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 댓글 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("댓글 추천 취소 실패", "500"));
		}

		if (result) {
			return ResponseEntity.ok().body(new Response("댓글 추천 취소 성공"));
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("댓글 추천 취소 실패", "500"));
	}

}
