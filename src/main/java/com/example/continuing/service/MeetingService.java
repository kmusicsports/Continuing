package com.example.continuing.service;

import org.springframework.stereotype.Service;

import com.example.continuing.common.Utils;
import com.example.continuing.form.MeetingData;

@Service
public class MeetingService {

	// ミーティングフォーム用のチェック
	public boolean isValid(MeetingData meetingData) {
		Boolean answer = true;
		
		if(meetingData.getTopicName().equals("0")) {
			// トピック不選択
			System.out.println("Error: トピックを選択してください");
			answer = false;
		}
		
		if(!meetingData.getPassword().equals(meetingData.getPasswordAgain())) {
			// パスワード不一致
			System.out.println("Error: パスワードが一致しません");
			meetingData.setPassword(null);
			meetingData.setPasswordAgain(null);
			answer = false;
		}
		
		int duration = Utils.stringToInt(meetingData.getEndTime()) - Utils.stringToInt(meetingData.getStartTime());
		if(meetingData.getNumberPeople() == 2 && duration > 40) {
			System.out.println("Error: 複数人でのミーティングは最大40分です");
			meetingData.setStartTime(null);
			meetingData.setEndTime(null);
			answer = false;
		}
		
		return answer;
	}
}
