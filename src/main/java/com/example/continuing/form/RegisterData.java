package com.example.continuing.form;

import java.sql.Timestamp;
import java.util.Date;

import com.example.continuing.entity.Users;

import lombok.Data;

@Data
public class RegisterData {

	private String name;
	private String email;
	private String password1;
	private String password2;
	private boolean checked;
	
	public Users toEntity() {
		Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        Users user = new Users(null, this.name, this.email, this.password1, null, null, null, timestamp, timestamp);
		return user;
	}
}

