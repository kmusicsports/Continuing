package com.example.continuing.service;

import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MailService {

	private final JavaMailSender sender;
	
	public boolean sendMail(String mailAddress, String subject, String text) {
		
		MimeMessage message = sender.createMimeMessage();
	    MimeMessageHelper helper = null;
	    
		try {
			
			helper = new MimeMessageHelper(message, true);
			helper.setTo(mailAddress);
			helper.setSubject(subject);
			helper.setText(text, true);

			sender.send(message);
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
}
