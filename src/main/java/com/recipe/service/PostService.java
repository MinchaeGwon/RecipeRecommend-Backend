package com.recipe.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.recipe.dto.BrandDto;
import com.recipe.dto.CategoryDto;
import com.recipe.dto.PostDto;
import com.recipe.dto.PostUpdateDto;
import com.recipe.entity.Account;
import com.recipe.entity.Brand;
import com.recipe.entity.Category;
import com.recipe.entity.LikeRecipe;
import com.recipe.entity.Post;
import com.recipe.entity.RecommendPost;
import com.recipe.entity.Tag;
import com.recipe.entity.View;
import com.recipe.repository.AccountRepository;
import com.recipe.repository.BrandRepository;
import com.recipe.repository.CategoryRepository;
import com.recipe.repository.LikeRepository;
import com.recipe.repository.PostRepository;
import com.recipe.repository.RecommendPostRepository;
import com.recipe.repository.TagRepository;
import com.recipe.repository.ViewRepository;

@Service
public class PostService {
	@Autowired
	private PostRepository postRepository;

	@Autowired
	private LikeRepository likeRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private BrandRepository brandRepository;

	@Autowired
	private TagRepository tagRepository;

	@Autowired
	private RecommendPostRepository recommendPostRepo;

	@Autowired
	private AccountRepository accountRepo;

	@Autowired
	private ViewRepository viewRepo;

