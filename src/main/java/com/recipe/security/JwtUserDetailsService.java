package com.recipe.security;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipe.dto.AccountUpdateDto;
import com.recipe.dto.CommentDto;
import com.recipe.dto.PostDto;
import com.recipe.entity.Account;
import com.recipe.entity.Brand;
import com.recipe.entity.Category;
import com.recipe.entity.Comment;
import com.recipe.entity.LikeRecipe;
import com.recipe.entity.Post;
import com.recipe.repository.AccountRepository;
import com.recipe.repository.CommentRepository;
import com.recipe.repository.LikeRepository;
import com.recipe.repository.PostRepository;
import com.recipe.repository.RecommendCommtRepository;
import com.recipe.repository.RecommendPostRepository;
import com.recipe.repository.ViewRepository;

@Service
public class JwtUserDetailsService implements UserDetailsService {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private PostRepository postRepo;
	
	@Autowired
	private CommentRepository commtRepo;

	@Autowired
	private LikeRepository likeRepo;

	@Autowired
	private RecommendPostRepository recommendPostRepo;
	
	@Autowired
	private RecommendCommtRepository recommendCommtRepo;
	
	@Autowired
	private ViewRepository viewRepo;

	// 시큐리티에서 지정한 서비스이기 때문에 이 메소드를 필수로 구현
	public UserDetails loadUserByUsername(String loginid) throws UsernameNotFoundException {
		return accountRepository.findByLoginid(loginid).orElseThrow(() -> new UsernameNotFoundException((loginid)));
	}

	// 소셜 로그인에 사용
	public UserDetails loadUserByLoginIdAndType(String loginid, String type) throws UsernameNotFoundException {
		return accountRepository.findByLoginidAndType(loginid, type)
				.orElseThrow(() -> new UsernameNotFoundException((loginid)));
	}

	// 닉네임 중복 확인
	public Boolean isExistLoginidAndType(String loginid, String type) {
		return accountRepository.existsByLoginidAndType(loginid, type);
	}

	// 회원정보 저장
	@Transactional
	public Long save(Account info) {
		Long account_id;
		
		if (info.getEmail() != null) {
			account_id = accountRepository.save(Account.builder().loginid(info.getLoginid()).type(info.getType())
					.auth(info.getAuth()).email(info.getNickname()).nickname(info.getNickname()).imageUrl(info.getImageUrl()).build()).getId();
		}
		else {
			account_id = accountRepository.save(Account.builder().loginid(info.getLoginid()).type(info.getType())
					.auth(info.getAuth()).nickname(info.getNickname()).imageUrl(info.getImageUrl()).build()).getId();
		}
		return account_id;
	}

	// 닉네임 중복 확인
	public Boolean isExistNickname(String nickname) {
		return accountRepository.existsByNickname(nickname);
	}

	// 특정 사용자의 accountId로 사용자 정보 가져오기
	public Account loadUserById(long id) throws UsernameNotFoundException {
		return accountRepository.findById(id).orElseThrow();
	}

	// 회원정보 수정
	@Transactional
	public Long updateAccount(AccountUpdateDto infoDto, Account account) throws UsernameNotFoundException {
		Account updateAccount = (Account) loadUserByLoginIdAndType(account.getLoginid(), account.getType());

		updateAccount.update(infoDto);
		return accountRepository.save(updateAccount).getId();
	}

	// 사용자가 작성한 레시피 가져오기
	public List<PostDto.ListResponse> getMyPost(Account account, Long categoryId, List<Long> brandList, Long startId, Long size, String search) {
		
		List<Post> postList = new ArrayList<>();
		List<PostDto.ListResponse> result = new ArrayList<>();
		
		// 검색어가 없을 경우 공백 채우고 있을 경우 문자열 앞, 뒤 공백 제거
		search = search == null ? "" : search.trim();

		if (categoryId == null && brandList == null) {
			// 카테고리, 브랜드가 null일 경우 -> 모든 레시피 가져오기
			if (startId == null) {
				postList = postRepo.findAccountPost(account.getId(), search, size);
			} else {
				postList = postRepo.findAccountPostByStartId(account.getId(), search, startId, size);
			}
		} else if (brandList == null) {
			// 브랜드만 null일 경우 -> 카테고리에 해당하는 레시피 가져오기
			if (startId == null) {
				postList = postRepo.findAccountCategoryPost(account.getId(), search, categoryId, size);
			} else {
				postList = postRepo.findAccountCategoryPostByStartId(account.getId(), search, categoryId, startId, size);
			}
		} else {
			// 둘 다 null이 아닐 경우 -> 카테고리, 브랜드에 해당하는 레시피 가져오기
			for (Long brandId : brandList) {
				if (startId == null) {
					postList.addAll(postRepo.findAccountCategoryBrandPost(account.getId(), search, categoryId, brandId, size));
				} else {
					postList.addAll(postRepo.findAccountCategoryBrandPostByStartId(account.getId(), search, categoryId, brandId, startId, size));
				}
			}
		}

		for (Post post : postList) {
			int cntOfRecommend = recommendPostRepo.countByPost(post);
			int view = viewRepo.countByPost(post);

			Category cat = post.getCategory();
			Brand brand = post.getBrand();

			result.add(new PostDto.ListResponse(post.getId(), cat.getId(), cat.getCategoryname(), brand.getId(), brand.getBrandname(), 
					post.getTitle(), post.getImageUrl(), cntOfRecommend, view, post.getAccount().getNickname()));
		}

		return result;
	}

