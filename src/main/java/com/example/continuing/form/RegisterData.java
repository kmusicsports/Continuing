package com.example.continuing.form;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.continuing.entity.Temporaries;
import com.example.continuing.entity.Users;

import lombok.Data;

@Data
public class RegisterData {

	@NotBlank
	@Length(min = 1, max = 50)
	private String name;
	
	@NotBlank
	@Email
	private String email;
	
	@NotBlank
	@Length(min = 8, max = 32)
	@Pattern(regexp = "^[a-zA-Z0-9]+$")
	private String password;
	
	@NotBlank
	@Length(min = 8, max = 32)
	@Pattern(regexp = "^[a-zA-Z0-9]+$")
	private String passwordAgain;
	
	@AssertTrue
	private boolean checked;
	
	public Users toEntity(PasswordEncoder passwordEncoder, Locale locale) {
		Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        Users user = new Users();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setContinuousDays(0);
        user.setLanguage(locale.getLanguage());
        user.setCreatedAt(timestamp);
        user.setUpdatedAt(timestamp);
		return user;
	}
	
	public Temporaries toEntity(PasswordEncoder passwordEncoder, String token) {
		Temporaries temporary = new Temporaries();
		temporary.setName(name);
		temporary.setEmail(email);
		temporary.setPassword(passwordEncoder.encode(password));
		temporary.setToken(token);

        Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        temporary.setCreatedAt(timestamp);
		return temporary;
	}
	
}

