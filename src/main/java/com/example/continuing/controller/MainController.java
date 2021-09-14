package com.example.continuing.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.dao.MeetingsDaoImpl;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Topics;
import com.example.continuing.entity.Users;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.TopicsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;
import com.example.continuing.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MainController {

	private final HttpSession session;
	private final FollowService followService;
	private final TopicsRepository topicsRepository;
	private final JoinService joinService;
	private final MeetingService meetingService;
	private final UserService userService;
	private final UsersRepository usersRepository;
	
	@PersistenceContext
    private EntityManager entityManager;
	private MeetingsDaoImpl meetingsDaoImpl;
	
	@PostConstruct
    public void init() {
		meetingsDaoImpl = new MeetingsDaoImpl(entityManager);
	}
	
	
	@GetMapping("/")
	public String showHome() {
		return "redirect:/home";
	}
	
	@GetMapping("/home")
	public ModelAndView showHome(ModelAndView mv, 
			@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
		// sessionから前回の検索条件を取得
		SearchData searchData = (SearchData)session.getAttribute("searchData");
		if (searchData == null) {
			searchData = new SearchData();
			session.setAttribute("searchData", searchData);
		}
		
		 // sessionから前回のpageableを取得
        Pageable prevPageable = (Pageable)session.getAttribute("prevPageable");
        if (prevPageable == null) {
            // なければ@PageableDefaultを使う
            prevPageable = pageable;
            session.setAttribute("prevPageable", prevPageable);
        }
		
		Page<Meetings> meetingPage = meetingsDaoImpl.findByCriteria(searchData, prevPageable);
		List<Users> userList = userService.getSearchReuslt(searchData);
		List<Topics> topicList = topicsRepository.findAll();
		List<Users> userRanking = usersRepository.findTop3ByOrderByContinuousDaysDesc();
		
		Integer userId = (Integer)session.getAttribute("user_id");
		List<Users> myFollowsList = followService.getFollowsList(userId);
		List<Meetings> myJoinMeetingList = joinService.getJoinMeetingList(userId);
		
		session.setAttribute("path", "/home");
		mv.setViewName("home");
		mv.addObject("meetingPage", meetingPage);
		mv.addObject("meetingList", meetingPage.getContent());
		mv.addObject("userList", userList);
		mv.addObject("topicList", topicList);
		mv.addObject("myFollowsList", myFollowsList);
		mv.addObject("myJoinMeetingList", myJoinMeetingList);
		mv.addObject("searchData", searchData);
		mv.addObject("userRanking", userRanking);
		return mv;
	}
	
	@PostMapping("/search")
	public ModelAndView search(ModelAndView mv, SearchData searchData, 
			@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable) {
		List<Topics> topicList = topicsRepository.findAll();
		List<Users> userRanking = usersRepository.findTop3ByOrderByContinuousDaysDesc();
		
		Integer userId = (Integer)session.getAttribute("user_id");
		List<Users> myFollowsList = followService.getFollowsList(userId);
		List<Meetings> myJoinMeetingList = joinService.getJoinMeetingList(userId);
		
		session.setAttribute("path", "/home");
		mv.setViewName("home");
		
		if (meetingService.isValid(searchData)) {			
			Page<Meetings> meetingPage = meetingsDaoImpl.findByCriteria(searchData, pageable);
			List<Users> userList = userService.getSearchReuslt(searchData);
			
			// 入力された検索条件をsessionへ保存
			session.setAttribute("searchData", searchData);
			mv.addObject("meetingPage", meetingPage);
			mv.addObject("meetingList", meetingPage.getContent());
			mv.addObject("userList", userList);
						
			if (meetingPage.getContent().size() == 0) {
				System.out.println("該当するミーティングはありません");
				// 該当なかったらメッセージを表示
				
			} else if(userList.size() == 0) {
				System.out.println("該当するユーザーアカウントはありません");
				// 該当なかったらメッセージを表示
				
			}
		} else {
			System.out.println("入力に間違いがあります");
			mv.addObject("meetingPage", null);
			mv.addObject("meetingList", null);
			mv.addObject("userList", null);
			// 検索条件エラーあり -> エラーメッセージをセット
			
		}
		
		mv.addObject("topicList", topicList);
		mv.addObject("myFollowsList", myFollowsList);
		mv.addObject("myJoinMeetingList", myJoinMeetingList);
		mv.addObject("searchData", searchData);
		mv.addObject("userRanking", userRanking);
		return mv;
	}
	
}
