package com.example.continuing.controller.meeting;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.continuing.dto.MessageDto;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Topics;
import com.example.continuing.entity.Users;
import com.example.continuing.form.MeetingData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.TopicsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
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
	private final ZoomApiIntegration zoomApiIntegration;
	private final UsersRepository usersRepository;
	private final FollowService followService;
	private final MessageSource messageSource;
	
	@GetMapping("/Meeting/showCreateForm")
	public ModelAndView createMeetingForm(ModelAndView mv) {
		List<Topics> topicList = topicsRepository.findAll();
		
		session.setAttribute("mode", "create");
		mv.setViewName("meetingForm");
		mv.addObject("topicList", topicList);
		mv.addObject("meetingData", new MeetingData());
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
	// call
	@PostMapping("/Meeting/create")
	public ModelAndView createRedirect(@ModelAttribute @Validated MeetingData meetingData, 
			BindingResult result, HttpServletResponse response,
			ModelAndView mv) {
		// エラーチェック
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();
		Locale locale = new Locale(user.getLanguage());
		boolean isValid = meetingService.isValid(meetingData, true, result, locale);
		if(!result.hasErrors() && isValid) {
			System.out.println("-create meeting api request");
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
			
			return null;
		} else {
			List<Topics> topicList = topicsRepository.findAll();
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			
			session.setAttribute("mode", "create");
			mv.setViewName("meetingForm");
			mv.addObject("topicList", topicList);
			mv.addObject("searchData", new SearchData());
			mv.addObject("msg", new MessageDto("E", msg));
			return mv;
		}
	}
	
	// callback
	@RequestMapping(value = "/create/meeting/redirect", method = { RequestMethod.GET, RequestMethod.POST })
	public String createMeeting(@RequestParam String code,
			@RequestParam String state, ModelAndView mv, 
			RedirectAttributes redirectAttributes) throws IOException {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();
		Locale locale = new Locale(user.getLanguage());
		try {
			System.out.println("Start creating the meeting.");
			OAuth2AccessToken oauthToken = zoomApiIntegration.getAccessToken(session, code, state);
			String apiResult = zoomApiIntegration.createMeeting(oauthToken, meetingData.toDto());
			System.out.println("apiResult: " + apiResult);
			
			// Json 配列から Json Objectに変換
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(apiResult);
			System.out.println("JsonObject result: " + jsonObject);
			
			Meetings meeting = meetingData.toEntity(jsonObject, user);
			meetingsRepository.saveAndFlush(meeting);
			
			List<Users> followersList = followService.getFollowersList(userId);
			for(Users follower : followersList) {
				meetingService.sendMail(meeting, follower, "create", locale);			
			}
			
			String msg = messageSource.getMessage("msg.s.meeting_created", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
			return "redirect:/Meeting/" + meeting.getId();
		} catch (Exception e) {
			e.printStackTrace();
			String msg = messageSource.getMessage("msg.e.meeting_create_failed", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
			return "redirect:" + session.getAttribute("path");
		} 
	}

}
