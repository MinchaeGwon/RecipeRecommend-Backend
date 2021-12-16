package com.recipe.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SuccessResponse<T> {
	private String message;
	private T result;
	
	public SuccessResponse(T result) {
		this.message = "success";
		this.result = result;
	}

}
