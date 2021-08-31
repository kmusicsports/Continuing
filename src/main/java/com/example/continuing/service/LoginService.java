package com.example.continuing.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.continuing.entity.Users;
import com.example.continuing.form.RegisterData;
import com.example.continuing.repository.UsersRepository;

import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
public class LoginService {
	
	private final UsersRepository usersRepository;

	// 登録画面用のチェック
	public boolean isValid(RegisterData registerData) {
		Boolean flag = true;
		
		if(!registerData.isChecked()) {
			System.out.println("Error: 利用規約に同意して、チェックを入れてください");
			flag = false;
		}
		
		if(!registerData.getPassword1().equals(registerData.getPassword2())) {
			// パスワード不一致
			System.out.println("Error: パスワードが一致しません");
			registerData.setPassword1(null);
			registerData.setPassword2(null);
			flag = false;
		}
		
		Optional<Users> someUser;
		someUser = usersRepository.findByName(registerData.getName());
		if(someUser.isPresent()) {
			// 既に同じ名前が登録されている ->　別の名前で登録してください
			System.out.println("Error: 既に登録されている名前です");
			registerData.setName(null);
			flag = false;
		}
		someUser = usersRepository.findByEmail(registerData.getEmail());		
		if(someUser.isPresent()) {
			// 既にemailアドレスが登録されている ->　別のemailアドレスで登録してください
			System.out.println("Error: 既に登録されているメールアドレスです");
			registerData.setEmail(null);
			flag = false;
		}
		
		return flag;
	}

}
