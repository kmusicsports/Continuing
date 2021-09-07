package com.example.continuing.service;

import java.time.DateTimeException;
import java.time.LocalDate;

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
		
		int duration = Utils.string2Int(meetingData.getEndTime()) - Utils.string2Int(meetingData.getStartTime());
		if(meetingData.getNumberPeople() == 2 && duration > 40) {
			System.out.println("Error: 複数人でのミーティング時間は最大40分です");
			meetingData.setStartTime(null);
			meetingData.setEndTime(null);
			answer = false;
		} else if(meetingData.getNumberPeople() == 1 && duration > 1800) {
			System.out.println("Error: ミーティング時間は最大30時間(1800分)です");
			meetingData.setStartTime(null);
			meetingData.setEndTime(null);
			answer = false;
		}
		
		String date = meetingData.getDate().replace("/", "-");
		LocalDate today = LocalDate.now();
        LocalDate localeDate = null;
        try {
            // parseできればyyyy-mm-dd形式とみなす
        	localeDate = LocalDate.parse(date);
            if (localeDate.isBefore(today)) {
                // 過去日付なのでfalse
            	System.out.println("今日以降の日付を入力してください");
                answer =  false;
            }
        } catch (DateTimeException e) {
            // yyyy-mm-dd形式以外
        	System.out.println("Error: 日付はyyyy-mm-dd または　yyyy/mm/dd　の形式で入力してください");
        	e.printStackTrace();
            answer =  false;
        }
        
        if (!Utils.checkTimeFormat(meetingData.getEndTime()) || !Utils.checkTimeFormat(meetingData.getEndTime())) {
        	System.out.println("Error: 時間はHH:mm　の形式で入力してください");
        	answer = false;
        }
        
        return answer;
	}
}
