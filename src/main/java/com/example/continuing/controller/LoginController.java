package com.example.continuing.controller;

import javax.servlet.http.HttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	private final PasswordEncoder passwordEncoder;

	@GetMapping("/showLogin")
	public ModelAndView showLogin(ModelAndView mv) {
		mv.setViewName("login");
		mv.addObject("loginData", new LoginData());
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
	@PostMapping("/login")
	public String login(@ModelAttribute @Validated LoginData loginData,
			BindingResult result, ModelAndView mv) {
		
		// バリデーション
		if(result.hasErrors()) {
			// エラーあり -> エラーメッセージをセット
			String msg = "メールアドレスまたはパスワードが間違っています。";
			System.out.println(msg);
			return "redirect:/showLogin"; 
		}
		
		// サービスでチェック
		if(!loginService.isValid(loginData, result)) {
			// エラーあり -> エラーメッセージをセット
			String msg = "メールアドレスまたはパスワードが間違っています。";
			System.out.println(msg);
			return "redirect:/showLogin"; 
		}
		Users user = usersRepository.findByEmail(loginData.getEmail()).get();
		session.setAttribute("user_id", user.getId());
		session.setAttribute("user_name", user.getName());
		if(session.getAttribute("path") == null) {
			return "redirect:/home";    					
		} else {
			return "redirect:" + session.getAttribute("path");
		}
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
	public ModelAndView registCheck(@ModelAttribute @Validated RegisterData registerData,
			BindingResult result, ModelAndView mv) {
		// エラーチェック
		if(!result.hasErrors()) {
			boolean isValid = loginService.isValid(registerData, result);
			if(isValid) {
				// ユーザー新規登録
				Users newUser = registerData.toEntity(passwordEncoder);
				usersRepository.saveAndFlush(newUser);
				loginService.sendMail(newUser.getEmail(), "welcome");
				
				System.out.println("ユーザーアカウントが正常に登録されました。メールが届いていない場合は、メールアドレスが正しいか確認し、変更して下さい。");
				mv.setViewName("redirect:/showLogin");						
			} else {
				System.out.println("入力に誤りがあります。");
				registerData.setChecked(false);
				mv.setViewName("register");
				mv.addObject("searchData", new SearchData());
			}
		} else {
			System.out.println("入力に誤りがあります。");
			registerData.setChecked(false);
			mv.setViewName("register");
			mv.addObject("searchData", new SearchData());
		}
		return mv;
	}
}
