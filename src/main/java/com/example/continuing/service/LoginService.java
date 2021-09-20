package com.example.continuing.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.continuing.common.Utils;
import com.example.continuing.entity.Users;
import com.example.continuing.form.LoginData;
import com.example.continuing.form.RegisterData;
import com.example.continuing.repository.UsersRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class LoginService {
	
	private final UsersRepository usersRepository;
	private final PasswordEncoder passwordEncoder;
	private final MailService mailService;

	@Value("${app.url}")
	private String APP_URL;
	
	// ログインチェック
	public boolean isValid(LoginData loginData, BindingResult result) {
		Optional<Users> someUser = usersRepository.findByEmail(loginData.getEmail());
    	if(!someUser.isPresent()) {
    		// 登録されていない
			System.out.println("メールアドレスが違います");
			return false;
    	}
    	
    	// パスワードが正しいか？ 
    	if (!passwordEncoder.matches(loginData.getPassword(), someUser.get().getPassword())) {
    		System.out.println("パスワードが違います");
    		return false;
    	}
    	
    	return true;
	}
	
	// 登録画面用のチェック
	public boolean isValid(RegisterData registerData, BindingResult result) {
		if(!registerData.getPassword().equals(registerData.getPasswordAgain())) {
			// パスワード不一致
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"passwordAgain",
					"パスワードが一致しません");
			result.addError(fieldError);
			registerData.setPassword(null);
			registerData.setPasswordAgain(null);
			return false;
		}
		
		Optional<Users> someUser;
		String name = registerData.getName(); 
		someUser = usersRepository.findByName(name);
		if(someUser.isPresent()) {
			// 既に同じ名前が登録されている ->　別の名前で登録してください
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"name",
					"既に使用されている名前です");
			result.addError(fieldError);
			registerData.setName(null);
			return false;
		}
		
		// 名前が全角スペースだけで構成されていたらエラー
		if (!Utils.isBlank(name)) {
			if (Utils.isAllDoubleSpace(name)) {
				FieldError fieldError = new FieldError(
						result.getObjectName(),
						"name",
						"名前が全角スペースです");
				result.addError(fieldError);
				return false;
			}
		}
		
		someUser = usersRepository.findByEmail(registerData.getEmail());		
		if(someUser.isPresent()) {
			// 既にemailアドレスが登録されている ->　別のemailアドレスで登録してください
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"email",
					"既に登録されているメールアドレスです");
			result.addError(fieldError);
			registerData.setEmail(null);
			return false;
		}
		
		return true;
	}

	public String sendMail(String email, String type) {
		String token = null;
		String subject = null;;
		String messageText = "<html><head></head><body>";
		if(type.equals("welcome")) {
			subject = "Welcom to Continuing!";
			messageText += "<h3>Welcom to Continuing!</h3>"
					+ "You're officially a Continuing user.<br>"
					+ "<br>"
					+ "<a href='" + APP_URL + "/User/mypage'>Go to Continuing!</a>";
		} else {
			subject = "Something is wrong!";
			messageText += "Something is wrong!";
		}
		
		messageText += "</body></html>";
		mailService.sendMail(email, subject, messageText);
		
		return token;
	}
	
}
