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
import com.aeiou.bigbang.domain.UserAccount;
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
    
    protected void swithCurrentOwner(UserAccount pOwner, Model model, HttpServletRequest request){
    	model.addAttribute("spaceOwner", pOwner.getName());
    	model.addAttribute("description", pOwner.getDescription());
    	checkIfCusTextNeedTobeReInit(pOwner, model, request);
    	request.getSession().setAttribute("CurrentOwnerID", pOwner.getId());
    }
    /**
     * if the language is switched, or visiting a different owner's space, then re-init the texts on customisable areas (header and footer)
     * @param pOwner
     * @param model
     * @param request
     */
	private void checkIfCusTextNeedTobeReInit(UserAccount pOwner, Model model, HttpServletRequest request){
		//make sure session has language prop first.
		HttpSession session = request.getSession();
		if(session.getAttribute("lang") == null){	//first visiting, no language set yet. (beginning of visiting)
			session.setAttribute("lang", decideLangWithDefaultValue(request, "en"));
		}
		
		Object tlang = request.getParameter("lang");			//language in request
		if(!pOwner.getId().equals(session.getAttribute("CurrentOwnerID"))){	//first visit or user is visiting different owner page.
			reInitCosText(pOwner, session, tlang);
		} else if(tlang != null){				//same owner page while user is clicking the language button
			if(!session.getAttribute("lang").equals(tlang)){	// and the new setted languagd is different from old one.
				session.setAttribute("lang", tlang);	//then set the new one into session.
				reInitCosText(pOwner, session, tlang);
			}
		}
	}
	
	/**
	 * find the language property from user request, if not matching supported language, return default resource texts.
	 * only when the local is in five supported lang, and the flag in customize table is set as "true", request.locale will be returned.
	 * @param request
	 * @return
	 */
	private String decideLangWithDefaultValue(HttpServletRequest request, String defaultLang){
		Locale locale = request.getLocale();
		String tlang = locale == null ? defaultLang : locale.getLanguage();
		if (("en".equals(tlang) && request.getSession().getAttribute("en") != "true") ||
			("fr".equals(tlang) && request.getSession().getAttribute("fr") != "true") ||
			("zh".equals(tlang) && request.getSession().getAttribute("zh") != "true") ||
			("it".equals(tlang) && request.getSession().getAttribute("it") != "true") ||
			("es".equals(tlang) && request.getSession().getAttribute("es") != "true")){
			return tlang;
		}
		return defaultLang;
	}
	
	//load all customizes into session to allow the webpage costomizable.
	//when visiting public page, the 
	private void reInitCosText(UserAccount pOwner, HttpSession session, Object pLang){

		if(session.getAttribute("user_role") == null){
			session.setAttribute("user_role", "");
		}
		if(pLang == null){
			pLang = session.getAttribute("lang");
		}
		String suffix = "_" + pLang.toString();
		
		//admin's customise need to be reload anyway.	
		UserAccount admin = UserAccount.findUserAccountByName("admin");
		List<Customize> customizesOfAdmin = reloadCustomizesToSession(admin, session);
		replaceValuesWithLang(customizesOfAdmin, suffix, session);

		//load administrator's customizations.
		UserAccount administrator = UserAccount.findUserAccountByName("administrator");
		List<Customize> customizesOfAdministrator = reloadCustomizesToSession(administrator, session);
		replaceValuesWithLang(customizesOfAdministrator, suffix, session);
		
		if(pOwner != null && !"admin".equals(pOwner.getName())){
			List<Customize> customizesOfGeneralUser = reloadCustomizesToSession(pOwner, session);
			replaceValuesWithLang(customizesOfGeneralUser, suffix, session);
		}
	}
	
	/*
	 * if it's normal user's main page, then need to reload his/her customise. in case he has his own customize.
	 * please be noticced order, make sure admin's first, then user's customise, so user's can replace admins.
	 */
	private List<Customize> reloadCustomizesToSession(UserAccount pOwner, HttpSession session){
		List<Customize> customizes = Customize.findCustomizesByOwner(pOwner);
		for (Customize customize : customizes){
			session.setAttribute(customize.getCusKey(), customize.getCusValue());
		}
		return customizes;
	}
	
	/*
	 * apply the language property, replace the value with the one under right key(with language mark).
	 * the logic, if found key end with current lang, then use it to replace the one which is used in jspx page.
	 */
	private void replaceValuesWithLang(List<Customize> customizes, String suffix, HttpSession session){
		if(customizes == null){
			return;
		}
		for (Customize customize : customizes){
			String tCusKey = customize.getCusKey();
			if(tCusKey.endsWith(suffix)){
				session.setAttribute(tCusKey.substring(0, tCusKey.length() - 3 ), customize.getCusValue());
			}
		}
	}
}