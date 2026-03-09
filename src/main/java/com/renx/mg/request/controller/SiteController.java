package com.renx.mg.request.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.renx.mg.request.model.Site;
import com.renx.mg.request.repository.CompanyRepository;
import com.renx.mg.request.repository.SiteRepository;

@Controller
public class SiteController {
	
	@Autowired
	SiteRepository repository;
	
	@Autowired
	CompanyRepository companyRepository;
	
	@GetMapping("/site")
	public String index(Model model) {
		model.addAttribute("siteList", repository.findAll());
		model.addAttribute("companyList", companyRepository.findAll());
		model.addAttribute("site", new Site());
		
		return "site";
	}
	
	@PostMapping("/site")
	public String save(Model model,@ModelAttribute("company") Site site) {
		model.addAttribute("companyList", companyRepository.findAll());
		model.addAttribute("siteList", repository.findAll());
		model.addAttribute("company", new Site());
		
		repository.save(site);
		
		return "redirect:/site";
	}
	
	@GetMapping("/site/{id}")
	public String findOne(Model model,@PathVariable("id") Long id) {
		model.addAttribute("companyList", companyRepository.findAll());
		model.addAttribute("siteList", repository.findAll());
		Optional<Site> site = repository.findById(id);
		model.addAttribute("site",site.get() );
		
		return "site";
	}
	
	@GetMapping("/deleteSite/{id}")
	public String save(Model model,@PathVariable("id") Long id) {
		model.addAttribute("companyList", companyRepository.findAll());
		model.addAttribute("siteList", repository.findAll());
		model.addAttribute("site", new Site());
		
		repository.deleteById(id);
		
		return "redirect:/site";
	}
	
	

}
