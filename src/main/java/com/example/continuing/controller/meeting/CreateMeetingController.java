package com.example.continuing.controller.meeting;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Topics;
import com.example.continuing.form.MeetingData;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.TopicsRepository;
import com.example.continuing.service.MeetingService;
import com.example.continuing.zoom.ZoomApiIntegration;
import com.example.continuing.zoom.ZoomDetails;
import com.github.scribejava.core.model.OAuth2AccessToken;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CreateMeetingController {
	
	private final TopicsRepository topicsRepository;
	private final MeetingService meetingService;
	private final HttpSession session;
	private final MeetingsRepository meetingsRepository;
	private MeetingData meetingData = new MeetingData(); 
	private ZoomApiIntegration zoomApiIntegration;

	@Autowired
	private void setZoomApiIntegration(ZoomApiIntegration zoomApiIntegration) {
		this.zoomApiIntegration = zoomApiIntegration;
	}

	
	@GetMapping("/Meeting/createForm")
	public ModelAndView createMeetingForm(ModelAndView mv) {
		List<Topics> topicList = topicsRepository.findAll();
		
		mv.setViewName("meetingForm");
		mv.addObject("topicList", topicList);
		mv.addObject("meetingData", new MeetingData());
		return mv;
	}
	
	// call
	@PostMapping("/Meeting/create")
	public void createRedirect(MeetingData meetingData, HttpServletResponse response) {
		// エラーチェック
		boolean isValid = meetingService.isValid(meetingData);
		if(isValid) {
			System.out.println("---create meeting api request");
			System.out.println("--Zoom 会議作成");
			this.meetingData = meetingData;
			
			// 会議作成状態に設定
			ZoomDetails.setZOOM_STATE("zoom_create");
			
			String zoomAuthUrl = zoomApiIntegration.getAuthorizationUrl(session);
			System.out.println("ZoomAuthUrl: " + zoomAuthUrl);
			try {
				response.sendRedirect(zoomAuthUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	// callback
	@RequestMapping(value = "/create/meeting/redirect", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView createMeeting(@RequestParam String code, @RequestParam String state, ModelAndView mv) throws IOException {
		try {
			System.out.println("会議を作成しました");
			OAuth2AccessToken oauthToken = zoomApiIntegration.getAccessToken(session, code, state);
			String apiResult = zoomApiIntegration.createMeeting(oauthToken, meetingData.toDto());
			System.out.println("apiResult: " + apiResult);
			
			// Json 配列から Json Objectに変換
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(apiResult);
			System.out.println("JsonObject result: " + jsonObject);
			
			Integer userId = (Integer)session.getAttribute("user_id");
			Meetings meeting = meetingData.toEntity(jsonObject, userId);
			meetingsRepository.saveAndFlush(meeting);
			
			mv.setViewName("redirect:/User/mypage");
//			mv.setViewName("redirect:/Meeting/" + meeting.getId());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("会議の作成に失敗しました");
			mv.setViewName("redirect:/User/mypage");
		} 
		return mv;
	}

}
