package com.example.continuing.form;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EmailData {

	@NotBlank
	private String email;

	public EmailData(String email) {
		this.email = email;
	}
	
}
