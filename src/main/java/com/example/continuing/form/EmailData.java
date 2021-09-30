package com.example.continuing.form;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class EmailData {

	@NotBlank
	private String email;
	
}
