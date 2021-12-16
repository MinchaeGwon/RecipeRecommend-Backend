package com.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
public class CommentDto {
	private Long post_id;
	private String content;
	
	@Getter @Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ListResponse {
		private Long commentId;
		private String content;
		private int countOfRecommend;
		private String authorImage;
		private String nickname;
		private boolean isAuthor;
		private boolean isRecommended;
		private String createAt;
	}
	
	@Getter @Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MyListResponse {
		private Long commentId;
		private String content;
		private int countOfRecommend;
		private String createAt;
	}
	
	@Getter @Setter
	public static class update {
		private String content;
	}
	
}
