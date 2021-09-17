package com.example.continuing.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.continuing.common.Utils;
import com.example.continuing.entity.Users;
import com.example.continuing.form.RegisterData;
import com.example.continuing.repository.UsersRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class LoginService {
	
	private final UsersRepository usersRepository;
	private final MailService mailService;
	private final static String MESSAGE_TEXT = "<html>"
			+ "<head></head>"
			+ "<body>"
			+ "<h3>Welcom to Continuing!</h3>"
			+ "<p>You're officially a Continuing user.</p>"
			+ "<a href='https://www.yahoo.co.jp'>Go to Yahoo</a>"
			+ "</body>"
			+ "</html>";

	// 登録画面用のチェック
	public boolean isValid(RegisterData registerData) {
		Boolean answer = true;
		
		if(!registerData.isChecked()) {
			System.out.println("Error: 利用規約に同意して、チェックを入れてください");
			answer = false;
		}
		
		if(!registerData.getPassword().equals(registerData.getPasswordAgain())) {
			// パスワード不一致
			System.out.println("Error: パスワードが一致しません");
			registerData.setPassword(null);
			registerData.setPasswordAgain(null);
			answer = false;
		}
		
		Optional<Users> someUser;
		String name = registerData.getName(); 
		someUser = usersRepository.findByName(name);
		if(someUser.isPresent()) {
			// 既に同じ名前が登録されている ->　別の名前で登録してください
			System.out.println("Error: 既に登録されている名前です");
			registerData.setName(null);
			answer = false;
		}
		
		// 名前が全角スペースだけで構成されていたらエラー
		if (!Utils.isBlank(name)) {
			if (Utils.isAllDoubleSpace(name)) {
				answer = false;
			}
		}
		
		someUser = usersRepository.findByEmail(registerData.getEmail());		
		if(someUser.isPresent()) {
			// 既にemailアドレスが登録されている ->　別のemailアドレスで登録してください
			System.out.println("Error: 既に登録されているメールアドレスです");
			registerData.setEmail(null);
			answer = false;
		} else {
			if(!mailService.sendMail(registerData.getEmail(), "Welcom to Continuing!", MESSAGE_TEXT)) {
				System.out.println("Error: メールが送信できませんでした");
				answer = false;
			}
		}
		
		return answer;
	}

}
