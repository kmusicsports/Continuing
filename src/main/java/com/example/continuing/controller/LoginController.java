package com.example.continuing.controller;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
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
	private final MessageSource messageSource;

	@GetMapping("/showLogin")
	public ModelAndView showLogin(ModelAndView mv) {
		mv.setViewName("login");
		mv.addObject("loginData", new LoginData());
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
	@PostMapping("/login")
	public String login(@ModelAttribute @Validated LoginData loginData,
			BindingResult result, ModelAndView mv, 
			RedirectAttributes redirectAttributes, Locale locale) {
		
		boolean isValid = loginService.isValid(loginData, result); 
		if(!result.hasErrors() && isValid) { 
			Users user = usersRepository.findByEmail(loginData.getEmail()).get();
			session.setAttribute("user_id", user.getId());
			session.setAttribute("user_name", user.getName());
			String[] args = {user.getName()};
			locale = new Locale(user.getLanguage());
			String msg = messageSource.getMessage("msg.s.login", args, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
			if(session.getAttribute("path") == null) {
				return "redirect:/home";    					
			} else {
				return "redirect:" + session.getAttribute("path");
			}
		} else {
			// エラーあり -> エラーメッセージをセット
			String msg = messageSource.getMessage("msg.e.login", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
			return "redirect:/showLogin"; 
		}
	}
	
	@GetMapping("/logout")
	public String logout(RedirectAttributes redirectAttributes) {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();
		Locale locale = new Locale(user.getLanguage());
		
		// セッション情報をクリアする
		session.invalidate();
		String msg = messageSource.getMessage("msg.i.logout", null, locale);
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
			BindingResult result, ModelAndView mv, 
			RedirectAttributes redirectAttributes, Locale locale) {
		
		// エラーチェック
		if(!result.hasErrors()) {
			boolean isValid = loginService.isValid(registerData, result, locale);
			if(isValid) {
				// ユーザー新規登録
				Users newUser = registerData.toEntity(passwordEncoder, locale);
				usersRepository.saveAndFlush(newUser);
				loginService.sendMail(newUser.getEmail(), "welcome", locale);
				
				mv.setViewName("redirect:/showLogin");
				String msg = messageSource.getMessage("msg.s.regist", null, locale);
				redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
			} else {
				String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
				registerData.setChecked(false);
				mv.setViewName("register");
				mv.addObject("searchData", new SearchData());
				mv.addObject("msg",new MessageDto("E", msg));
			}
		} else {
			registerData.setChecked(false);
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			mv.setViewName("register");
			mv.addObject("searchData", new SearchData());
			mv.addObject("msg",new MessageDto("E", msg));
		}
		
		return mv;
	}
}
