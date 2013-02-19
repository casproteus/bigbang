package com.aeiou.bigbang.web;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.aeiou.bigbang.domain.Login;
import com.aeiou.bigbang.util.CookieHelper;

@RequestMapping("/login")
@Controller
public class LoginController {

	@Inject
	private CookieHelper cookieHelper;
	
	@RequestMapping(method = RequestMethod.GET)
	public String defaultView(Model model, HttpServletRequest request, HttpServletResponse response) {
		return createForm(model, request, response);
	}

	@RequestMapping(params = "form", method = RequestMethod.GET)
	public String createForm(Model model, HttpServletRequest request, HttpServletResponse response) {
		Login login = new Login();

		// Remove old cookies
		cookieHelper.deleteCookie("JSESSIONID", request, response);

		// Force the creation of a session
		request.getSession(true);

		model.addAttribute("login", login);

		if (request.getParameter("activation_needed") != null) {
			return "activation/activation_needed";
		} else {
			return "login";
		}
	}
}
