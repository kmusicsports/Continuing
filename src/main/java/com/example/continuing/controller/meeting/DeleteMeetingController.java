package com.example.continuing.controller.meeting;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Meetings;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.zoom.ZoomApiIntegration;
import com.example.continuing.zoom.ZoomDetails;
import com.github.scribejava.core.model.OAuth2AccessToken;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DeleteMeetingController {

	private final MeetingsRepository meetingsRepository;
	private final HttpSession session;
	private Integer id = null;
	private final ZoomApiIntegration ZoomApiIntegration;

	@GetMapping("/Meeting/delete/{id}")
    public ModelAndView createRedirect(@PathVariable(name = "id") int id, HttpServletResponse response, ModelAndView mv) {
		Optional<Meetings> someMeeting = meetingsRepository.findById(id);
		if(someMeeting.isPresent()) {
			Integer myId = (Integer)session.getAttribute("user_id");
			if(someMeeting.get().getHost().getId().equals(myId)) {
				System.out.println("--delete meeting api request");
				
				this.id = id;
				
				//ミーティング情報
				ZoomDetails.setZOOM_STATE("zoom_delete");
				String zoomAuthUrl = ZoomApiIntegration.getAuthorizationUrl(session);
				System.out.println("ZoomAuthUrl: " + zoomAuthUrl);
				try {
					response.sendRedirect(zoomAuthUrl);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return null;
			} else {
				System.out.println("他ユーザーのミーティングです");
				mv.setViewName("redirect:/Meeting/" + id);
				return mv;
			}				
		} else {
			System.out.println("存在しないミーティングです");
			mv.setViewName("redirect:/home");
			return mv;
		}
        
    }
	
	@RequestMapping(value = "/delete/meeting/redirect", method = { RequestMethod.GET, RequestMethod.POST })
	public String deleteMeeting(@RequestParam String code, @RequestParam String state)
            throws IOException {
		System.out.println("会議の削除を開始します");

    	Meetings meeting = meetingsRepository.findById(id).get();
    	String meetingId = meeting.getMeetingId();
    	
		OAuth2AccessToken oauthToken = ZoomApiIntegration.getAccessToken(session, code, state);
		ZoomApiIntegration.deleteMeeting(oauthToken, meetingId);
		
		meetingsRepository.deleteById(id);
		
		System.out.println("会議の削除に成功しました");
		
		return "redirect:/User/mypage";
	}
}
