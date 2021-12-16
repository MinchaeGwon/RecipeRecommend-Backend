package com.recipe.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.recipe.dto.AccountDto;
import com.recipe.dto.AccountUpdateDto;
import com.recipe.dto.CommentDto;
import com.recipe.dto.PostDto;
import com.recipe.dto.SocialDto;
import com.recipe.entity.Account;
import com.recipe.response.ErrorResponse;
import com.recipe.response.Response;
import com.recipe.response.SuccessListResponse;
import com.recipe.response.SuccessResponse;
import com.recipe.security.JwtResponse;
import com.recipe.security.JwtTokenUtil;
import com.recipe.security.JwtUserDetailsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {
	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailService;

	@GetMapping("/login/{id}/{type}")
	public ResponseEntity<?> loginTest(@PathVariable("id") String id, @PathVariable("type") String type) {
		String token = "";
		final UserDetails userDetails;

		try {
			userDetails = userDetailService.loadUserByLoginIdAndType(id, type);

			token = jwtTokenUtil.generateToken(userDetails);
		} catch (UsernameNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 회원정보가 없습니다.", "404"));
		}

		return ResponseEntity.ok(new JwtResponse(token));
	}

	// 카카오 로그인
	@PostMapping(value = "/login/kakao")
	public ResponseEntity<?> kakaoLoginRequest(@RequestBody SocialDto infoDto) throws Exception {
		String token = "";
		final UserDetails userDetails;

		try {
			RestTemplate rt = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();

			headers.add("Authorization", "Bearer " + infoDto.getAccessToken());
			headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

			HttpEntity<MultiValueMap<String, String>> kakaoRequest = new HttpEntity<>(headers);
			ResponseEntity<String> response = rt.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET,
					kakaoRequest, String.class);

			JsonParser jsonParser = new JsonParser();
			JsonObject jsonObject = (JsonObject) jsonParser.parse(response.getBody());

			String id = jsonObject.get("id").getAsString();

			System.out.println("카카오");
			
			boolean isExist = userDetailService.isExistLoginidAndType(id, "kakao");

			if (!isExist) {
//	            JsonObject properties = jsonObject.getAsJsonObject().get("properties").getAsJsonObject();
				JsonObject kakao_account = (JsonObject) jsonObject.get("kakao_account");
				JsonObject profile = (JsonObject) kakao_account.get("profile");

				String email = null;
				if (kakao_account.has("email")) {
					email = kakao_account.get("email").getAsString();	
				}
				
				String nickname = profile.get("nickname").getAsString();
				String profile_image = profile.get("profile_image_url").getAsString();
				
				System.out.println("kakao email: " + email + ", nickname: " + nickname + ", image: " + profile_image);

				Account account = new Account(id, "kakao", "ROLE_USER", email, nickname, profile_image);

				userDetailService.save(account);
			}

			userDetails = userDetailService.loadUserByLoginIdAndType(id, "kakao");

			token = jwtTokenUtil.generateToken(userDetails);

		} catch (UsernameNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 회원정보가 없습니다.", "404"));
		}

		return ResponseEntity.ok(new JwtResponse(token));
	}

	// 구글 로그인
	@PostMapping(value = "/login/google")
	public ResponseEntity<?> googleLoginRequest(@RequestBody SocialDto infoDto) throws Exception {
		String token = "";
		final UserDetails userDetails;

		RestTemplate rt = new RestTemplate();
		String response = "";

		try {
			response = rt.getForEntity("https://oauth2.googleapis.com/tokeninfo?id_token=" + infoDto.getAccessToken(),
					String.class).getBody().toString();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("토큰 인증 실패", "500"));
		}

		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject) jsonParser.parse(response);

		String id = jsonObject.get("sub").getAsString();

		boolean isExist = userDetailService.isExistLoginidAndType(id, "google");

		if (!isExist) {
			String email = null;
			if (jsonObject.has("email")) {
				email = jsonObject.get("email").getAsString();	
			}
			
			String nickname = jsonObject.get("name").getAsString();
			String profile_image = jsonObject.get("picture").getAsString();
			
			System.out.println("google email: " + email + ", nickname: " + nickname + ", image: " + profile_image);

			Account account = new Account(id, "google", "ROLE_USER", email, nickname, profile_image);

			userDetailService.save(account);
		}

		try {
			userDetails = userDetailService.loadUserByLoginIdAndType(id, "google");

			token = jwtTokenUtil.generateToken(userDetails);

		} catch (UsernameNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 회원정보가 없습니다.", "404"));
		}

		return ResponseEntity.ok(new JwtResponse(token));
	}

	// 네이버 로그인
	@PostMapping(value = "/login/naver")
	public ResponseEntity<?> naverLoginRequest(@RequestBody SocialDto infoDto) throws Exception {
		String token = "";
		final UserDetails userDetails;
		StringBuilder response = new StringBuilder();

		try {
			String apiURL = "https://openapi.naver.com/v1/nid/me";
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", "Bearer " + infoDto.getAccessToken());

			int responseCode = con.getResponseCode();

			BufferedReader br;
			if (responseCode == 200) {
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			String line;
			while ((line = br.readLine()) != null) {
				response.append(line);
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		JsonParser jsonParser = new JsonParser();
		JsonObject jsonObject = (JsonObject) jsonParser.parse(response.toString());
		JsonObject res = (JsonObject) jsonObject.get("response");

		if (res == null) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("토큰 인증 실패", "500"));
		}

		String id = res.get("id").getAsString();
		
		System.out.println("id: " + id);
		System.out.println("네이버");
		
		boolean isExist = userDetailService.isExistLoginidAndType(id, "naver");

		if (!isExist) {
			String email = null;
			if (res.has("email")) {
				email = res.get("email").getAsString();
			}
			
			String nickname = res.get("nickname").getAsString();
			String profile_image = res.get("profile_image").getAsString();
			
			System.out.println("naver email: " + email + ", nickname: " + nickname + ", image: " + profile_image);

			Account account = new Account(id, "naver", "ROLE_USER", email, nickname, profile_image);

			userDetailService.save(account);
		}
		
		try {
			userDetails = userDetailService.loadUserByLoginIdAndType(id, "naver");

			token = jwtTokenUtil.generateToken(userDetails);

		} catch (UsernameNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 회원정보가 없습니다.", "404"));
		}

		return ResponseEntity.ok(new JwtResponse(token));
	}

	// 로그인한 사용자의 정보 가져오기 -> 필요한 정보만 return 해야 함
	@GetMapping(value = "/account/profile")
	public ResponseEntity<?> getMyAccount(@AuthenticationPrincipal Account account) {
		AccountDto dto = new AccountDto(account.getId(), account.getLoginid(), account.getType(), account.getEmail(),
				account.getNickname(), account.getImageUrl());
		return ResponseEntity.ok().body(new SuccessResponse<AccountDto>(dto));
	}

	// 사용자 id로 정보 가져오기
	@GetMapping(value = "/account/{id}")
	public ResponseEntity<?> getMyAccountById(@PathVariable("id") Long accountId) {
		Account account;
		AccountDto dto;

		try {
			account = userDetailService.loadUserById(accountId);

			dto = new AccountDto(account.getId(), account.getLoginid(), account.getType(), account.getEmail(),
					account.getNickname(), account.getImageUrl());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 회원정보가 없습니다.", "404"));
		}

		return ResponseEntity.ok().body(new SuccessResponse<AccountDto>(dto));
	}

	// 회원정보 수정
	@PutMapping("account/profile")
	public ResponseEntity<?> updateAccount(@RequestBody AccountUpdateDto infoDto,
			@AuthenticationPrincipal Account account) {
		try {
			userDetailService.updateAccount(infoDto, account);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("회원정보 수정 실패", "500"));
		}

		return ResponseEntity.ok().body(new Response("회원정보 수정 성공"));
	}

	// 닉네임 중복 확인
	@GetMapping("/nickname/exist")
	public ResponseEntity<?> isExistName(@RequestParam("nickname") String nickname) {
		if (userDetailService.isExistNickname(nickname)) {
			return ResponseEntity.ok().body(new Response("이미 존재하는 닉네임입니다"));
		} else {
			return ResponseEntity.ok().body(new Response("사용 가능"));
		}
	}

	// 사용자가 작성한 레시피 가져오기
	@GetMapping("/myrecipe")
	public ResponseEntity<?> getMyRecipe(@AuthenticationPrincipal Account account,
			@RequestParam(value = "categoryId", required = false) Long categoryId,
			@RequestParam(value = "brandId", required = false) List<Long> brandId, 
			@RequestParam(value="startId", required=false) Long startId,
			@RequestParam(value="size", required=false) Long size,
			@RequestParam(value="searchText", required=false) String searchText) {
		List<PostDto.ListResponse> postList = new ArrayList<>();

		try {
			if (size == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("size가 null입니다.", "400"));
			}
			
			postList = userDetailService.getMyPost(account, categoryId, brandId, startId, size, searchText);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("작성한 레시피 목록을 가져오는 도중 오류가 발생했습니다.", "500"));
		}
		
		if (postList.size() == 0) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("작성한 레시피 목록이 없습니다.", "404"));
		}

		return ResponseEntity.ok().body(new SuccessListResponse<List<PostDto.ListResponse>>(postList.size(), postList));
	}

	// 작성한 레시피 목록에서 레시피 삭제하기
	@DeleteMapping("/myrecipe/{id}")
	public ResponseEntity<?> deleteRecipePost(@PathVariable("id") Long post_id) {
		try {
			userDetailService.deleteMyRecipe(post_id);
		} catch (EmptyResultDataAccessException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("일치하는 레시피 정보가 없습니다.", "404"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("레시피 삭제 실패", "500"));
		}

		return ResponseEntity.ok().body(new Response("레시피 삭제 성공"));
	}

	// 사용자가 찜한 레시피 가져오기
	@GetMapping("/mylikerecipe")
	public ResponseEntity<?> getMyLikeRecipe(@AuthenticationPrincipal Account account,
			@RequestParam(value = "categoryId", required = false) Long categoryId,
			@RequestParam(value = "brandId", required = false) List<Long> brandId, 
			@RequestParam(value="startId", required=false) Long startId,
			@RequestParam(value="size", required=false) Long size,
			@RequestParam(value="searchText", required=false) String searchText) {
		List<PostDto.ListResponse> postList = new ArrayList<>();

		try {
			if (size == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("size가 null입니다.", "400"));
			}
			
			postList = userDetailService.getLikePost(account, categoryId, brandId, startId, size, searchText);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("찜한 레시피를 가져오는 도중 오류가 발생했습니다.", "500"));
		}
		
		if (postList.size() == 0) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("찜한 레시피 목록이 없습니다.", "404"));
		}

		return ResponseEntity.ok().body(new SuccessListResponse<List<PostDto.ListResponse>>(postList.size(), postList));
	}

	// 찜하기 목록에서 찜하기 취소하기
	@DeleteMapping("/mylikerecipe/{id}")
	public ResponseEntity<?> deleteLiked(@PathVariable("id") Long post_id, @AuthenticationPrincipal Account account) {
		boolean result;

		try {
			result = userDetailService.deleteMyLikeRecipe(post_id, account);

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
	
	@GetMapping("/mycomment")
	public ResponseEntity<?> getMyComment(@AuthenticationPrincipal Account account) {
		List<CommentDto.MyListResponse> commtList = new ArrayList<>();

		try {
			commtList = userDetailService.getMyComment(account);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("작성 댓글을 가져오는 도중 오류가 발생했습니다.", "500"));
		}
		
		if (commtList.size() == 0) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("작성한 댓글이 없습니다.", "404"));
		}

		return ResponseEntity.ok().body(new SuccessResponse<List<CommentDto.MyListResponse>>(commtList));
	}

}
