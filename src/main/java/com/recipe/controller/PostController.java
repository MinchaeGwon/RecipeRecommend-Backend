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

import com.recipe.dto.BrandDto;
import com.recipe.dto.CategoryDto;
import com.recipe.dto.PostDto;
import com.recipe.dto.PostUpdateDto;
import com.recipe.entity.Account;
import com.recipe.response.ErrorResponse;
import com.recipe.response.Response;
import com.recipe.response.SuccessListResponse;
import com.recipe.response.SuccessResponse;
import com.recipe.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PostController {

	@Autowired
	PostService postService;

	// 모든 카테고리 가져오기
	@GetMapping("/category")
	public ResponseEntity<?> getAllCategory() {
		List<CategoryDto> catList = new ArrayList<>();

		try {
			catList = postService.getAllCategoryName();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("카테고리를 가져오는 도중 오류가 발생했습니다.", "500"));
		}
		
		if (catList.size() == 0) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("카테고리 목록이 없습니다.", "404"));
		}

		return ResponseEntity.ok().body(new SuccessResponse<List<CategoryDto>>(catList));
	}

	// 모든 브랜드 / 특정 카테고리의 모든 브랜드 가져오기
	@GetMapping("/brand")
	public ResponseEntity<?> getBrandByCategory(@RequestParam(value="categoryId", required=false) Long categoryId) {
		List<BrandDto> brandList = new ArrayList<>();

		try {
			if (categoryId == null) {
				brandList = postService.getAllBrand();
			}
			else {
				brandList = postService.getBrandByCategoryId(categoryId);	
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 카테고리가 없습니다.", "404"));
		}
		
		if (brandList.size() == 0) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("브랜드 목록이 없습니다.", "404"));
		}

		return ResponseEntity.ok().body(new SuccessResponse<List<BrandDto>>(brandList));
	}
	
	// 모든 레시피 / 특정 카테고리, 브랜드의 레시피 가져오기
	@GetMapping("/recipe")
	public ResponseEntity<?> getPostByCategoryAndBrand(
			@RequestParam(value="categoryId", required=false) Long categoryId, 
			@RequestParam(value="brandId", required=false) List<Long> brandId,
			@RequestParam(value="startId", required=false) Long startId,
			@RequestParam(value="size", required=false) Long size,
			@RequestParam(value="searchText", required=false) String searchText) {
		
		List<PostDto.ListResponse> postList = new ArrayList<>();

		try {
			if (size == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("size가 null입니다.", "400"));
			}
			
			postList = postService.getPostList(categoryId, brandId, startId, size, searchText);
			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피 목록을 가져오는 도중 오류가 발생했습니다. 없습니다.", "500"));
		}
		
		if (postList.size() == 0) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("레시피 목록이 없습니다.", "404"));
		}

		return ResponseEntity.ok().body(new SuccessListResponse<List<PostDto.ListResponse>>(postList.size(), postList));
	}
	
	// 베스트 레시피 가져오기
	@GetMapping("/recipe/best")
	public ResponseEntity<?> getBestRecipe(@RequestParam(value="size", required=false) Long size) {
		List<PostDto.ListResponse> postList = new ArrayList<>();
		
		try {
			if (size == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("size가 null입니다.", "400"));
			}
			
			postList = postService.getBestPostList(size);
			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("베스트 레시피를 가져오는 도중 오류가 발생했습니다. 없습니다.", "500"));
		}
		
		if (postList.size() == 0) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("베스트 레시피 목록이 없습니다.", "404"));
		}
		
		return ResponseEntity.ok().body(new SuccessResponse<List<PostDto.ListResponse>>(postList));
	}
	
	// 랜덤 레시피 가져오기
	@GetMapping("/recipe/random")
	public ResponseEntity<?> getRandomRecipe(@RequestParam(value="size", required=false) Long size) {
		List<PostDto.ListResponse> postList = new ArrayList<>();
		
		try {
			if (size == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("size가 null입니다.", "400"));
			}
			
			postList = postService.getRandomPostList(size);
			
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("랜덤 레시피를 가져오는 도중 오류가 발생했습니다. 없습니다.", "500"));
		}
		
		if (postList.size() == 0) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("랜덤 레시피 목록이 없습니다.", "404"));
		}
		
		return ResponseEntity.ok().body(new SuccessResponse<List<PostDto.ListResponse>>(postList));
	}

	// 특정 레시피 가져오기
	@GetMapping("/recipe/{id}")
	public ResponseEntity<?> getPostById(@PathVariable("id") Long id, @AuthenticationPrincipal Account account) {
		PostDto.DetailResponse post;

		try {
			post = postService.getPostById(id, account);
			return ResponseEntity.ok().body(new SuccessResponse<PostDto.DetailResponse>(post));
			
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 레시피 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피를 가져오는 도중 오류가 발생했습니다.", "500"));
		}

	}

	// 레시피 작성
	@PostMapping("/recipe")
	public ResponseEntity<?> createRecipePost(@RequestBody PostDto info, @AuthenticationPrincipal Account account) {
		Long postId;

		try {
			postId = postService.save(info, account);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피 작성 실패", "500"));
		}

		return ResponseEntity.ok().body(new SuccessResponse<Long>(postId));
	}

	// 레시피 수정
	@PutMapping("/recipe/{id}")
	public ResponseEntity<?> updateRecipePost(@PathVariable("id") Long id, @RequestBody PostUpdateDto info
			, @AuthenticationPrincipal Account account) {
		Long postId;

		try {
			postId = postService.updatePost(id, info, account);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 레시피 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피를 수정하는 도중 오류가 발생했습니다.", "500"));
		}

		return ResponseEntity.ok().body(new SuccessResponse<Long>(postId));
	}

	// 레시피 삭제
	@DeleteMapping("/recipe/{id}")
	public ResponseEntity<?> deleteRecipePost(@PathVariable("id") Long id) {
		try {
			postService.deletePost(id);
		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 레시피 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피 삭제 실패", "500"));
		}
		
		return ResponseEntity.ok().body(new Response("레시피 삭제 성공"));
	}

	// 특정 레시피 찜하기
	@PostMapping("/recipe/like/{id}")
	public ResponseEntity<?> addLiked(@PathVariable("id") Long id, @AuthenticationPrincipal Account account) {
		boolean result;

		try {
			result = postService.addLike(id, account);
			
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 레시피 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피를 찜하는 도중 오류가 발생했습니다.", "500"));
		}

		if (result) {
			return ResponseEntity.ok().body(new Response("레시피 찜하기 성공"));			
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("이미 찜한 레시피입니다.", "400"));
	}

	// 찜하기 취소
	@DeleteMapping("/recipe/like/{id}")
	public ResponseEntity<?> deleteLiked(@PathVariable("id") Long id, @AuthenticationPrincipal Account account) {
		boolean result;
		
		try {
			result = postService.deleteLike(id, account);
			
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 레시피 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피 찜하기 취소 실패", "500"));
		}

		if (result) {
			return ResponseEntity.ok().body(new Response("레시피 찜하기 취소 성공"));			
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피 찜하기 취소 실패", "500"));
	}

	// 특정 레시피 추천
	@PostMapping("/recipe/recommend/{id}")
	public ResponseEntity<?> addRecommend(@PathVariable("id") Long id, @AuthenticationPrincipal Account account) {
		boolean result;

		try {
			result = postService.addRecommend(id, account);
			
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 레시피 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피를 추천하는 도중 오류가 발생했습니다.", "500"));
		}

		if (result) {
			return ResponseEntity.ok().body(new Response("레시피 추천 성공"));			
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("이미 추천한 레시피입니다.", "400"));
	}

	// 레시피 추천 취소
	@DeleteMapping("/recipe/recommend/{id}")
	public ResponseEntity<?> deleteRecommend(@PathVariable("id") Long id, @AuthenticationPrincipal Account account) {
		boolean result;
		
		try {
			result = postService.deleteRecommend(id, account);
			
		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 레시피 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피 추천 취소 실패", "500"));
		}

		if (result) {
			return ResponseEntity.ok().body(new Response("레시피 추천 취소 성공"));			
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피 추천 취소 실패", "500"));
	}

}