	// 사용자가 찜한 레시피 가져오기
	public List<PostDto.ListResponse> getLikePost(Account account, Long categoryId, List<Long> brandList, Long startId, Long size, String search) {
		List<LikeRecipe> likeList = new ArrayList<>();
		List<PostDto.ListResponse> result = new ArrayList<>();

		// 검색어가 없을 경우 공백 채우고 있을 경우 문자열 앞, 뒤 공백 제거
		search = search == null ? "" : search.trim();
		
		if (categoryId == null && brandList == null) {
			// 카테고리, 브랜드가 null일 경우 -> 찜한 모든 레시피 가져오기
			if (startId == null) {
				likeList = likeRepo.findLikePost(account.getId(), search, size);
			} else {
				likeList = likeRepo.findLikePostByStartId(account.getId(), search, startId, size);
			}
		}
		else if (brandList == null) {
			// 브랜드만 null일 경우 -> 찜한 레시피 중 카테고리에 해당하는 것만 가져오기
			if (startId == null) {
				likeList = likeRepo.findLikeCategoryPost(account.getId(), search, categoryId, size);
			} else {
				likeList = likeRepo.findLikeCategoryPostByStartId(account.getId(), search, categoryId, startId, size);
			}
		} else {
			// 둘 다 null이 아닐 경우 -> 찜한 레시피 중 카테고리, 브랜드에 해당하는 것만 가져오기
			for (Long brandId : brandList) {
				if (startId == null) {
					likeList.addAll(likeRepo.findLikeCategoryBrandPost(account.getId(), search, categoryId, brandId, size));
				} else {
					likeList.addAll(likeRepo.findLikeCategoryBrandPostByStartId(
							account.getId(), search, categoryId, brandId, startId, size));
				}
			}
		}
		
		for (LikeRecipe like : likeList) {
			Post post = like.getPost();

			int cntOfRecommend = recommendPostRepo.countByPost(post);
			int view = viewRepo.countByPost(post);
			
			Category cat = post.getCategory();
			Brand brand = post.getBrand();

			result.add(new PostDto.ListResponse(post.getId(), cat.getId(), cat.getCategoryname(), brand.getId(), brand.getBrandname(), 
					post.getTitle(), post.getImageUrl(), cntOfRecommend, view, post.getAccount().getNickname()));
		}

		return result;
	}

	// 작성한 레시피 목록에서 레시피 삭제하기
	@Transactional
	public void deleteMyRecipe(Long post_id) {
		postRepo.deleteById(post_id);
	}

	// 찜한 레시피 목록에서 찜하기 취소하기
	@Transactional
	public boolean deleteMyLikeRecipe(Long post_id, Account account) {
		Post post = postRepo.findById(post_id).orElseThrow(EntityNotFoundException::new);

		int delete = likeRepo.deleteByPostAndAccount(post, account);

		if (delete == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	// 사용자가 작성한 댓글 가져오기
	public List<CommentDto.MyListResponse> getMyComment(Account account) {
		List<Comment> list = commtRepo.findByAccount(account);
		
		List<CommentDto.MyListResponse> result = new ArrayList<>();
		
		for (Comment commt : list) {			
			String createAt = commt.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
			int cntOfRecommend = recommendCommtRepo.countByComment(commt);
			
			result.add(new CommentDto.MyListResponse(commt.getId(), commt.getContent(), cntOfRecommend, createAt));
		}
		
		return result;
	}
}
