package com.example.continuing.service;

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
	private final MessageSource messageSource;

	@Value("${app.name}")
	private String APP_NAME;
	
	@Value("${app.url}")
	private String APP_URL;
	
	// ログインチェック
	public boolean isValid(LoginData loginData, BindingResult result) {
		boolean isValid = true;
		
		Optional<Users> someUser = usersRepository.findByEmail(loginData.getEmail());
    	if(!someUser.isPresent()) {
    		// 登録されていない
			System.out.println("email is wrong");
			isValid = false;
    	}
    	
    	// パスワードが正しいか？ 
    	if (!passwordEncoder.matches(loginData.getPassword(), someUser.get().getPassword())) {
    		System.out.println("password is wrong");
			isValid = false;
    	}
    	
    	return isValid;
	}
	
	// 登録画面用のチェック
	public boolean isValid(RegisterData registerData, BindingResult result, Locale locale) {
		boolean isValid = true; 
		
		if(!registerData.getPassword().equals(registerData.getPasswordAgain())) {
			// パスワード不一致
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"passwordAgain",
					messageSource.getMessage("Unmatch.password", null, locale));
			result.addError(fieldError);
			registerData.setPassword(null);
			registerData.setPasswordAgain(null);
			isValid = false;
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
			isValid = false;
		}
		
		// 名前が全角スペースだけで構成されていたらエラー
		if (!Utils.isBlank(name)) {
			if (Utils.isAllDoubleSpace(name)) {
				FieldError fieldError = new FieldError(
						result.getObjectName(),
						"name",
						messageSource.getMessage("DoubleSpace.name", null, locale));
				result.addError(fieldError);
				isValid = false;
			}
		}
		
		if(name.toLowerCase().contains("continuing")) {
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"name",
					messageSource.getMessage("Cannnot.included_continuing.name", null, locale));
			result.addError(fieldError);
			isValid = false;
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
			isValid = false;
		}
		
		String email = registerData.getEmail();
		String address = email.substring(email.lastIndexOf("@") + 1); 
		if(address.equals("icloud.com") || address.equals("mac.com") || address.equals("me.com")) {
			// Apple系のメールアドレス
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"email",
					messageSource.getMessage("AppleBased.email", null, locale));
			result.addError(fieldError);
			registerData.setEmail(null);
			isValid = false;
		}
		
		return isValid;
	}

	public String sendMail(String email, String type, Locale locale) {
		String token = null;
		String subject = null;;
		String messageText = "<html><head></head><body>";
		
		if(type == null) {
			type = "error";
		}
		
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
			case "registration":
				token = UUID.randomUUID().toString();
				subject = messageSource.getMessage("mail.subject.registration", null, locale);
				messageText += messageSource.getMessage("mail.msg.register_thanks", null, locale)
						+ "<br>"
						+ messageSource.getMessage("mail.msg.full_registration", null, locale)
						+ "<br>"
						+ "<br>"
						+ "<a href='" + APP_URL + "/register"
						+ "/email/" + email 
						+ "/token/" + token
						+ "'>"
						+ APP_URL + "/register"
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
	
}
