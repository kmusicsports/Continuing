package com.example.continuing.controller.meeting;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.continuing.dto.MessageDto;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;
import com.example.continuing.zoom.ZoomApiIntegration;
import com.example.continuing.zoom.ZoomDetails;
import com.github.scribejava.core.model.OAuth2AccessToken;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DeleteMeetingController {

	private final MeetingsRepository meetingsRepository;
	private final HttpSession session;
	public Integer id = null;
	private final ZoomApiIntegration zoomApiIntegration;
	private final JoinService joinService;
	private final MeetingService meetingService;
	private final MessageSource messageSource;
	private final UsersRepository usersRepository;

	@GetMapping("/Meeting/delete/{id}")
    public String createRedirect(@PathVariable(name = "id") int meetingId,
    		RedirectAttributes redirectAttributes) {

		Integer myId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(myId).get();
		Locale locale = new Locale(user.getLanguage());

		Optional<Meetings> someMeeting = meetingsRepository.findById(meetingId);
		if(someMeeting.isPresent()) {
			if(someMeeting.get().getHost().getId().equals(myId)) {
				System.out.println("--delete meeting api request");
				
				this.id = meetingId;
				
				//ミーティング情報
				ZoomDetails.setZOOM_STATE("zoom_delete");
				String zoomAuthUrl = zoomApiIntegration.getAuthorizationUrl(session);
				System.out.println("ZoomAuthUrl: " + zoomAuthUrl);
				return "redirect:" + zoomAuthUrl;
			} else {
				String msg = messageSource.getMessage("msg.e.meeting_not_yours", null, locale);
				redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
				return "redirect:/Meeting/" + meetingId;
			}
		} else {
			String msg = messageSource.getMessage("msg.w.meeting_not_found", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("W", msg));
			return "redirect:/home";
		}
        
    }
	
	@RequestMapping(value = "/delete/meeting/redirect", method = { RequestMethod.GET, RequestMethod.POST })
	public String deleteMeeting(@RequestParam String code, @RequestParam String state,
			RedirectAttributes redirectAttributes) throws IOException {
		System.out.println("Start deleting the meeting.");

    	Meetings meeting = meetingsRepository.findById(id).get();
    	String meetingId = meeting.getMeetingId();

		OAuth2AccessToken oauthToken = zoomApiIntegration.getAccessToken(session, code, state);
		zoomApiIntegration.deleteMeeting(oauthToken, meetingId);
				
		List<Users> joinUserList = joinService.getJoinUserList(meeting);
		for(Users user : joinUserList) {
			meetingService.sendMail(meeting, user, "delete", new Locale(user.getLanguage()));			
		}
		
		meetingsRepository.deleteById(id);
		
		String msg = messageSource.getMessage("msg.s.meeting_deleted", null, new Locale(meeting.getHost().getLanguage()));
		redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
		return "redirect:/User/mypage";
	}
}
