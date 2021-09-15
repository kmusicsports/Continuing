package com.example.continuing.controller.meeting;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Joins;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.JoinsRepository;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class MainMeetingController {

	private final MeetingsRepository meetingsRepository;
	private final JoinService joinService;
	private final HttpSession session;
	private final FollowService followService;
	private final JoinsRepository joinsRepository;
	private final MeetingService meetingService;
	
	@GetMapping("/Meeting/{meeting_id}")
	public ModelAndView showMeetingDetail(ModelAndView mv, @PathVariable(name = "meeting_id") int meetingId) {
		Optional<Meetings> someMeeting = meetingsRepository.findById(meetingId);
		someMeeting
			.ifPresentOrElse(meeting -> {
				Integer myId = (Integer)session.getAttribute("user_id");
				List<Meetings> myJoinMeetingList = joinService.getJoinMeetingList(myId);
				List<Users> myFollowsList = followService.getFollowsList(myId);
				
				List<Users> joinUserList = joinService.getJoinUserList(meeting);
				
				session.setAttribute("path", "/Meeting/" + meetingId);
				mv.setViewName("meetingDetail");
				mv.addObject("meeting", meeting);
				mv.addObject("myJoinMeetingList", myJoinMeetingList);
				mv.addObject("joinUserList", joinUserList);
				mv.addObject("myFollowsList", myFollowsList);
				mv.addObject("searchData", new SearchData());
			}, () -> {
				System.out.println("存在しないミーティングです");
				mv.setViewName("redirect:/home");
			});
		return mv;
	}
	
	@GetMapping("/Meeting/join/{meeting_id}")
	public String joinMeeting(@PathVariable(name = "meeting_id") int meetingId, HttpServletRequest request) {
		Optional<Meetings> someMeeting = meetingsRepository.findById(meetingId);
		if(someMeeting.isPresent()) {
			Integer myId = (Integer)session.getAttribute("user_id");
			Joins join = new Joins(myId, someMeeting.get());
			joinsRepository.saveAndFlush(join);
		} else {
			System.out.println("存在しないミーティングです");
			return "redirect:/home";
		}
		return "redirect:" + session.getAttribute("path");
	}
	
	@GetMapping("/Meeting/leave/{meeting_id}")
	public String leaveMeeting(@PathVariable(name = "meeting_id") int meetingId, HttpServletRequest request) {
		Optional<Meetings> someMeeting = meetingsRepository.findById(meetingId);
		if(someMeeting.isPresent()) {
			Integer myId = (Integer)session.getAttribute("user_id");
			List<Joins> joinList = joinsRepository.findByUserIdAndMeeting(myId, someMeeting.get());
			joinsRepository.deleteAll(joinList);
		} else {
			System.out.println("存在しないミーティングです");
			return "redirect:/home";
		}
		return "redirect:" + session.getAttribute("path");
	}
	
	@GetMapping("/Meeting/cancel")
	public String cancel(HttpServletRequest request) {
		return "redirect:" + session.getAttribute("path");
	}
	
	@GetMapping("/Meeting/check/{meeting_id}")
	public String joinCheck(@PathVariable(name = "meeting_id") int meetingId) {
		Optional<Meetings> someMeeting = meetingsRepository.findById(meetingId);
		String meetingUrl = null;
		if(someMeeting.isPresent()) {
			Meetings meeting = someMeeting.get();
			Integer myId = (Integer)session.getAttribute("user_id");
			meetingService.joinCheck(meeting, myId);
			if(meeting.getHost().getId() == myId) {
				meetingUrl = meeting.getStartUrl();
			} else {
				meetingUrl = meeting.getJoinUrl();
			}
		} else {
			System.out.println("存在しないミーティングです");
			return "redirect:/home";
		}
		return "redirect:" +  meetingUrl;
	}
	
	@GetMapping("/Meeting/list/mine/today")
	public ModelAndView showTodayMyMeeting(ModelAndView mv) {
		mv.setViewName("todayMyMeetings");
		mv.addObject("searchData", new SearchData());
		return mv;
	}
}
