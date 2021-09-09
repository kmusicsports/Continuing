package com.example.continuing.controller.meeting;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Meetings;
import com.example.continuing.repository.MeetingsRepository;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class MainMeetingController {

	private final MeetingsRepository meetingsRepository;
	
	@GetMapping("/Meeting/{meeting_id}")
	public ModelAndView showMeetingDetail(ModelAndView mv, @PathVariable(name = "meeting_id") int meetingId) {
		Optional<Meetings> meeting = meetingsRepository.findById(meetingId); 
		if (meeting.isPresent()) {			
			mv.setViewName("meetingDetail");
			mv.addObject("meeting", meeting.get());
		} else {
			System.out.println("存在しないミーティングです");
			mv.setViewName("redirect:/home");
		}
		return mv;
	}
	
}
