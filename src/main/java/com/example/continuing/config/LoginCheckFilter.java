package com.example.continuing.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

@Component
public class LoginCheckFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest)request;
		HttpServletResponse httpResponse = (HttpServletResponse)response;

		String requestURI = httpRequest.getRequestURI();
		if(requestURI.startsWith("/User") || requestURI.startsWith("/Meeting")) {
			// sessionが存在するか
			HttpSession session = httpRequest.getSession(false);
			if(session == null) {
				// session無し -> Login画面へリダイレクト
				httpResponse.sendRedirect("/showLogin");
			} else {
				// sessionにuserIdが存在するか(=loginしたか？)
				Integer userId = (Integer)session.getAttribute("user_id");
				if(userId == null) {
					// userId無し　-> loginしていない -> Login画面へリダイレクト
					httpResponse.sendRedirect("/showLogin");
				} else {
					// loginしている -> コントローラーへリクエストを渡す
					chain.doFilter(request, response);
				}
			} 
		} else {
			// check対象外 -> コントローラーへリクエストを渡す
			chain.doFilter(request, response);
		}
	}

}