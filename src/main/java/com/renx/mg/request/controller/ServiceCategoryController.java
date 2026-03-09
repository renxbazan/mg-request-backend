package com.renx.mg.request.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.renx.mg.request.model.ServiceCategory;
import com.renx.mg.request.repository.ServiceCategoryRepository;

@Controller
public class ServiceCategoryController {
	
	@Autowired
	ServiceCategoryRepository repository;
	
	@GetMapping("/service-category")
	public String index(Model model) {
		model.addAttribute("serviceCategoryList", repository.findAll());
		model.addAttribute("service-category", new ServiceCategory());
		
		return "service-category";
	}
	
	@PostMapping("/service-category")
	public String save(Model model,@ModelAttribute("service-category") ServiceCategory serviceCategory) {
		model.addAttribute("serviceCategoryList", repository.findAll());
		model.addAttribute("service-category", new ServiceCategory());
		
		repository.save(serviceCategory);
		
		return "redirect:/service-category";
	}
	
	@GetMapping("/service-category/{id}")
	public String findOne(Model model,@PathVariable("id") Long id) {
		model.addAttribute("serviceCategoryList", repository.findAll());
		Optional<ServiceCategory> serviceCategory = repository.findById(id);
		model.addAttribute("service-category",serviceCategory.get() );
		model.addAttribute("subCategoryList",serviceCategory.get().getSubCategoryList());
		
		return "service-category";
	}
	
	@GetMapping("/deleteServiceCategory/{id}")
	public String save(Model model,@PathVariable("id") Long id) {
		model.addAttribute("serviceCategoryList", repository.findAll());
		model.addAttribute("service-category", new ServiceCategory());
		repository.deleteById(id);
		
		return "redirect:/service-category";
	}
	
	

}
