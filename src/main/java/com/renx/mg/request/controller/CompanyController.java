package com.renx.mg.request.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.renx.mg.request.model.Company;
import com.renx.mg.request.model.CompanyType;
import com.renx.mg.request.repository.CompanyRepository;

@Controller
public class CompanyController {
	
	@Autowired
	CompanyRepository repository;
	
	@GetMapping("/company")
	public String index(Model model) {
		model.addAttribute("companyList", repository.findAll());
		model.addAttribute("companyTypes", CompanyType.values());
		model.addAttribute("company", new Company());
		
		return "company";
	}
	
	@PostMapping("/company")
	public String save(Model model,@ModelAttribute("company") Company company) {
		model.addAttribute("companyList", repository.findAll());
		model.addAttribute("companyTypes", CompanyType.values());
		model.addAttribute("company", new Company());
		
		repository.save(company);
		
		return "redirect:/company";
	}
	
	@GetMapping("/company/{id}")
	public String findOne(Model model,@PathVariable("id") Long id) {
		model.addAttribute("companyList", repository.findAll());
		model.addAttribute("companyTypes", CompanyType.values());
		
		Optional<Company> company = repository.findById(id);
		model.addAttribute("company",company.get() );
		
		return "company";
	}
	
	@GetMapping("/deleteCompany/{id}")
	public String save(Model model,@PathVariable("id") Long id) {
		model.addAttribute("companyList", repository.findAll());
		model.addAttribute("companyTypes", CompanyType.values());
		model.addAttribute("company", new Company());
		
		repository.deleteById(id);
		
		return "redirect:/company";
	}
	
	

}
