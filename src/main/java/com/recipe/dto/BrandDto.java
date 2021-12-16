package com.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class BrandDto {
	private Long categoryId;
	private Long brandId;
	private String brandName;
	
	@Getter @Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Detail {
		private Long brandId;
		private String brandName;
	}
}
