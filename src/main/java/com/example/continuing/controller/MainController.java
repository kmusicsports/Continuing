package com.example.continuing.controller;


import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Users;
import com.example.continuing.repository.UsersRepository;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class MainController {

	private final UsersRepository usersRepository;
	
	@GetMapping("/Meeting/list/all") // "/home"
	public ModelAndView showHome(ModelAndView mv) {
		List<Users> userList = usersRepository.findAll();
		mv.setViewName("home");
		mv.addObject("userList", userList);
		return mv;
	}
}
