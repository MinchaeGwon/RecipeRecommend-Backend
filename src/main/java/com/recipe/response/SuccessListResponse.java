package com.recipe.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SuccessListResponse<T> {
	private String message;
	private int count;
	private T result;
	
	public SuccessListResponse(int count, T result) {
		this.message = "success";
		this.count = count;
		this.result = result;
	}
}
