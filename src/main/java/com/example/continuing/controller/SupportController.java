package com.example.continuing.controller;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.continuing.dto.MessageDto;
import com.example.continuing.form.ContactData;
import com.example.continuing.form.SearchData;
import com.example.continuing.service.UserService;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class SupportController {

	private final HttpSession session;
	private final UserService userService;
	private final MessageSource messageSource;
	
	@GetMapping("/help")
	public ModelAndView showHelp(ModelAndView mv) {
		session.setAttribute("path", "/help");
		mv.setViewName("help");
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
	@GetMapping("/contactForm")
	public ModelAndView showContactForm(ModelAndView mv) {
		session.setAttribute("path", "/contactForm");
		mv.setViewName("contactForm");
		mv.addObject("searchData", new SearchData());
		mv.addObject("contactData", new ContactData());
		return mv;
	}
	
	@PostMapping("/contact")
	public ModelAndView contact(@ModelAttribute @Validated ContactData contactData,
			BindingResult result, ModelAndView mv, Locale locale,
			RedirectAttributes redirectAttributes) {
		
		if(!result.hasErrors()) {
			userService.sendContactEmail(contactData);
			String msg = messageSource.getMessage("msg.s.contact", null, locale);
			redirectAttributes.addFlashAttribute("msg", new MessageDto("S", msg));
			mv.setViewName("redirect:/contactForm");
		} else {
			String msg = messageSource.getMessage("msg.e.input_something_wrong", null, locale);
			
			session.setAttribute("path", "/contactForm");
			mv.setViewName("contactForm");
			mv.addObject("searchData", new SearchData());
			mv.addObject("msg", new MessageDto("E", msg));
		}		
		
		return mv;
	}
	
}