	// 모든 레시피 / 특정 카테고리, 브랜드의 레시피 가져오기
	@Transactional
	public List<PostDto.ListResponse> getPostList(Long categoryId, List<Long> brandList, Long startId, Long size, String search) {
		List<PostDto.ListResponse> result = new ArrayList<>();
		List<Post> postList = new ArrayList<>();
		
		// 검색어가 없을 경우 공백 채우고 있을 경우 문자열 앞, 뒤 공백 제거
		search = search == null ? "" : search.trim();

		if (categoryId == null && brandList == null) {
			// 카테고리, 브랜드가 null일 경우 -> 모든 레시피 가져오기
			
			if (startId == null) {
				// startId가 null일 경우 최신순으로 size만큼 가져오기
				postList = postRepository.findPost(search, size);
			} else {
				// startId가 null이 아닐 경우 startId부터 size만큼 가져오기
				postList = postRepository.findPostByStartId(search, startId, size);
			}
		} else if (brandList == null) {
			// 브랜드만 null일 경우 -> 카테고리에 해당하는 레시피 가져오기
			if (startId == null) {
				postList = postRepository.findCategoryPost(search, categoryId, size);
			} else {
				postList = postRepository.findCategoryPostByStartId(search, categoryId, startId, size);
			}
		} else {
			// 둘 다 null이 아닐 경우 -> 카테고리, 브랜드에 해당하는 레시피 가져오기
			for (Long brandId : brandList) {
				if (startId == null) {
					postList.addAll(postRepository.findCategoryBrandPost(search, categoryId, brandId, size));
				} else {
					postList.addAll(postRepository.findCategoryBrandPostByStartId(search, categoryId, brandId, startId, size));
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

	// size만큼 베스트 레시피 가져오기
	@Transactional
	public List<PostDto.ListResponse> getBestPostList(Long size) {
		List<PostDto.ListResponse> result = new ArrayList<>();
		List<RecommendPost> best = new ArrayList<>();

		best = recommendPostRepo.findBestRecipe(size);

		for (RecommendPost recommend : best) {
			Post post = recommend.getPost();

			int cntOfRecommend = recommendPostRepo.countByPost(post);
			int view = viewRepo.countByPost(post);

			Category cat = post.getCategory();
			Brand brand = post.getBrand();

			result.add(new PostDto.ListResponse(post.getId(), cat.getId(), cat.getCategoryname(), brand.getId(), brand.getBrandname(), 
					post.getTitle(), post.getImageUrl(), cntOfRecommend, view, post.getAccount().getNickname()));
		}

		return result;
	}

	// size만큼 랜덤으로 레시피 가져오기
	@Transactional
	public List<PostDto.ListResponse> getRandomPostList(Long size) {
		List<PostDto.ListResponse> result = new ArrayList<>();
		List<Post> random = new ArrayList<>();

		random = postRepository.findRandomRecipe(size);

		for (Post post : random) {

			int cntOfRecommend = recommendPostRepo.countByPost(post);
			int view = viewRepo.countByPost(post);

			Category cat = post.getCategory();
			Brand brand = post.getBrand();

			result.add(new PostDto.ListResponse(post.getId(), cat.getId(), cat.getCategoryname(), brand.getId(), brand.getBrandname(), 
					post.getTitle(), post.getImageUrl(), cntOfRecommend, view, post.getAccount().getNickname()));
		}

		return result;
	}

	// post_id로 특정 레시피 가져오기
	@Transactional
	public PostDto.DetailResponse getPostById(Long id, Account account) {
		Post post = postRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		int cntOfRecommend = recommendPostRepo.countByPost(post);
		String createAt = post.getCreateAt().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
		Account post_account = post.getAccount();

		Category cat = post.getCategory();
		CategoryDto catDto = new CategoryDto(cat.getId(), cat.getCategoryname());

		Brand brand = post.getBrand();
		BrandDto.Detail brandDto = new BrandDto.Detail(brand.getId(), brand.getBrandname());

		boolean author = false;
		int cntOfView = 0;

		if (account != null && account.getId() == post_account.getId()) {
			author = true;
		}
		
		increaseView(post, account); // 조회수 증가시키기
		cntOfView = getViewCount(post); // 조회수 가져오기

		PostDto.DetailResponse result = new PostDto.DetailResponse(post.getId(), catDto, brandDto, post.getTitle(),
				post.getContent(), post.getImageUrl(), post.getPrice(), cntOfRecommend, cntOfView, getTagByPost(post.getTags()),
				post.getComment().size(), createAt, post_account.getImageUrl(), post_account.getNickname(),
				author, isRecommended(post, account), isLiked(post, account));

		return result;
	}

	// 레시피 내용 저장
	@Transactional
	public Long save(PostDto info, Account account) {
		Category category = categoryRepository.findById(info.getCategory_id()).get();
		Brand brand = brandRepository.findById(info.getBrand_id()).get();

		Long post_id = postRepository.save(Post.builder().title(info.getTitle()).content(info.getContent())
				.imageUrl(info.getImageUrl()).price(info.getPrice()).account(account).category(category).brand(brand).build()).getId();

		saveTag(post_id, account.getId(), info.getTags());

		return post_id;
	}

	// 태그 저장
	@Transactional
	public void saveTag(Long post_id, Long account_id, List<String> list) {
		Post post = postRepository.findById(post_id).get();
		Account account = accountRepo.findById(account_id).get();

		for (String tag : list) {
			tagRepository.save(Tag.builder().tagname(tag).post(post).account(account).build());
		}
	}

	// 레시피 수정
	@Transactional
	public Long updatePost(Long id, PostUpdateDto infoDto, Account account) {
		Post updatePost = postRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		List<Tag> originalTag = updatePost.getTags(); // 원본 태그

		List<String> originalName = new ArrayList<>(); // 원본 태그 이름 저장
		for (Tag tag : originalTag) {
			originalName.add(tag.getTagname());
		}

		List<String> list = new ArrayList<>();
		for (String tagName : infoDto.getTags()) {
			// 수정할 태그 이름이 원본 태그 이름에 없을 경우
			if (!originalName.contains(tagName)) {
				list.add(tagName);
			}
		}
		saveTag(id, account.getId(), list); // 새롭게 저장

		for (String tagName : originalName) {
			// 수정할 태그 이름에 원본 태그 이름이 저장되어있지 않을 경우
			if (!infoDto.getTags().contains(tagName)) {
				Tag tag = tagRepository.findByTagnameAndPost_Id(tagName, id).get();
				updatePost.getTags().remove(tag); // 해당 태그를 삭제
			}
		}

		updatePost.update(infoDto);
		Long post_id = postRepository.save(updatePost).getId();

		return post_id;
	}

	// 레시피 삭제
	@Transactional
	public void deletePost(Long id) {
		postRepository.deleteById(id);
	}

	// 모든 카테고리 가져오기
	public List<CategoryDto> getAllCategoryName() {
		List<Category> list = categoryRepository.findAll();

		List<CategoryDto> result = new ArrayList<>();

		for (Category cat : list) {
			result.add(new CategoryDto(cat.getId(), cat.getCategoryname()));
		}

		return result;
	}

	// 모든 브랜드 가져오기
	@Transactional
	public List<BrandDto> getAllBrand() {
		List<Brand> list = brandRepository.findAll();

		List<BrandDto> result = new ArrayList<>();

		for (Brand brand : list) {
			result.add(new BrandDto(brand.getCategory().getId(), brand.getId(), brand.getBrandname()));
		}

		return result;
	}

	// 특정 카테고리의 모든 브랜드 가져오기
	@Transactional
	public List<BrandDto> getBrandByCategoryId(Long id) {
		Category category = categoryRepository.findById(id).orElseThrow(EntityNotFoundException::new);
		List<Brand> list = brandRepository.findByCategory(category);

		List<BrandDto> result = new ArrayList<>();

		for (Brand brand : list) {
			result.add(new BrandDto(brand.getCategory().getId(), brand.getId(), brand.getBrandname()));
		}

		return result;
	}

	// 게시글 추천 -> 이미 추천한 상태가 아닐 경우에만 추천
	@Transactional
	public boolean addRecommend(Long id, Account account) {
		Post post = postRepository.findById(id).orElseThrow(EntityNotFoundException::new);

		boolean isExist = isRecommended(post, account);

		if (isExist) {
			return false;
		}

		RecommendPost recommend = RecommendPost.builder().post(post).account(account).build();
		recommendPostRepo.save(recommend);

		return true;
	}

	// 게시글 추천 취소
	@Transactional
	public boolean deleteRecommend(Long post_id, Account account) {
		Post post = postRepository.findById(post_id).orElseThrow(EntityNotFoundException::new);

		int delete = recommendPostRepo.deleteByPostAndAccount(post, account);

		if (delete == 1) {
			return true;
		} else {
			return false;
		}
	}

	// 사용자가 특정 게시글을 추천했는지
	@Transactional
	public boolean isRecommended(Post post, Account account) {
		return account == null ? false : recommendPostRepo.existsByPostAndAccount(post, account);
	}

	// 레시피 찜하기 -> 이미 찜한 상태가 아닐 경우에만 찜하기
	@Transactional
	public boolean addLike(Long id, Account account) {
		Post post = postRepository.findById(id).orElseThrow(EntityNotFoundException::new);

		boolean isExist = isLiked(post, account);

		if (isExist) {
			return false;
		}

		LikeRecipe like = LikeRecipe.builder().post(post).account(account).build();
		likeRepository.save(like);

		return true;
	}

	// 찜한 레시피 삭제
	@Transactional
	public boolean deleteLike(Long post_id, Account account) {
		Post post = postRepository.findById(post_id).orElseThrow(EntityNotFoundException::new);

		int delete = likeRepository.deleteByPostAndAccount(post, account);

		if (delete == 1) {
			return true;
		} else {
			return false;
		}
	}

	// 사용자가 특정 게시글을 찜했는지
	public boolean isLiked(Post post, Account account) {
		return account == null ? false : likeRepository.existsByPostAndAccount(post, account);
	}

	// 태그 이름 String으로 반환
	public List<String> getTagByPost(List<Tag> list) {
		List<String> result = new ArrayList<>();

		for (Tag tag : list) {
			result.add(tag.getTagname());
		}

		return result;

	}

	// 조회수 증가
	@Transactional
	public View increaseView(Post post, Account account) {
		Optional<View> optionalView = viewRepo.findByPostAndAccount(post, account);
		View view = null;
		
		if (optionalView.isPresent()) {
			view = optionalView.get();
			
			if (account != null) {
				// 처음 방문이 아닐 경우
				LocalDateTime currentDateTime = LocalDateTime.now(); // 현재 시간
				System.out.println("현재 시간: " + currentDateTime);

				Long viewTime = ChronoUnit.MINUTES.between(view.getUpdateAt(), currentDateTime); // 수정된 시간과의 차이
//				Long viewTime = view.getUpdateAt().until(currentDateTime, ChronoUnit.MINUTES); 
				
				System.out.println("조회수 수정된 시간: " + view.getUpdateAt());
				System.out.println("시간 차이: " + viewTime);
				
				// 레시피를 조회한지 15분이 지난 경우
				if (viewTime > 15) {
					view.update(view.getCount() + 1);
					viewRepo.save(view);
				}
				
			} else {
				// 로그인 하지 않은 경우에도 조회수 증가
				view.update(view.getCount() + 1);
				viewRepo.save(view);
			}
			
		} else {
			// 처음 방문할 경우 -> view에 count를 1로 넣기 / 로그인 하지 않은 경우에도 조회수 삽입
			view = viewRepo.save(View.builder().post(post).account(account).count(1).build());
		}
		
		return view;
	}
	
	// 조회수 가져오기
	private int getViewCount(Post post) {
		int result = 0;
		
		List<View> viewList = viewRepo.findByPost(post);
		for (View v : viewList) {
			result += v.getCount();
		}
		
		return result;
	}

}
