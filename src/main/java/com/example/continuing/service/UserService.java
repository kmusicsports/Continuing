package com.example.continuing.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.continuing.common.Utils;
import com.example.continuing.entity.Users;
import com.example.continuing.form.ContactData;
import com.example.continuing.form.EmailData;
import com.example.continuing.form.ProfileData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UsersRepository usersRepository;
	private final MailService mailService;
	private final MessageSource messageSource;

	@Value("${spring.mail.username}")
	private String FROM_ADDRESS;
	
	@Value("${app.version}")
	private String APP_VERSION;
	
	@Value("${app.url}")
	private String APP_URL;
	
	// プロフィール編集用のチェック
	public boolean isValid(ProfileData profileData, Users oldData, 
			BindingResult result, Locale locale) {
		
		boolean isValid = true;
		
		String newPassword = profileData.getNewPassword();
		String newPasswordAgain = profileData.getNewPasswordAgain();
		if(!newPassword.equals("")) {
			if(newPassword.length() < 8 || newPassword.length() > 32) {
				// パスワードの長さ
				FieldError fieldError = new FieldError(
						result.getObjectName(),
						"newPassword",
						messageSource.getMessage("Password.Length.newPassword", null, locale));
				result.addError(fieldError);
				profileData.setNewPassword(null);
				profileData.setNewPasswordAgain(null);
				isValid =  false;
			}			
		}
		
		if(!newPassword.equals(newPasswordAgain)) {
			// パスワード不一致
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"newPasswordAgain",
					messageSource.getMessage("Unmatch.password", null, locale));
			result.addError(fieldError);
			profileData.setNewPassword(null);
			profileData.setNewPasswordAgain(null);
			isValid =  false;
		}
		
		String newName = profileData.getName(); 
		if (!Utils.isBlank(newName)) {
			if (!newName.equals(oldData.getName())) {
				// 名前が変更されている
				Optional<Users> nameUser = usersRepository.findByName(newName);
				if(nameUser.isPresent()) {
					// 既に同じ名前が登録されている ->　別の名前で登録してください
					FieldError fieldError = new FieldError(
							result.getObjectName(),
							"name",
							messageSource.getMessage("AlreadyUsed.name", null, locale));
					result.addError(fieldError);
					profileData.setName(null);
					isValid =  false;
				}
				
				if (Utils.isAllDoubleSpace(newName)) {
					// 名前が全角スペースだけで構成されていたらエラー
					FieldError fieldError = new FieldError(
							result.getObjectName(),
							"name",
							messageSource.getMessage("DoubleSpace.name", null, locale));
					result.addError(fieldError);
					profileData.setName(null);
					isValid =  false;
				}
				
				if(newName.toLowerCase().contains("continuing")) {
					FieldError fieldError = new FieldError(
							result.getObjectName(),
							"name",
							messageSource.getMessage("Cannnot.included_continuing.name", null, locale));
					result.addError(fieldError);
					profileData.setName(null);
					isValid =  false;
				}
			}
		}
		
		return isValid;
	}
	
	// メールアドレス変更用のチェック
	public boolean isValid (EmailData emailData, BindingResult result, Locale locale) {	
		Optional<Users> emailUser = usersRepository.findByEmail(emailData.getEmail());		
		if(emailUser.isPresent()) {
			// 既にemailアドレスが登録されている ->　別のemailアドレスで登録してください
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"email",
					messageSource.getMessage("AlreadyUsed.email", null, locale));
			result.addError(fieldError);
			return false;
		}
		
		String email = emailData.getEmail();
		String address = email.substring(email.lastIndexOf("@") + 1); 
		if(address.equals("icloud.com") || address.equals("mac.com") || address.equals("me.com")) {
			// Apple系のメールアドレス
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"email",
					messageSource.getMessage("AppleBased.email", null, locale));
			result.addError(fieldError);
			return false;
		}
		
		return true;
	}
	
	public List<Users> getSearchReuslt(SearchData searchData) {
		String searchKeyword = searchData.getKeyword();
		
		List<Users> userListName = usersRepository.findByNameContainingIgnoreCase(searchKeyword);
		List<Users> userList = usersRepository.findByProfileMessageContainingIgnoreCase(searchKeyword);
		userList.addAll(userListName);
		userList = new ArrayList<Users>(new LinkedHashSet<>(userList));
		
		return userList;
	}
 	
	public Map<Integer, Integer> makeRankingMap(List<Users> userList) {
		
		Collections.sort(
            userList, 
            new Comparator<Users>() {
                @Override
                public int compare(Users user1, Users user2) {
                    return user2.getContinuousDays() - user1.getContinuousDays();
                }
            }
        );
		
		Map<Integer, Integer> rankingMap = new TreeMap<>();
		int i = 1;
		for(Users user : userList) {
			if (!rankingMap.containsKey(user.getContinuousDays())) {
				rankingMap.put(user.getContinuousDays(), i++);				
			}
		}
		
		return rankingMap;
	}
	
	public void sendContactEmail(ContactData contactData) {
		String messageText = "<html><head></head><body>"
				+ "Email address: " + contactData.getEmail() + "<br>"
				+ "Contents: " + contactData.getContents() + "<br>"
				+ "<br>"
				+ "Version: " + APP_VERSION
				+ "</body></html>";
		mailService.sendMail(FROM_ADDRESS, "Contact", messageText);
	}
	
	public String sendAuthenticationEmail(String email, Locale locale) {		 
		String token = UUID.randomUUID().toString();
		String subject = messageSource.getMessage("mail.subject.authentication_email", null, locale);
		String messageText = messageSource.getMessage("mail.msg.authentication_email", null, locale)
				+ "<br>"
				+ "<a href='" + APP_URL + "/updateEmail"
				+ "/email/" + email 
				+ "/token/" + token
				+ "'>"
				+ APP_URL + "/updateEmail"
				+ "/email/" + email 
				+ "/token/" + token
				+ "</a>";
		mailService.sendMail(email, subject, messageText);
		return token;
	}
	
}
