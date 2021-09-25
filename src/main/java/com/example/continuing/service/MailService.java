package com.example.continuing.service;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailService {

	private final JavaMailSender sender;
	
	@Value("${spring.mail.username}")
	private String FROM_ADDRESS;
	
	private final static String FROM_NAME = "Continuing";
	private final static String ENCODE = "UTF-8";
	
	@Async
	public void sendMail(String mailAddress, String subject, String text) {
		
		MimeMessage message = sender.createMimeMessage();
	    MimeMessageHelper helper = null;
	    
		try {
			
			helper = new MimeMessageHelper(message, true);
			InternetAddress fromAddress = new InternetAddress(FROM_ADDRESS, FROM_NAME, ENCODE);
			helper.setFrom(fromAddress);
			helper.setTo(mailAddress);
			helper.setSubject(subject);
			helper.setText(text, true);

			sender.send(message);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}
