package com.example.continuing.controller.meeting;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
	private ZoomApiIntegration ZoomApiIntegration;
	
	@Autowired
    private void setZoomApiIntegration(ZoomApiIntegration ZoomApiIntegration) {
        this.ZoomApiIntegration = ZoomApiIntegration;
    }
	
	@GetMapping("/Meeting/delete/{id}")
    public void createRedirect(@PathVariable(name = "id") int id, HttpServletResponse response) {
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
