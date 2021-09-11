package com.example.continuing.controller;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Users;
import com.example.continuing.form.LoginData;
import com.example.continuing.form.RegisterData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.LoginService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class LoginController {
	
	private final UsersRepository usersRepository;
	private final HttpSession session;
	private final LoginService loginService;

	@GetMapping("/showLogin")
	public ModelAndView showLogin(ModelAndView mv) {
		mv.setViewName("login");
		mv.addObject("loginData", new LoginData());
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
	@PostMapping("/login")
	public ModelAndView login(LoginData loginData, ModelAndView mv) {
		Optional<Users> someUser = usersRepository.findByEmail(loginData.getEmail());
    	someUser
    		.ifPresentOrElse(user -> {
    			// userは存在する
    			
    			// パスワードが正しいか？
    			if (loginData.getPassword().equals(user.getPassword())) {
    				session.setAttribute("user_id", user.getId());
    				session.setAttribute("user_name", user.getName());
    				mv.setViewName("redirect:/home");
    			} else {
    				System.out.println("パスワードが違います。");
        			mv.setViewName("redirect:/showLogin");
    			}
    		}, () -> {
    			// userが存在しない
    			System.out.println("ユーザーアカウントが見つかりませんでした。");
    			mv.setViewName("redirect:/showLogin");
    		});
    	return mv;
	}
	
	@GetMapping("/logout")
	public String logout() {
		// セッション情報をクリアする
		session.invalidate();
		return "redirect:/showLogin";
	}
	
	@GetMapping("/showRegister")
	public ModelAndView showRegister(ModelAndView mv) {
		mv.setViewName("register");
		mv.addObject("registerData", new RegisterData());
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
	@PostMapping("/regist")
	public ModelAndView regist(RegisterData registerData, ModelAndView mv) {
		// エラーチェック
		boolean isValid = loginService.isValid(registerData);
		if(isValid) {
			// ユーザー新規登録
			Users newUser = registerData.toEntity();
			usersRepository.saveAndFlush(newUser);
			System.out.println("ユーザーアカウントが正常に登録されました。");
			mv.setViewName("redirect:/showLogin");
		} else {
			registerData.setChecked(false);
			mv.setViewName("register");
			mv.addObject("registerData", registerData);
			mv.addObject("searchData", new SearchData());
		}
		return mv;
	}
}
