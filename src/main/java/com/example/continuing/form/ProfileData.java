package com.example.continuing.form;

import java.sql.Timestamp;
import java.util.Date;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.continuing.entity.Users;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileData {
	@NotBlank
	private String name;
	
	@NotBlank
	@Email
	private String email;
	
	private String profileImage;
	
	@Length(min = 0, max = 140)
	private String profileMessage;
	
	private String newPassword;
	private String newPasswordAgain;
	private Timestamp createdAt;
	
	public ProfileData(Users user) {
		this.name = user.getName();
		this.email = user.getEmail();
		this.profileImage = user.getProfileImage();
		this.profileMessage = user.getProfileMessage();
		this.newPassword = null;
		this.newPasswordAgain = null;
	}
	
	public Users toEntity(Users oldData, PasswordEncoder passwordEncoder) {
		Users user = new Users();
		user.setId(oldData.getId());
		user.setName(name);
		user.setEmail(email);
		user.setProfileImage(profileImage);
		user.setProfileMessage(profileMessage);
		user.setContinuousDays(oldData.getContinuousDays());
		user.setCreatedAt(oldData.getCreatedAt());
		
		if (newPassword == null || newPassword.equals("")) {
			user.setPassword(oldData.getPassword());			
		} else {
			user.setPassword(passwordEncoder.encode(newPassword));
		}
		
		Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
		user.setUpdatedAt(timestamp);
		
		return user;
	}
}
