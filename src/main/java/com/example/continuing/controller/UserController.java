package com.example.continuing.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.continuing.dto.MessageDto;
import com.example.continuing.entity.Meetings;
import com.example.continuing.entity.Users;
import com.example.continuing.form.ProfileData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.FollowsRepository;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;
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
	private final PasswordEncoder passwordEncoder;
	private final MeetingService meetingService;
	
	@GetMapping("/User/{user_id}")
	public ModelAndView showUserDetail(ModelAndView mv, @PathVariable(name = "user_id") int userId,
			RedirectAttributes redirectAttributes) {
		Optional<Users> someUser = usersRepository.findById(userId);
		someUser
			.ifPresentOrElse(user -> {
				List<Users> followsList = followService.getFollowsList(userId);
				List<Users> followersList = followService.getFollowersList(userId);
				List<Meetings> meetingList = meetingService.getUserMeetingList(user);
				
				Integer myId = (Integer)session.getAttribute("user_id");
				List<Users> myFollowsList = followService.getFollowsList(myId);
				List<Meetings> myJoinMeetingList = joinService.getJoinMeetingList(myId);
				
				session.setAttribute("path", "/User/" + userId);
				mv.setViewName("userDetail");
				mv.addObject("user", user);		
				mv.addObject("followsList", followsList);
				mv.addObject("followersList", followersList);
				mv.addObject("myFollowsList", myFollowsList);
				mv.addObject("meetingList", meetingList);
				mv.addObject("myJoinMeetingList", myJoinMeetingList);
				mv.addObject("searchData", new SearchData());
			}, () -> {
				String msg = "存在しないユーザーです。";
				mv.setViewName("redirect:/home");
				redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
			});
		return mv;
	}
	
	@GetMapping("/User/mypage")
	public ModelAndView showMyPage(ModelAndView mv) {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();			
		List<Users> followsList = followService.getFollowsList(userId);
		List<Users> followersList = followService.getFollowersList(userId);
		List<Meetings> joinMeetingList = joinService.getJoinMeetingList(userId);
		List<Meetings> meetingList = meetingService.getUserMeetingList(user);
		
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
	public ModelAndView updateProfile(@ModelAttribute @Validated ProfileData profileData,
			BindingResult result, ModelAndView mv, RedirectAttributes redirectAttributes) {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users oldData = usersRepository.findById(userId).get();
		// エラーチェック
		boolean isValid = userService.isValid(profileData, oldData, result); 
		if(!result.hasErrors() && isValid) {
			// エラーなし -> 更新
			Users user = profileData.toEntity(oldData, passwordEncoder);
			usersRepository.saveAndFlush(user);
			String msg = "プロフィールを更新しました。";
			mv.setViewName("redirect:/User/mypage");
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
		} else {
			String msg = "入力に誤りがあります。";
			mv.setViewName("profile");
			mv.addObject("searchData", new SearchData());
			mv.addObject("msg", new MessageDto("E", msg));
		}
		
		return mv;
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
		Map<Integer, Integer> rankingMap = userService.makeRankingMap(userList);
		
		Integer myId = (Integer)session.getAttribute("user_id");
		List<Users> myFollowsList = followService.getFollowsList(myId);
		
		session.setAttribute("path", "/User/list/ranking");
		mv.setViewName("userRanking");
		mv.addObject("searchData", new SearchData());
		mv.addObject("userList", userList);
		mv.addObject("myFollowsList", myFollowsList);
		mv.addObject("rankingMap", rankingMap);
		return mv;
	}
	
}
