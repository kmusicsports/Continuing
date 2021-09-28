package com.example.continuing.form;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ContactData {

	@NotBlank
	private String name;
	
	@NotBlank
	private String email;
	
	@NotBlank
	private String contents; 
	
}
