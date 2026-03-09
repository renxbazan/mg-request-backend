package com.renx.mg.request.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.model.ProfileMenuItem;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.ProfileMenuITemRepository;
import com.renx.mg.request.repository.UserRepository;

@Controller
public class LoginController {
	@Autowired
	private UserRepository repository;
	@Autowired
	private ProfileMenuITemRepository profileMenuItemRepositoy;
	
	
	@PostMapping("/login")
	public String login(Model model,@ModelAttribute("user") User user,  HttpServletRequest request) {
		
		User loggedUser = repository.findByUsernameAndPassword(user.getUsername().toLowerCase(), user.getPassword());
	      HttpSession session = request.getSession();
		  if (loggedUser != null)
		    {
		     List<ProfileMenuItem> menuItemList = profileMenuItemRepositoy.findByProfileIdOrderByMenuItemPosition(loggedUser.getProfileId());
		
		      session.setAttribute("usuarioLogueado", loggedUser);
		      session.setAttribute("menu", menuItemList);
		      if(loggedUser.getProfileId()==1)
		    	  return "redirect:/admin-home";
		      else if(loggedUser.getProfileId().equals(Constants.COMPANY_ADMIN_PROFILE_ID))
		    	  return "redirect:/company-admin-home";
		      
		      return "home";
		    }
		    else
		    {
		    
		      
		      String message = "invalid credentials";
		      model.addAttribute("message", message);
		      session.setAttribute("usuarioLogueado", null);
		      return "login";
		    }
		
		
	}
	
	
	@GetMapping("/home")
	public String home(Model model,  HttpServletRequest request) {
		return "home";
		
	}
	
  

}
