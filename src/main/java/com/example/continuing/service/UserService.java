package com.example.continuing.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.continuing.common.Utils;
import com.example.continuing.entity.Users;
import com.example.continuing.form.ContactData;
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
	
	// プロフィール編集画面用のチェック
	public boolean isValid(ProfileData profileData, Users oldData, 
			BindingResult result, Locale locale) {
		
		if(!profileData.getNewPassword().equals("")) {
			if(profileData.getNewPassword().length() < 8 || profileData.getNewPassword().length() > 32) {
				// パスワードの長さ
				FieldError fieldError = new FieldError(
						result.getObjectName(),
						"newPassword",
						messageSource.getMessage("Password.Length.newPassword", null, locale));
				result.addError(fieldError);
				profileData.setNewPassword(null);
				profileData.setNewPasswordAgain(null);
				return false;
			}			
		}
		
		if(!profileData.getNewPassword().equals(profileData.getNewPasswordAgain())) {
			// パスワード不一致
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"newPasswordAgain",
					messageSource.getMessage("Unmatch.password", null, locale));
			result.addError(fieldError);
			profileData.setNewPassword(null);
			profileData.setNewPasswordAgain(null);
			return false;
		}
		
		String newName = profileData.getName(); 
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
				return false;
			}
			
			// 名前が全角スペースだけで構成されていたらエラー
			if (!Utils.isBlank(newName)) {
				if (Utils.isAllDoubleSpace(newName)) {
					FieldError fieldError = new FieldError(
							result.getObjectName(),
							"name",
							messageSource.getMessage("DoubleSpace.name", null, locale));
					result.addError(fieldError);
					return false;
				}
			}
			
			if(newName.toLowerCase().contains("continuing")) {
				FieldError fieldError = new FieldError(
						result.getObjectName(),
						"name",
						messageSource.getMessage("Cannnot.included_continuing.name", null, locale));
				result.addError(fieldError);
				return false;
			}
		}
		
		if (!profileData.getEmail().equals(oldData.getEmail())) {
			// emailアドレスが変更されている
			Optional<Users> emailUser = usersRepository.findByEmail(profileData.getEmail());		
			if(emailUser.isPresent()) {
				// 既にemailアドレスが登録されている ->　別のemailアドレスで登録してください
				FieldError fieldError = new FieldError(
						result.getObjectName(),
						"email",
						messageSource.getMessage("AlreadyUsed.email", null, locale));
				result.addError(fieldError);
				profileData.setEmail(null);
				return false;
			} else {
				String subject = messageSource.getMessage("mail.subject.email_updated", null, locale);
				String messageText = "<html><head></head><body>"
						+ messageSource.getMessage("mail.msg.email_updated", null, locale)
						+ "</body></html>";
				mailService.sendMail(profileData.getEmail(), subject, messageText);
			}
		}
		
		return true;
	}
	
	public List<Users> getSearchReuslt(SearchData searchData) {
		List<Users> userListName = usersRepository.findByNameContainingIgnoreCase(searchData.getKeyword());
		List<Users> userList = usersRepository.findByProfileMessageContainingIgnoreCase(searchData.getKeyword());
		userList.addAll(userListName);
		userList = new ArrayList<Users>(new LinkedHashSet<>(userList));
		
		return userList;
	}
 	
	public Map<Integer, Integer> makeRankingMap(List<Users> userList) {
		Map<Integer, Integer> rankingMap = new TreeMap<>();
		int i = 1;
		for(Users user : userList) {
			if (!rankingMap.containsKey(user.getContinuousDays())) {
				rankingMap.put(user.getContinuousDays(), i++);				
			}
		}
		
		return rankingMap;
	}
	
	// お問い合わせフォーム用のチェック
	public boolean isValid(ContactData contactData, Users user, BindingResult result, Locale locale) {
		boolean answer = true;
		
		if(!contactData.getName().equals(user.getName())) {
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"name",
					messageSource.getMessage("Unmatch.name", null, locale)
					);
			result.addError(fieldError);
			contactData.setName(null);
			answer = false;
		}
		
		if(!contactData.getEmail().equals(user.getEmail())) {
			FieldError fieldError = new FieldError(
					result.getObjectName(),
					"email",
					messageSource.getMessage("Unmatch.email", null, locale)
					);
			result.addError(fieldError);
			contactData.setEmail(null);
			answer = false;
		}
		
		return answer;
	}
	
	public void sendContactEmail(ContactData contactData) {
		String messageText = "<html><head></head><body>"
				+ "Username: " + contactData.getName() + "<br>"
				+ "Email address: " + contactData.getEmail() + "<br>"
				+ "Contents: " + contactData.getContents() + "<br>"
				+ "<br>"
				+ "Version: " + APP_VERSION
				+ "</body></html>";
		mailService.sendMail(FROM_ADDRESS, "Contact", messageText);
	}
	
}
