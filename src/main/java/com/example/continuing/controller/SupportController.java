package com.example.continuing.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.continuing.form.SearchData;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class SupportController {

	private final HttpSession session;
	
	@GetMapping("/help")
	public ModelAndView showHelp(ModelAndView mv) {
		session.setAttribute("path", "/help");
		mv.setViewName("help");
		mv.addObject("searchData", new SearchData());
		return mv;
	}
	
}
