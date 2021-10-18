package com.example.continuing.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class ContactData {

	@NotBlank
	@Email
	private String email;
	
	@NotBlank
	private String contents; 
	
}
