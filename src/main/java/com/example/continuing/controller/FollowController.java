package com.example.continuing.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.continuing.dto.MessageDto;
import com.example.continuing.entity.Follows;
import com.example.continuing.entity.Users;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.FollowsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.MailService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FollowController {
	
	private final HttpSession session;
	private final FollowsRepository followsRepository;
	private final FollowService followService;
	private final UsersRepository usersRepository;
	private final MailService mailService;
	
	@Value("${app.url}")
	private String APP_URL;
	
	@GetMapping("/User/follow/{followee_id}")
	public String follow(@PathVariable(name = "followee_id") int followeeId, 
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		Optional<Users> someUser = usersRepository.findById(followeeId);
		if(someUser.isPresent()) {
			Integer followerId = (Integer)session.getAttribute("user_id");
			Follows follow = new Follows(followerId, followeeId);
			followsRepository.saveAndFlush(follow);
			
			Users followee = someUser.get();
			Users follower = usersRepository.findById(followerId).get();
		
			String messageText = "<html><head></head><html><head></head><body>"
					+ "<a href='" + APP_URL + "/User/" + follower.getId() + "'>" + follower.getName() + "さんのページに行く</a>"
					+ "</body></html>";
			
			mailService.sendMail(
					followee.getEmail(), 
					"Continuing - " + follower.getName() + "さんがあなたをフォローしました", 
					messageText);
			
		    return "redirect:" + session.getAttribute("path");
		} else {
			String msg = "存在しないユーザーです。";
			redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
			return "redirect:/home";
		}
	}
	
	@GetMapping("/User/unfollow/{followee_id}")
	public String unfollow(@PathVariable(name = "followee_id") int followeeId, 
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		Optional<Users> someUser = usersRepository.findById(followeeId);
		if(someUser.isPresent()) {
			Integer followerId = (Integer)session.getAttribute("user_id");
			List<Follows> follows = followsRepository.findByFollowerIdAndFolloweeId(followerId, followeeId);
			followsRepository.deleteAll(follows);
			return "redirect:" + session.getAttribute("path");
		} else {
			String msg = "存在しないユーザーです。";
			redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
			return "redirect:/home";
		}
	}
	
	@GetMapping("/User/{user_id}/list/follows")
	public ModelAndView showUserFollows(@PathVariable(name = "user_id") int userId, 
			ModelAndView mv, RedirectAttributes redirectAttributes) {
		Optional<Users> someUser = usersRepository.findById(userId);
		if(someUser.isPresent()) {
			List<Users> followsList = followService.getFollowsList(userId);
			List<Users> followersList = followService.getFollowersList(userId);
			
			Integer myId = (Integer)session.getAttribute("user_id");
			List<Users> myFollowsList = followService.getFollowsList(myId);
			
			session.setAttribute("path", "/User/" + userId + "/list/follows");
			mv.setViewName("follows");
			mv.addObject("followsList", followsList);
			mv.addObject("followersList", followersList);
			mv.addObject("myFollowsList", myFollowsList);
			mv.addObject("searchData", new SearchData());
		} else {
			String msg = "存在しないユーザーです。";
			mv.setViewName("redirect:/home");
			redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
		}
		return mv;
		
	}
}
