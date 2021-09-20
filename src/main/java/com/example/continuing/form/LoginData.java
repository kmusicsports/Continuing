package com.example.continuing.form;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class LoginData {

	@NotBlank
	private String email;
	
	@NotBlank
	private String password;
}
