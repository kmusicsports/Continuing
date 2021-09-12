package com.example.continuing.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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
	public boolean isValid(ProfileData profileData, Users oldData) {
		Boolean answer = true;
		
		if(!profileData.getNewPassword().equals(profileData.getNewPasswordAgain())) {
			// パスワード不一致
			System.out.println("Error: パスワードが一致しません");
			profileData.setNewPassword(null);
			profileData.setNewPasswordAgain(null);
			answer = false;
		}
		
		String newName = profileData.getName(); 
		if (!newName.equals(oldData.getName())) {
			// 名前が変更されている
			Optional<Users> nameUser = usersRepository.findByName(newName);
			if(nameUser.isPresent()) {
				// 既に同じ名前が登録されている ->　別の名前で登録してください
				System.out.println("Error: 既に登録されている名前です");
				profileData.setName(null);
				answer = false;
			}
			// 名前が全角スペースだけで構成されていたらエラー
			if (!Utils.isBlank(newName)) {
				if (Utils.isAllDoubleSpace(newName)) {
					answer = false;
				}
			}
		}
		
		if (!profileData.getEmail().equals(oldData.getEmail())) {
			// emailアドレスが変更されている
			Optional<Users> emailUser = usersRepository.findByEmail(profileData.getEmail());		
			if(emailUser.isPresent()) {
				// 既にemailアドレスが登録されている ->　別のemailアドレスで登録してください
				System.out.println("Error: 既に登録されているメールアドレスです");
				profileData.setEmail(null);
				answer = false;
			}			
		}
		
		return answer;
	}
	
	public List<Users> getSearchReuslt(SearchData searchData) {
		List<Users> userListName = usersRepository.findByNameContainingIgnoreCase(searchData.getKeyword());
		List<Users> userList = usersRepository.findByProfileMessageContainingIgnoreCase(searchData.getKeyword());
		userList.addAll(userListName);
		userList = new ArrayList<Users>(new LinkedHashSet<>(userList));
		
		return userList;
	}
 	
}
