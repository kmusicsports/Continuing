package com.example.continuing.schedulingtasks;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.repository.RecordsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.MailService;
import com.example.continuing.service.MeetingService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

	private final UsersRepository usersRepository;
	private final RecordsRepository recordsRepository;
	private final MeetingService meetingService;
	private final MailService mailService;
	
	@Value("${app.url}")
	private String APP_URL;
	
	@Scheduled(cron = "${cron.every.month}")
	public void resetDays() {
		
		recordsRepository.deleteAll();
		List<Users> userList = usersRepository.findAll();
		for(Users user : userList) {
			user.setContinuousDays(0);
		}
		usersRepository.saveAllAndFlush(userList);
	}
	
	@Scheduled(cron = "${cron.every.day}")
	public void sendDailyMail() {
		
		List<Users> userList = usersRepository.findAll();
		for(Users user : userList) {
			List<Meetings> todayMeetingList = meetingService.getTodayMeetingList(user);
			if (todayMeetingList.size() != 0) {
				String messageText = "<html><head></head><body>"
						+ user.getName()+ "さん、こんにちは<br>"
						+ "今日は" + todayMeetingList.size() + "件のミーティングに参加予定です。<br>"
						+ "<br>"
						+ "<a href='" + APP_URL + "/Meeting/list/mine/today'>今日参加予定のミーティングをチェックしに行こう!</a>"
						+ "</body></html>";
				mailService.sendMail(user.getEmail(), "Continuing - 今日参加予定のミーティング", messageText);				
			}
		}	
	}
}
