package com.renx.mg.request.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.renx.mg.request.model.User;

@Controller
public class HelloController {

  @RequestMapping("/")
  public String index(Model model) {
    model.addAttribute("user", new User());
    return "login";
  }

  @PostMapping("/hello")
  public String sayHello(@RequestParam("name") String name, Model model) {
    model.addAttribute("name", name);
    return "hello";
  }
  
  @RequestMapping("/logout")
  public String logout(Model model, HttpServletRequest request) {
	HttpSession session = request.getSession();
	session.removeAttribute("usuarioLogueado");
    model.addAttribute("user", new User());
    return "login";
  }
}
