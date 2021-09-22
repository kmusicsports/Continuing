package com.example.continuing.schedulingtasks;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
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
	private final MessageSource messageSource;
	
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
			Locale locale = new Locale(user.getLanguage());
			List<Meetings> todayMeetingList = meetingService.getTodayMeetingList(user);
			if (todayMeetingList.size() != 0) {
				String messageText = "<html><head></head><body>"
						// "Hello,"
						+ messageSource.getMessage("mail.msg.hello_start", null, locale)
						+ " " + user.getName() 
						// "!"
						+ messageSource.getMessage("mail.msg.hello_end", null, locale)
						+ "<br>"
						// "You have "
						+ messageSource.getMessage("mail.msg.today_meeting_start", null, locale)
						+ todayMeetingList.size() 
						// " meetings scheduled for today."
						+ messageSource.getMessage("mail.msg.today_meeting_end", null, locale)
						+ "<br>"
						+ "<br>"
						+ "<a href='" + APP_URL + "/Meeting/list/mine/today'>" 
						// "Go check out today's meeting!"
						+ messageSource.getMessage("mail.msg.today_meeting_check", null, locale)
						+ "</a>"
						+ "</body></html>";
				mailService.sendMail(user.getEmail(), "Today's meeting", messageText);				
			}
		}	
	}
}
