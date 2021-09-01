package com.example.continuing.form;

import java.sql.Timestamp;
import java.util.Date;

import com.example.continuing.entity.Users;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileData {

	private Integer id;
	private String name;
	private String email;
	private String profile_image;	
	private String profile_message;	
	private String new_password;
	private String new_password_again;
	private Timestamp created_at;
	
	public ProfileData(Users user) {
		this.id = user.getId();
		this.name = user.getName();
		this.email = user.getEmail();
		this.profile_image = user.getProfile_image();
		this.profile_message = user.getProfile_message();
		this.new_password = null;
		this.new_password_again = null;
	}
	
	public Users toEntity(Users oldData) {
		Users user = new Users();
		user.setId(oldData.getId());
		user.setName(name);
		user.setEmail(email);
		user.setProfile_image(profile_image);
		user.setProfile_message(profile_message);
		user.setContinuous_days(oldData.getContinuous_days());
		user.setCreated_at(oldData.getCreated_at());
		
		if (new_password == null || new_password.equals("")) {
			user.setPassword(oldData.getPassword());			
		} else {
			user.setPassword(new_password);
		}
		
		Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
		user.setUpdated_at(timestamp);
		
		return user;
	}
}
