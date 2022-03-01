package com.example.continuing.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
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
import com.example.continuing.entity.Temporaries;
import com.example.continuing.entity.Users;
import com.example.continuing.form.EmailData;
import com.example.continuing.form.ProfileData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.DeliveriesRepository;
import com.example.continuing.repository.FollowsRepository;
import com.example.continuing.repository.MeetingsRepository;
import com.example.continuing.repository.TemporariesRepository;
import com.example.continuing.repository.UsersRepository;
import com.example.continuing.service.FollowService;
import com.example.continuing.service.JoinService;
import com.example.continuing.service.MeetingService;
import com.example.continuing.service.StorageService;
import com.example.continuing.service.TemporaryService;
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
	private final MessageSource messageSource;
	private final DeliveriesRepository deliveriesRepository;
	private final TemporariesRepository temporariesRepository;
	private final TemporaryService temporaryService;
	
	@GetMapping("/User/{user_id}")
	public ModelAndView showUserDetail(@PathVariable(name = "user_id") int userId,
			ModelAndView mv, RedirectAttributes redirectAttributes) {
		Integer myId = (Integer)session.getAttribute("user_id");
		Optional<Users> someUser = usersRepository.findById(userId);
		someUser
			.ifPresentOrElse(user -> {
				List<Users> followsList = followService.getFollowsList(userId);
				List<Users> followersList = followService.getFollowersList(userId);
				List<Meetings> meetingList = meetingService.getUserMeetingList(user);
				
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
				Users user = usersRepository.findById(myId).get();
				Locale locale = new Locale(user.getLanguage());
				
				mv.setViewName("redirect:/home");
				String msg = messageSource.getMessage("msg.w.user_not_found", null, locale);
				redirectAttributes.addFlashAttribute("msg", new MessageDto("W", msg));
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
	
	@GetMapping("/User/delete")
	public String deleteUser(RedirectAttributes redirectAttributes) {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();
		Locale locale = new Locale(user.getLanguage());
		followsRepository.deleteByFollowerId(userId);
		followsRepository.deleteByFolloweeId(userId);
		deliveriesRepository.deleteByUserId(userId);
		meetingsRepository.deleteByHost(user);
		usersRepository.deleteById(userId);		
		// セッション情報をクリアする
		session.invalidate();
		String msg = messageSource.getMessage("msg.s.user_deleted", null, locale);
		redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
		return "redirect:/home";
	}
	
	@GetMapping("/User/updateForm")
	public ModelAndView updateProfileForm(ModelAndView mv) {
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();
		
		session.setAttribute("path", "/User/updateForm");
		session.setAttribute("mode", "profile");
		mv.setViewName("profile");
		mv.addObject("profileData", new ProfileData(user));
		mv.addObject("searchData", new SearchData());
		mv.addObject("emailData", new EmailData(user.getEmail()));
		return mv;
	}
	
	@PostMapping("/User/update")
	public ModelAndView updateProfile(@ModelAttribute @Validated ProfileData profileData,
			BindingResult result, ModelAndView mv, 
			RedirectAttributes redirectAttributes) {
		
		Integer userId = (Integer)session.getAttribute("user_id");
		Users oldData = usersRepository.findById(userId).get();
		Locale locale = new Locale(oldData.getLanguage());
		
		// エラーチェック
		boolean isValid = userService.isValid(profileData, oldData, result, locale); 
		if(!result.hasErrors() && isValid) {
			// エラーなし -> 更新
			Users user = profileData.toEntity(oldData, passwordEncoder);
			locale = new Locale(user.getLanguage());
			usersRepository.saveAndFlush(user);
			session.setAttribute("user_name", user.getName());
			
			mv.setViewName("redirect:/User/mypage");
			String msg = messageSource.getMessage("msg.s.user_updated", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
		} else {
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, new Locale(oldData.getLanguage()));
			
			session.setAttribute("path", "/User/updateForm");
			session.setAttribute("mode", "profile");
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
	
	@GetMapping("/User/profileImage/delete")
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
		List<Users> userList = usersRepository.findAll();
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
	
	@GetMapping("/User/setting")
	public ModelAndView showSetting(ModelAndView mv) {
		session.setAttribute("path", "/User/setting");
		mv.setViewName("setting");
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
	@PostMapping("/User/updateEmail")
	public ModelAndView sendAuthenticationEmail(@ModelAttribute @Validated EmailData emailData,
			BindingResult result, RedirectAttributes redirectAttributes,
			ModelAndView mv) {
		
		Integer userId = (Integer)session.getAttribute("user_id");
		Users user = usersRepository.findById(userId).get();
		Locale locale = new Locale(user.getLanguage());
		
		// エラーチェック
		boolean isValid = userService.isValid(emailData, result, locale);
		if(!result.hasErrors() && isValid) {
			// エラーなし -> 更新
			String token = userService.sendAuthenticationEmail(emailData.getEmail(), locale);
			Temporaries temporaries = new Temporaries(emailData.getEmail(), user, token);
			temporariesRepository.saveAndFlush(temporaries);
			
			String msg = messageSource.getMessage("msg.s.authentication_email_sent", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
			mv.setViewName("redirect:/User/mypage");
		} else {
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			session.setAttribute("path", "/User/updateForm");
			session.setAttribute("mode", "profile");
			mv.setViewName("profile");
			mv.addObject("profileData", new ProfileData(user));
			mv.addObject("searchData", new SearchData());
			mv.addObject("msg", new MessageDto("E", msg));
		} 
		
		return mv;
	}
	
	@GetMapping("/updateEmail/email/{email}/token/{token}")
	public String updateEmail(@PathVariable(name = "email") String email,
			@PathVariable(name = "token") String token, ModelAndView mv,
			Locale locale, RedirectAttributes redirectAttributes) {
		
		boolean isValid = temporaryService.isValid(email, token);
		if(isValid) {
			List<Temporaries> temporaryList = temporariesRepository.findByEmailOrderByCreatedAtDesc(email);
			Temporaries latestTemporary = temporaryList.get(0);
			Users user = usersRepository.findById(latestTemporary.getUserId()).get();
			user.setEmail(email);
			
			Date date = new Date();
			Timestamp timestamp = new Timestamp(date.getTime());
			user.setUpdatedAt(timestamp);
			usersRepository.saveAndFlush(user);
			temporariesRepository.deleteAll(temporaryList);
			
			String msg = messageSource.getMessage("msg.s.email_updated", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
		} else {
			String msg = messageSource.getMessage("msg.e.retry_email_update", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("E", msg));
		}
		return "redirect:/home";
	}
	
}
