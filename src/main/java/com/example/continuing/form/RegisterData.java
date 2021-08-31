package com.example.continuing.form;

import java.sql.Timestamp;
import java.util.Date;

import com.example.continuing.entity.Users;

import lombok.Data;

@Data
public class RegisterData {

	private String name;
	private String email;
	private String password;
	private String passwordAgain;
	private boolean checked;
	
	public Users toEntity() {
		Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        Users user = new Users(null, this.name, this.email, this.password, null, null, null, timestamp, timestamp);
		return user;
	}
}

