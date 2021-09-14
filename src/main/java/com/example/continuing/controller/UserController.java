package com.example.continuing.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.comparator.MeetingsComparator;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.form.ProfileData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.FollowsRepository;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.StorageService;
import com.example.continuing.service.UserService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class UserController {

	private final UsersRepository usersRepository;
	private final HttpSession session;
	private final UserService userService;
	private final StorageService storageService;
	private final FollowService followService;
	private final FollowsRepository followsRepository; 
	private final MeetingsRepository meetingsRepository;
	private final JoinService joinService;
	
	@GetMapping("/User/{user_id}")
	public ModelAndView showUserDetail(ModelAndView mv, @PathVariable(name = "user_id") int userId) {
		Optional<Users> user = usersRepository.findById(userId);
		if(user.isPresent()) {
			List<Users> followsList = followService.getFollowsList(userId);
			List<Users> followersList = followService.getFollowersList(userId);
			
			List<Meetings> meetingList = meetingsRepository.findByHost(user.get());
			List<Meetings> joinMeetingList = joinService.getJoinMeetingList(userId);
			meetingList.addAll(joinMeetingList);
			Collections.sort(meetingList, new MeetingsComparator());
			
			Integer myId = (Integer)session.getAttribute("user_id");
			List<Users> myFollowsList = followService.getFollowsList(myId);
			List<Meetings> myJoinMeetingList = joinService.getJoinMeetingList(myId);
			
			session.setAttribute("path", "/User/" + userId);
			mv.setViewName("userDetail");
			mv.addObject("user", user.get());		
			mv.addObject("followsList", followsList);
			mv.addObject("followersList", followersList);
			mv.addObject("myFollowsList", myFollowsList);
			mv.addObject("meetingList", meetingList);
			mv.addObject("myJoinMeetingList", myJoinMeetingList);
			mv.addObject("searchData", new SearchData());
		} else {
			System.out.println("存在しないユーザーです");
			mv.setViewName("redirect:/home");
		}
		return mv;
	}
	
	@GetMapping("/User/mypage")
	public ModelAndView showMyPage(ModelAndView mv) {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();			
		List<Users> followsList = followService.getFollowsList(userId);
		List<Users> followersList = followService.getFollowersList(userId);
		
		List<Meetings> meetingList = meetingsRepository.findByHost(user);
		List<Meetings> joinMeetingList = joinService.getJoinMeetingList(userId);
		meetingList.addAll(joinMeetingList);
		Collections.sort(meetingList, new MeetingsComparator());
		
		session.setAttribute("path", "/User/mypage");
		mv.setViewName("userDetail");
		mv.addObject("user", user);
		mv.addObject("followsList", followsList);
		mv.addObject("followersList", followersList);
		mv.addObject("myFollowsList", followsList);
		mv.addObject("meetingList", meetingList);
		mv.addObject("myJoinMeetingList", joinMeetingList);
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
	@GetMapping("/User/setting")
	public ModelAndView setting(ModelAndView mv) {
		mv.setViewName("setting");
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
	@GetMapping("/User/delete")
	public String deleteUser() {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();
		followsRepository.deleteByFollowerId(userId);
		followsRepository.deleteByFolloweeId(userId);
		meetingsRepository.deleteByHost(user);
		usersRepository.deleteById(userId);		
		// セッション情報をクリアする
		session.invalidate();
		return "redirect:/home";
	}
	
	
	@GetMapping("/User/updateForm")
	public ModelAndView updateProfileForm(ModelAndView mv) {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();
		mv.setViewName("profile");
		mv.addObject("profileData", new ProfileData(user));
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
	@PostMapping("/User/update")
	public String updateProfile(ProfileData profileData) {
		Integer userId = (Integer)session.getAttribute("user_id");
		if(userId == null) {
			System.out.println("Error: ログインし直してください");
			return "redirect:/User/showLogin";
		} else {
			Users oldData = usersRepository.findById(userId).get();
			// エラーチェック
			boolean isValid = userService.isValid(profileData, oldData); 
			if(isValid) {
				// エラーなし -> 更新
				Users user = profileData.toEntity(oldData);
				usersRepository.saveAndFlush(user);
				return "redirect:/User/mypage";
			} else {
				
				return "redirect:/User/updateForm";
			}			
		}
	}
	
	@PostMapping("/User/profileImage/upload")
	public String uploadProfileImage(@RequestParam(value = "file") MultipartFile file) {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();
		// 既にプロフィール画像があれば、それをS3から削除する
		String oldProfileImage = user.getProfileImage();
		if(oldProfileImage != null) {
			storageService.deleteFile(oldProfileImage);
		}
		// アップロード & 変更
		String profile_image = storageService.uploadFile(file);
		user.setProfileImage(profile_image);
		usersRepository.saveAndFlush(user);
		return "redirect:/User/updateForm";
    }
	
	@PostMapping("/User/profileImage/delete")
	public String deleteProfileImage() {
		Integer id = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(id).get();
		String profile_image = user.getProfileImage();
		if(profile_image != null) {
			storageService.deleteFile(profile_image);
		}
		user.setProfileImage(null);
		usersRepository.saveAndFlush(user);
		return "redirect:/User/updateForm";
    }

	@GetMapping("/User/list/ranking")
	public ModelAndView showUserRanking(ModelAndView mv) {
		List<Users> userList = usersRepository.findAllByOrderByContinuousDaysDesc();
		
		Integer myId = (Integer)session.getAttribute("user_id");
		List<Users> myFollowsList = followService.getFollowsList(myId);
		
		session.setAttribute("path", "/User/list/ranking");
		mv.setViewName("userRanking");
		mv.addObject("searchData", new SearchData());
		mv.addObject("userList", userList);
		mv.addObject("myFollowsList", myFollowsList);
		return mv;
	}
	
}
