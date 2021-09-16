package com.example.continuing.form;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.continuing.entity.Users;

import lombok.Data;

@Data
public class RegisterData {

	private String name;
	private String email;
	private String password;
	private String passwordAgain;
	private boolean checked;
	
	public Users toEntity(PasswordEncoder passwordEncoder) {
		Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        Users user = new Users();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setContinuousDays(0);
        user.setCreatedAt(timestamp);
        user.setUpdatedAt(timestamp);
		return user;
	}
}

