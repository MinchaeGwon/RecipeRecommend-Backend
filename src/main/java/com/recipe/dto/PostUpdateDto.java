package com.recipe.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostUpdateDto {
	private String title;
	private String content;
	private String imageUrl;
	private int price;
	private List<String> tags = new ArrayList<>();
}
