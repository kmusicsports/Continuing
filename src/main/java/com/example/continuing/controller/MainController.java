package com.example.continuing.controller;


import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Users;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class MainController {

	private final UsersRepository usersRepository;
	private final HttpSession session;
	private final FollowService followService;
	
	@GetMapping("/Meeting/list/all") // "/home"
	public ModelAndView showHome(ModelAndView mv) {
		List<Users> userList = usersRepository.findAll();
		Integer userId = (Integer)session.getAttribute("user_id");
		List<Integer> followeeIdList = followService.getFolloweeIdList(userId);
		mv.setViewName("home");
		mv.addObject("userList", userList);
		mv.addObject("followeeIdList", followeeIdList);
		return mv;
	}
}
