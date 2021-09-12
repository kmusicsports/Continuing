package com.example.continuing.service;

import java.time.DateTimeException;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.example.continuing.common.Utils;
import com.example.continuing.form.MeetingData;
import com.example.continuing.form.SearchData;

@Service
public class MeetingService {

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
		
		int duration = Utils.string2Int(meetingData.getEndTime()) - Utils.string2Int(meetingData.getStartTime());
		if(meetingData.getNumberPeople() == 2 && duration > 40) {
			System.out.println("Error: 複数人でのミーティング時間は最大40分です");
			meetingData.setStartTime("");
			meetingData.setEndTime("");
			answer = false;
		} else if(meetingData.getNumberPeople() == 1 && duration > 1800) {
			System.out.println("Error: ミーティング時間は最大30時間(1800分)です");
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
	
}
