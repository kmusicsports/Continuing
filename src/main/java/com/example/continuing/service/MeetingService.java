package com.example.continuing.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.continuing.common.Utils;
import com.example.continuing.comparator.MeetingsComparator;
import com.example.continuing.entity.Deliveries;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Records;
import com.example.continuing.entity.Users;
import com.example.continuing.form.MeetingData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.DeliveriesRepository;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.RecordsRepository;
import com.example.continuing.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingService {
	
	private final UsersRepository usersRepository;
	private final RecordsRepository recordsRepository;
	private HttpSession session;
	private final MeetingsRepository meetingsRepository;
	private final JoinService joinService;
	private final MeetingsComparator meetingsComparator;
	private final MailService mailService;
	private final MessageSource messageSource;
	private final DeliveriesRepository deliveriesRepository;
	
	@Value("${app.url}")
	private String APP_URL;

	// ミーティングフォーム用のチェック
	public boolean isValid(MeetingData meetingData, boolean isCreate, 
			BindingResult result, Locale locale) {
		boolean answer = true;
		
		if(!meetingData.getPassword().equals(meetingData.getPasswordAgain())) {
			// パスワード不一致
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"passwordAgain",
					messageSource.getMessage("Unmatch.password", null, locale));
			result.addError(fieldError);
			meetingData.setPassword(null);
			meetingData.setPasswordAgain(null);
			answer = false;
		}
		
		int duration = Utils.string2Int(meetingData.getEndTime()) - Utils.string2Int(meetingData.getStartTime());
		if(meetingData.getNumberPeople() == 2 && (duration < 15 || duration > 40)) {
			// 複数人でのミーティングにおいて、ミーティング時間が15分以上40分以下でない
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"startTime",
					messageSource.getMessage("Time.Length.2.startTime", null, locale));
			result.addError(fieldError);
			fieldError = new FieldError(
					result.getObjectName(),
					"endTime",
					messageSource.getMessage("Time.Length.2.endTime", null, locale));
			result.addError(fieldError);
			meetingData.setStartTime(null);
			meetingData.setEndTime(null);
			answer = false;
		} else if(meetingData.getNumberPeople() == 1 && (duration < 15 || duration > 1800)) {
			// 1対1でのミーティングにおいて、ミーティング時間が15分以上1800分(30時間)以下でない
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"startTime",
					messageSource.getMessage("Time.Length.1.startTime", null, locale));
			result.addError(fieldError);
			fieldError = new FieldError(
					result.getObjectName(),
					"endTime",
					messageSource.getMessage("Time.Length.1.endTime", null, locale));
			result.addError(fieldError);
			meetingData.setStartTime(null);
			meetingData.setEndTime(null);
			answer = false;
		}
		
		String date = meetingData.getDate().replace("/", "-");
        LocalDate localeDate = null;
        try {
        	LocalDate today = LocalDate.now();
        	localeDate = LocalDate.parse(date);
            if (isCreate && localeDate.isBefore(today)) {
            	// ミーティングの日付が作成日以前
            	FieldError fieldError = new FieldError(
    					result.getObjectName(),
    					"date",
    					messageSource.getMessage("Previous.date", null, locale));
    			result.addError(fieldError);
            	meetingData.setDate(null);
                answer =  false;
            }
        } catch (DateTimeException e) {
        	// ミーティングの日付がDate型に変換できない
        	FieldError fieldError = new FieldError(
					result.getObjectName(),
					"date",
					messageSource.getMessage("InvalidFormat.date", null, locale));
			result.addError(fieldError);
        	meetingData.setDate(null);
        	e.printStackTrace();
            answer =  false;
        }
        
        return answer;
	}
	
	// 検索条件のチェック
	public boolean isValid(SearchData searchData, BindingResult result, Locale locale) {
		boolean answer = true;
		
		String date = searchData.getDate().replace("/", "-");
		if (!date.equals("")) {
			try {
				LocalDate.parse(date);
			} catch (DateTimeException e) {
				FieldError fieldError = new FieldError(
						result.getObjectName(),
						"date",
						messageSource.getMessage("InvalidFormat.date", null, locale));
				result.addError(fieldError);
				searchData.setDate(null);
				e.printStackTrace();
				answer =  false;
			}			
		}
        
        if (!searchData.getStartTime().equals("") && !Utils.checkTimeFormat(searchData.getStartTime())) {
        	FieldError fieldError = new FieldError(
					result.getObjectName(),
					"startTime",
					messageSource.getMessage("Pattern.meetingData.startTime", null, locale));
			result.addError(fieldError);
        	searchData.setStartTime(null);
        	answer = false;
        }
        
        if (!searchData.getEndTime().equals("") && !Utils.checkTimeFormat(searchData.getEndTime())) {
        	FieldError fieldError = new FieldError(
					result.getObjectName(),
					"endTime",
					messageSource.getMessage("Pattern.meetingData.endTime", null, locale));
			result.addError(fieldError);
        	searchData.setEndTime(null);
        	answer = false;
        }
		
        return answer;
	}
	
	// ミーティングへの参加かどうかのチェック
	public String joinCheck(Meetings meeting, Integer userId, Locale locale) {
		final SimpleDateFormat stf = new SimpleDateFormat("HH:mm");
		
		LocalDate localDate = LocalDate.parse(meeting.getDate().toString());
		LocalDate localToday = LocalDate.now();
		if(meeting.getHost().getId() == userId && meeting.getJoinList().isEmpty()) {
			return messageSource.getMessage("msg.w.no_join", null, locale);
		}
		if(localDate.isEqual(localToday)) {
			String strStartTime = stf.format(meeting.getStartTime());
			String strNow = stf.format(new java.util.Date());
			int duration = Utils.string2Int(strStartTime) - Utils.string2Int(strNow);
			if(duration >= 0 && duration <= 15) {
				Users user = usersRepository.findById(userId).get();
				Optional<Records> someRecord = recordsRepository.findByUserAndTopic(user, meeting.getTopic());
				Records record = new Records(user, meeting.getTopic());
				if (someRecord.isPresent()) {
					record = someRecord.get();
				}
				if (session.getAttribute(strStartTime) == null) {
					System.out.println("Join");
					session.setAttribute(strStartTime, "first join in this meeting");
					session.setMaxInactiveInterval(24 * 60 - Utils.string2Int(strNow));
					record.setDays(record.getDays() + 1);
					
					java.util.Date date = new java.util.Date();
			        Timestamp timestamp = new Timestamp(date.getTime());
					record.setUpdatedAt(timestamp);
					
					recordsRepository.saveAndFlush(record);
					
					if(session.getAttribute(localDate.toString()) == null) {
						session.setAttribute(localDate.toString(), "today's first join");
						user.setContinuousDays(user.getContinuousDays() + 1);
						usersRepository.saveAndFlush(user);					
					}					
				}
				
				return null;
			} else {
				return messageSource.getMessage("msg.w.advance_15", null, locale);
			}
		} else {
			return messageSource.getMessage("msg.w.meeting_not_today", null, locale);
		}
	}
	
	public void sendMail(Meetings meeting, Users user, String type, Locale locale) {
		Deliveries deliveries = null;
		String username = user.getName();
		String subject = null;
		String messageText = "<html><head></head><body>";
		String meetingInfo = "<br>"
				+ messageSource.getMessage("mail.msg.meeting_host", null, locale)
				+ " : " + meeting.getHost().getName() + "<br>" 
				+ messageSource.getMessage("mail.msg.meeting_topic", null, locale) 
				+ " : " 
				+ messageSource.getMessage("option.topic." + meeting.getTopic(), null, locale) 
				+ "<br>"
				+ messageSource.getMessage("mail.msg.meeting_date", null, locale)
				+ " : " + Utils.date2str(meeting.getDate()) + "<br>"
				+ messageSource.getMessage("mail.msg.meeting_time", null, locale)
				+ " : " + Utils.time2str(meeting.getStartTime()) + "～" + Utils.time2str(meeting.getEndTime()) + "<br>"
				+ "<br>";
		
		if(type == null) {
			type = "error";
		}
		
		switch(type) {
			case "create":
				deliveries = deliveriesRepository.findByUserId(user.getId()).get();
				if(deliveries.getMeetingCreated() == 1) {
					subject = meeting.getHost().getName() + " " 
							+ messageSource.getMessage("mail.subject.meeting_created", null, locale);
					messageText += meeting.getHost().getName() + " " 
							+ messageSource.getMessage("mail.msg.meeting_created", null, locale) 
							+ "<br>"
							+ meetingInfo
							+ "<a href='" + APP_URL + "/Meeting/" + meeting.getId() + "'>" 
							+ messageSource.getMessage("mail.msg.go_join", null, locale) 
							+ "</a>"
							+ "</body></html>";
					
					mailService.sendMail(user.getEmail(), subject, messageText);					
				}
				break;
			case "delete":
				deliveries = deliveriesRepository.findByUserId(user.getId()).get();
				if(deliveries.getMeetingDeleted() == 1) {
					subject = messageSource.getMessage("mail.subject.meeting_deleted", null, locale);
					messageText += messageSource.getMessage("mail.msg.meeting_deleted", null, locale)
							+ "<br>"
							+ meetingInfo
							+ "<a href='" + APP_URL + "/home'>" 
							+ messageSource.getMessage("mail.msg.go_find_replacement", null, locale) 
							+ "</a>"
							+ "</body></html>";
					
					mailService.sendMail(user.getEmail(), subject, messageText);					
				}
				break;
			case "join":
				deliveries = deliveriesRepository.findByUserId(meeting.getHost().getId()).get();
				if(deliveries.getMeetingJoined() == 1) {
					subject = messageSource.getMessage("mail.subject.meeting_joined", null, locale);
					messageText += messageSource.getMessage("mail.msg.meeting_joined_start", null, locale) 
							+ username + " "
							+ messageSource.getMessage("mail.msg.meeting_joined_end", null, locale) 
							+ "<br>"
							+ meetingInfo
//					+ "参加を拒否する場合は<a href='https://" + APP_URL + "/'>こちら</a>";
							+ "</body></html>";
					
					mailService.sendMail(meeting.getHost().getEmail(), subject, messageText);					
				}
				break;
			case "leave":
				deliveries = deliveriesRepository.findByUserId(meeting.getHost().getId()).get();
				if(deliveries.getMeetingLeft() == 1) {
					subject = username + " " 
							+ messageSource.getMessage("mail.subject.meeting_left", null, locale);
					messageText += username + " " 
							+ messageSource.getMessage("mail.msg.meeting_left", null, locale) 
							+ "<br>"
							+ meetingInfo
							+ "</body></html>";
					
					mailService.sendMail(meeting.getHost().getEmail(), subject, messageText);					
				}
				break;
			default:
				subject = messageSource.getMessage("mail.subject.error", null, locale);
				messageText += messageSource.getMessage("mail.msg.operation_error", null, locale)
						+ "</body></html>";
				mailService.sendMail(meeting.getHost().getEmail(), subject, messageText);		
		}
	}
	
	public List<Meetings> getUserMeetingList(Users user) {
		List<Meetings> userMeetingList = new ArrayList<>();
		Date today = new Date(System.currentTimeMillis());
		LocalDate localToday = LocalDate.now();
		
		List<Meetings> hostMeetingList = meetingsRepository.findByHostAndDateGreaterThanEqual(user, today);
		List<Meetings> joinMeetingList = joinService.getJoinMeetingList(user.getId());
		userMeetingList.addAll(hostMeetingList);
		for(Meetings meeting : joinMeetingList) {
			LocalDate localDate = LocalDate.parse(meeting.getDate().toString());
			if(!localDate.isBefore(localToday)) {
				userMeetingList.add(meeting);
			}
		}
		Collections.sort(userMeetingList, meetingsComparator);
		
		return userMeetingList;
		
	}
	
	public List<Meetings> getTodayMeetingList(Users user) {
		List<Meetings> todayMeetingList = new ArrayList<>();
		Date today = new Date(System.currentTimeMillis());
		LocalDate localToday = LocalDate.now();
		
		List<Meetings> todayHostMeetingList = meetingsRepository.findByHostAndDate(user, today);
		List<Meetings> joinMeetingList = joinService.getJoinMeetingList(user.getId());  
		
		todayMeetingList.addAll(todayHostMeetingList);
		for(Meetings meeting : joinMeetingList) {
			LocalDate localDate = LocalDate.parse(meeting.getDate().toString());
			if(localDate.isEqual(localToday)) {
				todayMeetingList.add(meeting);
			}
		}
		Collections.sort(todayMeetingList, meetingsComparator);
		
		return todayMeetingList;
		
	}
	
}
