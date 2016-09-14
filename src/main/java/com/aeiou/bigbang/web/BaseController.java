package com.aeiou.bigbang.web;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.aeiou.bigbang.domain.Customize;
import com.aeiou.bigbang.services.secutiry.UserContextService;

@Controller
public class BaseController {			

	@Inject
    protected UserContextService userContextService;
    
    @ModelAttribute("user")
	public User getUser() {
		return userContextService.getCurrentUser();
	}
    
    @ModelAttribute("userName")
    protected String getUserName() {
		return userContextService.getCurrentUserName();
	}
    
	protected void init(Model model, HttpServletRequest request){
		//get the session out
		HttpSession session = request.getSession();
		
		//if the language is switched, then reinit the texts on common area (header and footer)
		Object tlang = request.getParameter("lang");			//language in request
		Object tLangExisting = session.getAttribute("lang");	//language existing
		if(tlang != null){				//user is clicking the language button
			if(!tlang.equals(tLangExisting)){	// and the new setted languagd is different from old one.
				session.setAttribute("lang", tlang);	//then set the new one into session.
				reinitCommonText(session, tlang);
			}
		}else if(tLangExisting == null){//not the case that luanguage is being clicked. and in session, no language set yet.
			tlang = decideDefaultLang(request);
			session.setAttribute("lang", tlang);	//set it as en.
			reinitCommonText(session, tlang);
		}
	}
	
	//find the language property from user request, if not matching the language in current session, return default resource texts.
	private String decideDefaultLang(HttpServletRequest request){
		Locale locale = request.getLocale();
		String tlang = locale == null ? "en" : locale.getLanguage();
		if (("en".equals(tlang) && request.getSession().getAttribute("en") != null) ||
			("fr".equals(tlang) && request.getSession().getAttribute("fr") != null) ||
			("zh".equals(tlang) && request.getSession().getAttribute("zh") != null) ||
			("it".equals(tlang) && request.getSession().getAttribute("it") != null) ||
			("es".equals(tlang) && request.getSession().getAttribute("es") != null)){
			return tlang;
		}
		return "en";
	}
	
	//load all customizes into session to allow the webpage costomizable.
	protected void reinitCommonText(HttpSession session, Object tlang){

		if(session.getAttribute("user_role") == null)
			session.setAttribute("user_role", "");
		
		List<Customize> customizes = Customize.findAllCustomizes();
		for (Customize customize : customizes){
			session.setAttribute(customize.getCusKey(), customize.getCusValue());
		}
	}
}