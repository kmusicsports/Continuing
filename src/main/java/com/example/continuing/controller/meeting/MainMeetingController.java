package com.example.continuing.controller.meeting;

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
import com.example.continuing.entity.Joins;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.JoinsRepository;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.UsersRepository;
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
	private final UsersRepository usersRepository;
	private final MessageSource messageSource;
	
	@GetMapping("/Meeting/{meeting_id}")
	public ModelAndView showMeetingDetail(ModelAndView mv, 
			@PathVariable(name = "meeting_id") int meetingId,
			RedirectAttributes redirectAttributes, Locale locale) {
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
				mv.setViewName("redirect:/home");
				String msg = messageSource.getMessage("msg.w.meeting_not_found", null, locale);
				redirectAttributes.addFlashAttribute("msg", new MessageDto("W", msg));
			});
		return mv;
	}
	
	@GetMapping("/Meeting/join/{meeting_id}")
	public String joinMeeting(@PathVariable(name = "meeting_id") int meetingId,
			HttpServletRequest request, 
			RedirectAttributes redirectAttributes, Locale locale) {
		Optional<Meetings> someMeeting = meetingsRepository.findById(meetingId);
		if(someMeeting.isPresent()) {
			Integer myId = (Integer)session.getAttribute("user_id");
			Meetings meeting = someMeeting.get();
			Joins join = new Joins(myId, meeting);
			joinsRepository.saveAndFlush(join);
			
			Users user = usersRepository.findById(myId).get();
			meetingService.sendMail(meeting, user, "join", locale);
		} else {
			String msg = messageSource.getMessage("msg.w.meeting_not_found", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("W", msg));
			return "redirect:/home";
		}
		return "redirect:" + session.getAttribute("path");
	}
	
	@GetMapping("/Meeting/leave/{meeting_id}")
	public String leaveMeeting(@PathVariable(name = "meeting_id") int meetingId, 
			HttpServletRequest request, 
			RedirectAttributes redirectAttributes, Locale locale) {
		Optional<Meetings> someMeeting = meetingsRepository.findById(meetingId);
		if(someMeeting.isPresent()) {
			Integer myId = (Integer)session.getAttribute("user_id");
			Meetings meeting = someMeeting.get();
			List<Joins> joinList = joinsRepository.findByUserIdAndMeeting(myId, meeting);
			joinsRepository.deleteAll(joinList);
			
			Users user = usersRepository.findById(myId).get();
			meetingService.sendMail(meeting, user, "leave", locale);
		} else {
			String msg = messageSource.getMessage("msg.w.meeting_not_found", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("W", msg));
			return "redirect:/home";
		}
		return "redirect:" + session.getAttribute("path");
	}
	
	@GetMapping("/Meeting/cancel")
	public String cancel(HttpServletRequest request) {
		return "redirect:" + session.getAttribute("path");
	}
	
	@GetMapping("/Meeting/check/{meeting_id}")
	public String joinCheck(@PathVariable(name = "meeting_id") int meetingId,
			RedirectAttributes redirectAttributes, Locale locale) {
		Optional<Meetings> someMeeting = meetingsRepository.findById(meetingId);
		if(someMeeting.isPresent()) {
			Meetings meeting = someMeeting.get();
			Integer myId = (Integer)session.getAttribute("user_id");
			String warningMessage = meetingService.joinCheck(meeting, myId, locale);
			if(warningMessage == null) {
				if(meeting.getHost().getId() == myId) {
					return "redirect:" +  meeting.getStartUrl();
				} else {
					return "redirect:" +  meeting.getJoinUrl();
				}				
			} else {
				redirectAttributes.addFlashAttribute("msg", new MessageDto("W", warningMessage));
				return "redirect:/Meeting/" + meetingId;
			}
		} else {
			String msg = messageSource.getMessage("msg.w.meeting_not_found", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("W", msg));
			return "redirect:/home";
		}
	}
	
	@GetMapping("/Meeting/list/mine/today")
	public ModelAndView showTodayMyMeeting(ModelAndView mv) {
		Integer myId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(myId).get();
		List<Meetings> meetingList = meetingService.getTodayMeetingList(user);
		List<Meetings> myJoinMeetingList = joinService.getJoinMeetingList(myId);
		
		mv.setViewName("todayMyMeetings");
		mv.addObject("meetingList", meetingList);
		mv.addObject("myJoinMeetingList", myJoinMeetingList);
		mv.addObject("searchData", new SearchData());
		return mv;
	}
}
