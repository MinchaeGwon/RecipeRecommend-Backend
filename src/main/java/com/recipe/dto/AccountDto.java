package com.recipe.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
	private Long id;
	private String loginid;
	private String type;
	private String email;
	private String nickname;
	private String imageUrl;
}
