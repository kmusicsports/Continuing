package com.example.continuing.controller;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Users;
import com.example.continuing.form.ProfileData;
import com.example.continuing.repository.UsersRepository;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class UserController {

	private final UsersRepository usersRepository;
	private final HttpSession session;
	
	@GetMapping("/User/{id}")
	public ModelAndView showUserDetail(ModelAndView mv, @PathVariable(name = "id") int id) {
		Optional<Users> user = usersRepository.findById(id);
		if(user.isPresent()) {
			mv.setViewName("userDetail");
			mv.addObject("user", user.get());
		} else {
			System.out.println("存在しないユーザーです");
			mv.setViewName("redirect:/Meeting/list/all");
		}
		return mv;
	}
	
	@GetMapping("/User/mypage")
	public ModelAndView showMyPage(ModelAndView mv) {
		Integer id = (Integer)session.getAttribute("user_id");
		if(id == null) {
			mv.setViewName("redirect:/User/showLogin");
			System.out.println("Error: ログインし直してください");
		} else {
			Users user = usersRepository.findById(id).get();
			mv.setViewName("userDetail");
			mv.addObject("user", user);			
		}
		return mv;
	}
	
	@GetMapping("/setting") // "/User/setting"
	public ModelAndView setting(ModelAndView mv) {
		mv.setViewName("setting");
		return mv;
	}
	
	@GetMapping("/User/delete")
	public String deleteUser() {
		Integer id = (Integer)session.getAttribute("user_id");
		usersRepository.deleteById(id);
		// セッション情報をクリアする
		session.invalidate();
		return "redirect:/Meeting/list/all";
	}
	
	@GetMapping("/User/updateForm")
	public ModelAndView updateProfileForm(ModelAndView mv) {
		Integer id = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(id).get();
		mv.setViewName("profile");
		mv.addObject("profileData", new ProfileData(user));
		return mv;
	}
	
}
