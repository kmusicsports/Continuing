package com.example.continuing.controller;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.continuing.dto.MessageDto;
import com.example.continuing.entity.Deliveries;
import com.example.continuing.entity.Temporaries;
import com.example.continuing.entity.Users;
import com.example.continuing.form.EmailData;
import com.example.continuing.form.LoginData;
import com.example.continuing.form.ProfileData;
import com.example.continuing.form.RegisterData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.DeliveriesRepository;
import com.example.continuing.repository.TemporariesRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.LoginService;
import com.example.continuing.service.TemporaryService;
import com.example.continuing.service.UserService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class LoginController {
	
	private final UsersRepository usersRepository;
	private final HttpSession session;
	private final LoginService loginService;
	private final PasswordEncoder passwordEncoder;
	private final MessageSource messageSource;
	private final CsrfTokenRepository csrfTokenRepository;
	private final DeliveriesRepository deliveriesRepository;
	private final TemporariesRepository temporariesRepository;
	private final UserService userService;
	private final TemporaryService temporaryService;

	@GetMapping("/showLogin")
	public ModelAndView showLogin(ModelAndView mv) {
		mv.setViewName("login");
		mv.addObject("loginData", new LoginData());
		mv.addObject("searchData", new SearchData());
		mv.addObject("emailData", new EmailData());
		return mv;
	}
	
	@PostMapping("/login")
	public String login(@ModelAttribute @Validated LoginData loginData,
			BindingResult result, HttpServletRequest request,
			RedirectAttributes redirectAttributes, Locale locale) {
		
		boolean isValid = loginService.isValid(loginData, result); 
		if(!result.hasErrors() && isValid) { 
			Users user = usersRepository.findByEmail(loginData.getEmail()).get();
			CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
			session.setAttribute("csrf_token", csrfToken.getToken());
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
	
	@GetMapping("/User/logout")
	public String logout(RedirectAttributes redirectAttributes) {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();
		Locale locale = new Locale(user.getLanguage());

		session.invalidate(); // セッション情報をクリアする
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
	public ModelAndView temporarilyRegister(@ModelAttribute @Validated RegisterData registerData,
			BindingResult result, ModelAndView mv, 
			RedirectAttributes redirectAttributes, Locale locale) {
		
		// エラーチェック
		boolean isValid = loginService.isValid(registerData, result, locale);
		if(!result.hasErrors() && isValid) {
			// ユーザー仮登録
			String token = loginService.sendMail(registerData.getEmail(), "registration", locale);
			Temporaries temporary = registerData.toEntity(passwordEncoder, token);
			temporariesRepository.saveAndFlush(temporary);
			
			mv.setViewName("redirect:/showLogin");
			String msg = messageSource.getMessage("msg.s.temporary_register", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
		} else {
			registerData.setChecked(false);
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			mv.setViewName("register");
			mv.addObject("searchData", new SearchData());
			mv.addObject("msg",new MessageDto("E", msg));
		}
		
		return mv;
	}
	
	@GetMapping("/register/email/{email}/token/{token}")
	public String fullRegister(@PathVariable(name = "email") String email,
			@PathVariable(name = "token") String token, Locale locale,
			RedirectAttributes redirectAttributes) {
		
		boolean isValid = temporaryService.isValid(email, token);
		if(isValid) {
			// ユーザー本登録
			List<Temporaries> temporariesList = temporariesRepository.findByEmailOrderByCreatedAtDesc(email);
			Temporaries latestTemporaries = temporariesList.get(0);
			Users user = new Users(latestTemporaries, locale);
			usersRepository.saveAndFlush(user);
			temporariesRepository.deleteAll(temporariesList);
			
			Deliveries deliveries = new Deliveries(user.getId());
			deliveriesRepository.saveAndFlush(deliveries);
			loginService.sendMail(user.getEmail(), "welcome", locale);
			
			String msg = messageSource.getMessage("msg.s.register", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
			return "redirect:/showLogin"; 
		} else {
			String msg = messageSource.getMessage("msg.e.register", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
			return "redirect:/showRegister";
		}
	}
	
	
	@GetMapping("/terms")
	public String showTerms(Locale locale) {
		String language = locale.getLanguage();

		switch(language) {
			case "ja":
				return "terms/terms_ja";
			case "en":
				return "terms/terms_en";
			default:
				return "terms/terms";
		}
	}

	@PostMapping("/login/reset-password")
	public String sendResetPasswordEmail(@ModelAttribute @Validated EmailData emailData,
			BindingResult result, Locale locale, RedirectAttributes redirectAttributes) {
	
		if(!result.hasErrors()) {
			String email = emailData.getEmail();
			Optional<Users> someUser = usersRepository.findByEmail(email);
			
			if(someUser.isPresent()) {
				Users user = someUser.get();
				String token = loginService.sendMail(email, "reset-password", locale);
				Temporaries temporaries = new Temporaries(user, token);
				temporariesRepository.saveAndFlush(temporaries);

				String msg = messageSource.getMessage("msg.s.send_reset_password_email", null, locale);
				redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
			} else {
				String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
				redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
			}
		} else {
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
		}
		
		return "redirect:/showLogin";
	}
	
	@GetMapping("/reset-password/email/{email}/token/{token}")
	public ModelAndView resetPasswordForm(@PathVariable(name = "email") String email,
			@PathVariable(name = "token") String token, ModelAndView mv,
			Locale locale, RedirectAttributes redirectAttributes) {
		
		boolean isValid = temporaryService.isValid(email, token);
		if(isValid) {
			Users user = usersRepository.findByEmail(email).get();
			
			session.setAttribute("mode", "reset-password");
			mv.setViewName("profile");
			mv.addObject("profileData", new ProfileData(user));
		} else {
			mv.setViewName("redirect:/showLogin");
			String msg = messageSource.getMessage("msg.e.start_over", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
		}
		
		return mv;
	}
	
	@PostMapping("/reset-password/reset")
	public ModelAndView resetPassword(@ModelAttribute ProfileData profileData,
			BindingResult result, ModelAndView mv, RedirectAttributes redirectAttributes) {
		
		Users oldData = usersRepository.findByName(profileData.getName()).get();
		Locale locale = new Locale(oldData.getLanguage());
		
		boolean isValid = userService.isValid(profileData, oldData, result, locale);
		if(isValid) {
			Users user = profileData.toEntity(oldData, passwordEncoder);
			usersRepository.saveAndFlush(user);
			List<Temporaries> temporaryList = temporariesRepository.findByEmailOrderByCreatedAtDesc(user.getEmail());
			temporariesRepository.deleteAll(temporaryList);

			mv.setViewName("redirect:/showLogin");
			String msg = messageSource.getMessage("msg.s.password_reset", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
		} else {
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			
			session.setAttribute("mode", "reset-password");
			mv.setViewName("profile");
			mv.addObject("msg", new MessageDto("E", msg));
		}
		
		return mv;
	}
	
}
