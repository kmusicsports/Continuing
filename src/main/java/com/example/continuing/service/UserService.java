package com.example.continuing.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.example.continuing.common.Utils;
import com.example.continuing.entity.Users;
import com.example.continuing.form.ProfileData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.UsersRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {

	private final UsersRepository usersRepository;

	// プロフィール編集画面用のチェック
	public boolean isValid(ProfileData profileData, Users oldData, BindingResult result) {
		
		if(!profileData.getNewPassword().equals("")) {
			if(profileData.getNewPassword().length() < 8 || profileData.getNewPassword().length() > 16) {
				// パスワードの長さ
				FieldError fieldError = new FieldError(
						result.getObjectName(),
						"newPasswordAgain",
						"パスワードの文字数は8～16にしてください");
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
					"パスワードが一致しません");
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
						"既に使用されている名前です");
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
							"名前が全角スペースです");
					result.addError(fieldError);
					return false;
				}
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
						"既に登録されているメールアドレスです");
				result.addError(fieldError);
				profileData.setEmail(null);
				return false;
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
	
}
