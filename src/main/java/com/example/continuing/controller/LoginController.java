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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.continuing.dto.MessageDto;
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
			BindingResult result, ModelAndView mv, RedirectAttributes redirectAttributes) {
		
		boolean isValid = loginService.isValid(loginData, result); 
		if(!result.hasErrors() && isValid) { 
			Users user = usersRepository.findByEmail(loginData.getEmail()).get();
			session.setAttribute("user_id", user.getId());
			session.setAttribute("user_name", user.getName());
			String msg = "こんにちは、" + user.getName() + "さん!";
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
			if(session.getAttribute("path") == null) {
				return "redirect:/home";    					
			} else {
				return "redirect:" + session.getAttribute("path");
			}
		} else {
			// エラーあり -> エラーメッセージをセット
			String msg = "メールアドレスまたはパスワードが間違っています。";
			redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
			return "redirect:/showLogin"; 
		}
	}
	
	@GetMapping("/logout")
	public String logout(RedirectAttributes redirectAttributes) {
		// セッション情報をクリアする
		session.invalidate();
		String msg = "ログアウトしました。";
		redirectAttributes.addFlashAttribute("msg", new MessageDto("I", msg));
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
			BindingResult result, ModelAndView mv, RedirectAttributes redirectAttributes) {
		// エラーチェック
		if(!result.hasErrors()) {
			boolean isValid = loginService.isValid(registerData, result);
			if(isValid) {
				// ユーザー新規登録
				Users newUser = registerData.toEntity(passwordEncoder);
				usersRepository.saveAndFlush(newUser);
				loginService.sendMail(newUser.getEmail(), "welcome");
				
				String msg = "ユーザーアカウントが正常に登録されました。メールが届いていない場合は、メールアドレスが正しいか確認し、変更して下さい。";
				mv.setViewName("redirect:/showLogin");
				redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
			} else {
				String msg = "入力に誤りがあります。";
				registerData.setChecked(false);
				mv.setViewName("register");
				mv.addObject("searchData", new SearchData());
				mv.addObject("msg", new MessageDto("E", msg));
			}
		} else {
			String msg = "入力に誤りがあります。";
			registerData.setChecked(false);
			mv.setViewName("register");
			mv.addObject("searchData", new SearchData());
			mv.addObject("msg", new MessageDto("E", msg));
		}
		
		return mv;
	}
}
