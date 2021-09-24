package com.example.continuing.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.example.continuing.entity.Follows;
import com.example.continuing.entity.Users;
import com.example.continuing.repository.FollowsRepository;
import com.example.continuing.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {
	
	private final FollowsRepository followsRepository;
	private final UsersRepository usersRepository;
	private final MailService mailService;
	private final MessageSource messageSource;

	@Value("${app.url}")
	private String APP_URL;
	
	// フォロー中のユーザーアカウントのリストを返す
	public List<Users> getFollowsList(Integer followerId) {
		List<Users> followsList = new ArrayList<>();
		List<Follows> followList = followsRepository.findByFollowerId(followerId);
		if(!followList.isEmpty()) {
			for(Follows follow : followList) {
				Users user = usersRepository.findById(follow.getFolloweeId()).get();
				followsList.add(user);
			}			
		}
		
		return followsList;
	}
	
	// フォロー中のユーザーアカウントのリストを返す
	public List<Users> getFollowersList(Integer followeeId) {
		List<Users> followersList = new ArrayList<>();
		List<Follows> followList = followsRepository.findByFolloweeId(followeeId);
		if(!followList.isEmpty()) {
			for(Follows follow : followList) {
				Users user = usersRepository.findById(follow.getFollowerId()).get();
				followersList.add(user);
			}			
		}
		
		return followersList;
	}
	
	public void sendMail(String followeeEmail, Users follower, Locale locale) {
		String subject = follower.getName() + " "
				// has followed you.
				+ messageSource.getMessage("mail.subject.followed_you", null, locale);
		String messageText = "<html><head></head><html><head></head><body>"
				+ "<a href='" + APP_URL + "/User/" + follower.getId() + "'>" 
				+ follower.getName() 
				// 's page
				+ messageSource.getMessage("mail.msg.follower_page", null, locale)
				+ "</a>"
				+ "</body></html>";
		
		mailService.sendMail(followeeEmail, subject, messageText);
	}
	
}
