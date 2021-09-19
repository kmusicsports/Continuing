package com.example.continuing.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.continuing.common.Utils;
import com.example.continuing.comparator.MeetingsComparator;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Records;
import com.example.continuing.entity.Users;
import com.example.continuing.form.MeetingData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.RecordsRepository;
import com.example.continuing.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeetingService {
	
	private final UsersRepository usersRepository;
	private final RecordsRepository recordsRepository;
	private final HttpSession session;
	private final MeetingsRepository meetingsRepository;
	private final JoinService joinService;
	private final MeetingsComparator meetingsComparator;
	
	@Value("${app.url}")
	private String APP_URL;

	// ミーティングフォーム用のチェック
	public boolean isValid(MeetingData meetingData, boolean isCreate) {
		Boolean answer = true;
		
		if(meetingData.getTopicName().equals("0")) {
			System.out.println("Error: トピックを選択してください");
			answer = false;
		}
		
		if(!meetingData.getPassword().equals(meetingData.getPasswordAgain())) {
			System.out.println("Error: パスワードが一致しません");
			meetingData.setPassword("");
			meetingData.setPasswordAgain("");
			answer = false;
		}
		
		if(meetingData.getNumberPeople() < 1) {
			System.out.println("Error: 人数を選択してください");
			answer = false;
		}
		
		int duration = Utils.string2Int(meetingData.getEndTime()) - Utils.string2Int(meetingData.getStartTime());
		if(meetingData.getNumberPeople() == 2 && (duration < 15 || duration > 40)) {
			System.out.println("Error: 複数人でのミーティング時間は15~40分です");
			meetingData.setStartTime("");
			meetingData.setEndTime("");
			answer = false;
		} else if(meetingData.getNumberPeople() == 1 && (duration < 15 || duration > 1800)) {
			System.out.println("Error: ミーティング時間は15~1800分(30時間)です");
			meetingData.setStartTime("");
			meetingData.setEndTime("");
			answer = false;
		}
		
		String date = meetingData.getDate().replace("/", "-");
        LocalDate localeDate = null;
        try {
        	LocalDate today = LocalDate.now();
        	localeDate = LocalDate.parse(date);
            if (isCreate && localeDate.isBefore(today)) {
            	System.out.println("今日以降の日付を入力してください");
            	meetingData.setDate("");
                answer =  false;
            }
        } catch (DateTimeException e) {
        	System.out.println("Error: 日付はyyyy/mm/dd または　yyyy-mm-dd　の形式で入力してください");
        	meetingData.setDate("");
        	e.printStackTrace();
            answer =  false;
        }
        
        if (!Utils.checkTimeFormat(meetingData.getStartTime()) || !Utils.checkTimeFormat(meetingData.getEndTime())) {
        	System.out.println("Error: 時間はHH:mm　の形式で入力してください");
        	meetingData.setStartTime("");
			meetingData.setEndTime("");
        	answer = false;
        }
        
        return answer;
	}
	
	// 検索条件のチェック
	public boolean isValid(SearchData searchData) {
		Boolean answer = true;
		
		String date = searchData.getDate().replace("/", "-");
		if (!date.equals("")) {
			try {
				LocalDate.parse(date);
			} catch (DateTimeException e) {
				System.out.println("Error: 日付はyyyy/mm/dd または　yyyy-mm-dd　の形式で入力してください");
				e.printStackTrace();
				answer =  false;
			}			
		}
        
        if (!searchData.getStartTime().equals("") && !Utils.checkTimeFormat(searchData.getStartTime())) {
        	System.out.println("Error: 時間はHH:mm　の形式で入力してください");
        	searchData.setStartTime(null);
        	answer = false;
        }
        
        if (!searchData.getEndTime().equals("") && !Utils.checkTimeFormat(searchData.getEndTime())) {
        	System.out.println("Error: 時間はHH:mm　の形式で入力してください");
        	searchData.setEndTime(null);
        	answer = false;
        }
		
        return answer;
	}
	
	// ミーティングへの参加かどうかのチェック
	public void joinCheck(Meetings meeting, Integer userId) {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		final SimpleDateFormat stf = new SimpleDateFormat("HH:mm");
		
		String strMeetingDate = meeting.getDate().toString();
		String strToday = sdf.format(new java.util.Date());
		if(strMeetingDate.equals(strToday)) {
			String strStartTime = stf.format(meeting.getStartTime());
			String strNow = stf.format(new java.util.Date());
			int duration = Utils.string2Int(strStartTime) - Utils.string2Int(strNow);
			if(duration <= 15) {
				Users user = usersRepository.findById(userId).get();
				Optional<Records> someRecord = recordsRepository.findByUserAndTopic(user, meeting.getTopic());
				Records record = new Records(user, meeting.getTopic());
				if (someRecord.isPresent()) {
					record = someRecord.get();
				}
				if (session.getAttribute(strStartTime) == null) {
					System.out.println("参加");
					session.setAttribute(strStartTime, "not first");
					session.setMaxInactiveInterval(24 * 60 - Utils.string2Int(strNow));
					record.setDays(record.getDays() + 1);
					
					java.util.Date date = new java.util.Date();
			        Timestamp timestamp = new Timestamp(date.getTime());
					record.setUpdatedAt(timestamp);
					
					recordsRepository.saveAndFlush(record);
					
					if(session.getAttribute(strToday) == null) {
						session.setAttribute(strToday, "not today's first");
						user.setContinuousDays(user.getContinuousDays() + 1);
						usersRepository.saveAndFlush(user);					
					}					
				}
				
			} else {
				System.out.println("不参加");
			}
		} else {
			System.out.println("meeting is not today!");
		}
	}
	
	public String getMessageText(Meetings meeting, String username, String type) {
		String messageText = "<html><head></head><html><head></head><body>";
		String meetingInfo = "<br>"
				+ "作成者 : " + meeting.getHost().getName() + "<br>" 
				+ "トピック : " + meeting.getTopic() + "<br>"
				+ "日付 : " + Utils.date2str(meeting.getDate()) + "<br>"
				+ "時間 : " + Utils.time2str(meeting.getStartTime()) + "～" + Utils.time2str(meeting.getEndTime()) + "<br>"
				+ "<br>";
		if(type.equals("create")) {
			messageText += username + "様。<br>"
					+ "<br>"
					+ meeting.getHost().getName() + "さんが以下のミーティングを作成しました。<br>"
					+ meetingInfo
					+ "<a href='" + APP_URL + "/Meeting/" + meeting.getId() + "'>さっそく参加予約をしに行こう!</a>";
		} else if (type.equals("delete")) {
			messageText += username + "様。<br>" 
					+ "<br>"
					+ "参加予定だった以下のミーティングが削除されました。<br>"
					+ meetingInfo
					+ "<a href='" + APP_URL + "/home'>代わりのミーティングを探しに行こう!</a>";
		} else if(type.equals("join")) {
			messageText += "以下のミーティングに" + username + "さんが参加予約をしました。<br>"
					+ meetingInfo;
//					+ "参加を拒否する場合は<a href='https://www.yahoo.co.jp'>こちら</a>";
		} else if(type.equals("leave")) {
			messageText += username + "さんが以下のミーティングの参加予約を取り消しました。<br>"
					+ meetingInfo;
		} else {
			messageText += "Something is wrong!";
		}
		
		messageText += "</body></html>";
		return messageText;
	}
	
	public List<Meetings> getUserMeetingList(Users user) {
		List<Meetings> userMeetingList = new ArrayList<>();
		Date today = new Date(System.currentTimeMillis());
		
		List<Meetings> hostMeetingList = meetingsRepository.findByHostAndDateGreaterThanEqual(user, today);
		List<Meetings> joinMeetingList = joinService.getJoinMeetingList(user.getId());
		userMeetingList.addAll(hostMeetingList);
		for(Meetings meeting : joinMeetingList) {
			if(meeting.getDate().compareTo(today) == 1) {
				userMeetingList.add(meeting);
			}
		}
		Collections.sort(userMeetingList, meetingsComparator);
		
		return userMeetingList;
		
	}
	
	public List<Meetings> getTodayMeetingList(Users user) {
		List<Meetings> todayMeetingList = new ArrayList<>();
		Date today = new Date(System.currentTimeMillis());
		
		List<Meetings> todayHostMeetingList = meetingsRepository.findByHostAndDate(user, today);
		List<Meetings> joinMeetingList = joinService.getJoinMeetingList(user.getId());  
		
		todayMeetingList.addAll(todayHostMeetingList);
		for(Meetings meeting : joinMeetingList) {
			if(meeting.getDate().compareTo(today) == 0) {
				todayMeetingList.add(meeting);
			}
		}
		Collections.sort(todayMeetingList, meetingsComparator);
		
		return todayMeetingList;
		
	}
	
}
