package com.example.continuing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {
	
	@GetMapping("/Meeting/list/all")
	public ModelAndView showHome(ModelAndView mv) {
		mv.setViewName("home");
		return mv;
	}
}
