package com.example.continuing.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.continuing.common.Utils;
import com.example.continuing.entity.Temporaries;
import com.example.continuing.entity.Users;
import com.example.continuing.form.LoginData;
import com.example.continuing.form.RegisterData;
import com.example.continuing.repository.TemporariesRepository;
import com.example.continuing.repository.UsersRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class LoginService {
	
	private final UsersRepository usersRepository;
	private final PasswordEncoder passwordEncoder;
	private final MailService mailService;
	private final MessageSource messageSource;
	private final TemporariesRepository temporariesRepository;

	@Value("${app.name}")
	private String APP_NAME;
	
	@Value("${app.url}")
	private String APP_URL;
	
	// ログインチェック
	public boolean isValid(LoginData loginData, BindingResult result) {
		Optional<Users> someUser = usersRepository.findByEmail(loginData.getEmail());
    	if(!someUser.isPresent()) {
    		// 登録されていない
			System.out.println("email is wrong");
			return false;
    	}
    	
    	// パスワードが正しいか？ 
    	if (!passwordEncoder.matches(loginData.getPassword(), someUser.get().getPassword())) {
    		System.out.println("password is wrong");
    		return false;
    	}
    	
    	return true;
	}
	
	// 登録画面用のチェック
	public boolean isValid(RegisterData registerData, BindingResult result, Locale locale) {
		if(!registerData.getPassword().equals(registerData.getPasswordAgain())) {
			// パスワード不一致
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"passwordAgain",
					messageSource.getMessage("Unmatch.password", null, locale));
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
					messageSource.getMessage("AlreadyUsed.name", null, locale));
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
						messageSource.getMessage("DoubleSpace.name", null, locale));
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
					messageSource.getMessage("AlreadyUsed.email", null, locale));
			result.addError(fieldError);
			registerData.setEmail(null);
			return false;
		}
		
		return true;
	}

	public String sendMail(String email, String type, Locale locale) {
		String token = null;
		String subject = null;;
		String messageText = "<html><head></head><body>";
		switch(type) {
			case "welcome":
				subject = messageSource.getMessage("mail.subject.regist_thanks", null, locale);
				messageText += "<h3>" 
						+ messageSource.getMessage("mail.msg.welcome_start", null, locale)
						+ " " + APP_NAME
						+ messageSource.getMessage("mail.msg.welcome_end", null, locale) 
						+ "</h3>"
						+ messageSource.getMessage("mail.msg.regist_successful", null, locale)
						+ "<br>"
						+ "<br>"
						+ "<a href='" + APP_URL + "/User/mypage'>"
						+ messageSource.getMessage("mail.msg.go_app_start", null, locale)
						+ " " + APP_NAME
						+ messageSource.getMessage("mail.msg.go_app_end", null, locale)
						+ "</a>";
				break;
			case "reset-password":
				token = UUID.randomUUID().toString();
				subject = messageSource.getMessage("mail.subject.reset_password", null, locale);
				messageText += messageSource.getMessage("mail.msg.reset_password", null, locale)
						+ "<br>"
						+ "<br>"
						+ "<a href='" + APP_URL + "/reset-password"
						+ "/email/" + email 
						+ "/token/" + token
						+ "'>"
						+ APP_URL + "/reset-password"
						+ "/email/" + email 
						+ "/token/" + token
						+ "</a>";
				break;
			default:
				subject = messageSource.getMessage("mail.subject.error", null, locale);
				messageText += messageSource.getMessage("mail.msg.operation_error", null, locale);
		}
		
		messageText += "</body></html>";
		mailService.sendMail(email, subject, messageText);
		
		return token;
	}
	
	public boolean isValid(String email, String token) {
		List<Temporaries> temporariesList = temporariesRepository.findByEmailOrderByCreatedAtDesc(email);
		if(temporariesList.size() != 0) {
			Temporaries latestTemporaries = temporariesList.get(0);
			return latestTemporaries.getToken().equals(token);
		} else {
			System.out.println("");
			return false;
		}
	}
	
}
