package com.example.continuing.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Users;
import com.example.continuing.repository.UsersRepository;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class UserController {

	private final UsersRepository usersRepository;
	
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
	
}