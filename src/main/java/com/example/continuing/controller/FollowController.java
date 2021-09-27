package com.example.continuing.controller;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
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

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class FollowController {
	
	private final HttpSession session;
	private final FollowsRepository followsRepository;
	private final FollowService followService;
	private final UsersRepository usersRepository;
	private final MessageSource messageSource;
	
	@GetMapping("/User/follow/{followee_id}")
	public String follow(@PathVariable(name = "followee_id") int followeeId, 
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		
		Integer followerId = (Integer)session.getAttribute("user_id");
		Users follower = usersRepository.findById(followerId).get(); 
		Optional<Users> someUser = usersRepository.findById(followeeId);
		if(someUser.isPresent()) {
			Follows follow = new Follows(followerId, followeeId);
			followsRepository.saveAndFlush(follow);
			
			Users followee = someUser.get();
		
			followService.sendMail(followee, follower, new Locale(followee.getLanguage()));
			
		    return "redirect:" + session.getAttribute("path");
		} else {
			String msg = messageSource.getMessage("msg.w.user_not_found", null, new Locale(follower.getLanguage()));
			redirectAttributes.addFlashAttribute("msg", new MessageDto("W", msg));
			return "redirect:/home";
		}
	}
	
	@GetMapping("/User/unfollow/{followee_id}")
	public String unfollow(@PathVariable(name = "followee_id") int followeeId, 
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		
		Integer followerId = (Integer)session.getAttribute("user_id");
		Optional<Users> someUser = usersRepository.findById(followeeId);
		if(someUser.isPresent()) {
			List<Follows> follows = followsRepository.findByFollowerIdAndFolloweeId(followerId, followeeId);
			followsRepository.deleteAll(follows);
			return "redirect:" + session.getAttribute("path");
		} else {
			Users follower = usersRepository.findById(followerId).get();
			Locale locale = new Locale(follower.getLanguage());
			String msg = messageSource.getMessage("msg.w.user_not_found", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("W", msg));
			return "redirect:/home";
		}
	}
	
	@GetMapping("/User/{user_id}/list/follows")
	public ModelAndView showUserFollows(@PathVariable(name = "user_id") int userId, 
		ModelAndView mv, RedirectAttributes redirectAttributes) {
		
		Integer myId = (Integer)session.getAttribute("user_id");
		Optional<Users> someUser = usersRepository.findById(userId);
		if(someUser.isPresent()) {
			List<Users> followsList = followService.getFollowsList(userId);
			List<Users> followersList = followService.getFollowersList(userId);
			
			List<Users> myFollowsList = followService.getFollowsList(myId);
			
			session.setAttribute("path", "/User/" + userId + "/list/follows");
			mv.setViewName("follows");
			mv.addObject("followsList", followsList);
			mv.addObject("followersList", followersList);
			mv.addObject("myFollowsList", myFollowsList);
			mv.addObject("searchData", new SearchData());
		} else {
			Users user = usersRepository.findById(myId).get();
			Locale locale = new Locale(user.getLanguage());
			
			mv.setViewName("redirect:/home");
			String msg = messageSource.getMessage("msg.w.user_not_found", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("W", msg));
		}
		
		return mv;		
	}
}
