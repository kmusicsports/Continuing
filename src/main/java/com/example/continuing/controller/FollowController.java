package com.example.continuing.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Follows;
import com.example.continuing.entity.Users;
import com.example.continuing.repository.FollowsRepository;
import com.example.continuing.service.FollowService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class FollowController {
	
	private final HttpSession session;
	private final FollowsRepository followsRepository;
	private final FollowService followService;

	@GetMapping("/User/follow/{followee_id}")
	public String follow(@PathVariable(name = "followee_id") int followeeId, HttpServletRequest request) {
		Integer followerId = (Integer)session.getAttribute("user_id");
		if(followerId == null) {
			System.out.println("Error: ログインし直してください");
			return "redirect:/User/showLogin";
		} else {
			Follows follow = new Follows(followerId, followeeId);
			followsRepository.saveAndFlush(follow);
			
			String header = request.getHeader("REFERER");
			String redirectUrl = header.substring(header.indexOf("/", 10));
		    return "redirect:" + redirectUrl;
		}
	}
	
	@GetMapping("/User/unfollow/{followee_id}")
	public String unfollow(@PathVariable(name = "followee_id") int followeeId, HttpServletRequest request) {
		Integer followerId = (Integer)session.getAttribute("user_id");
		if(followerId == null) {
			System.out.println("Error: ログインし直してください");
			return "redirect:/User/showLogin";
		} else {
			List<Follows> follows = followsRepository.findByFollowerIdAndFolloweeId(followerId, followeeId);
			followsRepository.deleteAll(follows);

			String header = request.getHeader("REFERER");
			String redirectUrl = header.substring(header.indexOf("/", 10));
		    return "redirect:" + redirectUrl;
		}
	}
	
	@GetMapping("/User/{user_id}/list/follows")
	public ModelAndView showUserFollows(@PathVariable(name = "user_id") int userId, ModelAndView mv) {
		List<Users> followsList = followService.getFollowsList(userId);
		List<Users> followersList = followService.getFollowersList(userId);
		mv.setViewName("follows");
		mv.addObject("followsList", followsList);
		mv.addObject("followersList", followersList);
		return mv;
	}
}
