package com.recipe.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
public class PostDto {
	private String title;
	private String content;
	private String imageUrl;
	private int price;
	private List<String> tags = new ArrayList<>();
	private Long category_id;
	private Long brand_id;
	
	@Getter @Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DetailResponse {
		private Long postId;
		private CategoryDto category;
		private BrandDto.Detail brand;
		private String title;
		private String content;
		private String imageUrl;
		private int price;
		private int countOfRecommend;
		private int view;
		private List<String> tags = new ArrayList<>();
		private int countOfComment;
		private String createAt;
		private String authorImage;
		private String nickname;
		private boolean isAuthor;
		private boolean isRecommended;
		private boolean isLiked;
	}
	
	@Getter @Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ListResponse {
		private Long postId;
		private Long categoryId;
		private String categoryName;
		private Long brandId;
		private String brandName;
		private String title;
		private String imageUrl;
		private int countOfRecommend;
		private int view;
		private String nickname;
	}
	
}
