package com.example.continuing.controller;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.continuing.dto.MessageDto;
import com.example.continuing.entity.Deliveries;
import com.example.continuing.entity.Users;
import com.example.continuing.form.DeliveryData;
import com.example.continuing.form.SearchData;
import com.example.continuing.repository.DeliveriesRepository;
import com.example.continuing.repository.UsersRepository;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class DeliveryController {
	
	private final DeliveriesRepository deliveriesRepository;
	private final UsersRepository usersRepository;
	private final MessageSource messageSource;

	@GetMapping("/User/setting/emailDelivery")
	public ModelAndView showSettingForm(ModelAndView mv, HttpSession session) {
		Integer userId = (Integer)session.getAttribute("user_id");
		Deliveries deliveries = deliveriesRepository.findByUserId(userId).get();
		mv.setViewName("deliverySetting");
		mv.addObject("searchData", new SearchData());
		mv.addObject("deliveryData", new DeliveryData(deliveries));
		return mv;
	}
	
	@PostMapping("/User/setting/emailDelivery/update")
	public String updateEmailDelivery(DeliveryData deliveryData, 
			HttpSession session, RedirectAttributes redirectAttributes) {
		
		Integer userId = (Integer)session.getAttribute("user_id");
		Deliveries oldData = deliveriesRepository.findByUserId(userId).get();
		Deliveries newData = deliveryData.toEntity(oldData);
		deliveriesRepository.saveAndFlush(newData);
		
		Users user = usersRepository.findById(userId).get(); 
		Locale locale = new Locale(user.getLanguage());
		
		String msg = messageSource.getMessage("msg.s.delivery_updated", null, locale);
		redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
		
		return "redirect:/User/setting";
	}
	
}
