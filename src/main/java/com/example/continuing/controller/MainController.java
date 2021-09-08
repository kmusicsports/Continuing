package com.example.continuing.controller;


import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Topics;
import com.example.continuing.entity.Users;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.TopicsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class MainController {

	private final UsersRepository usersRepository;
	private final HttpSession session;
	private final FollowService followService;
	private final TopicsRepository topicsRepository;
	private final MeetingsRepository meetingsRepository;
	
	@GetMapping("/")
	public String showHome() {
		return "redirect:/home";
	}
	
	@GetMapping("/home")
	public ModelAndView showHome(ModelAndView mv, 
			@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
		Page<Meetings> meetingPage = meetingsRepository.findAll(pageable);
		List<Users> userList = usersRepository.findAll();
		
		Integer userId = (Integer)session.getAttribute("user_id");
		List<Users> myFollowsList = followService.getFollowsList(userId);
		
		List<Topics> topicList = topicsRepository.findAll();
		
		mv.setViewName("home");
		mv.addObject("meetingPage", meetingPage);
		mv.addObject("meetingList", meetingPage.getContent());
		mv.addObject("userList", userList);
		mv.addObject("myFollowsList", myFollowsList);
		mv.addObject("topicList", topicList);
		return mv;
	}
}
