package com.renx.mg.request.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.renx.mg.request.model.ServiceSubCategory;
import com.renx.mg.request.repository.ServiceCategoryRepository;
import com.renx.mg.request.repository.ServiceSubCategoryRepository;

@Controller
public class ServiceSubCategoryController {
	
	@Autowired
	ServiceSubCategoryRepository repository;
	
	@Autowired
	ServiceCategoryRepository serviceCategoryRepository;
	
	@GetMapping("/service-sub-category")
	public String index(Model model) {
		model.addAttribute("serviceSubCategoryList", repository.findAll());
		model.addAttribute("serviceCategoryList", serviceCategoryRepository.findAll());
		model.addAttribute("service-sub-category", new ServiceSubCategory());
		
		return "service-sub-category";
	}
	
	@PostMapping("/service-sub-category")
	public String save(Model model,@ModelAttribute("service-sub-category") ServiceSubCategory serviceSubCategory) {
		model.addAttribute("serviceCategoryList", serviceCategoryRepository.findAll());
		model.addAttribute("serviceSubCategoryList", repository.findAll());
		model.addAttribute("service-sub-category", new ServiceSubCategory());
		
		repository.save(serviceSubCategory);
		
		return "redirect:/service-sub-category";
	}
	
	@GetMapping("/service-sub-category/{id}")
	public String findOne(Model model,@PathVariable("id") Long id) {
		model.addAttribute("serviceCategoryList", serviceCategoryRepository.findAll());
		model.addAttribute("serviceSubCategoryList", repository.findAll());
		Optional<ServiceSubCategory> serviceSubCategory = repository.findById(id);
		model.addAttribute("service-sub-category",serviceSubCategory.get() );
		
		return "service-sub-category";
	}
	
	@GetMapping("/deleteServiceSubCategory/{id}")
	public String delete(Model model,@PathVariable("id") Long id) {
		model.addAttribute("serviceCategoryList", serviceCategoryRepository.findAll());
		model.addAttribute("serviceSubCategoryList", repository.findAll());
		model.addAttribute("service-sub-category", new ServiceSubCategory());
		
		repository.deleteById(id);
		
		return "redirect:/service-sub-category";
	}
	
	@GetMapping(value = "/serviceSubCategoryByCategoryId/{serviceCategoryId}")
	  public @ResponseBody List<ServiceSubCategory> findBySearchTerm(@PathVariable("serviceCategoryId") Long serviceCategoryId) {

	    return repository.findByServiceCategoryId(serviceCategoryId);
	  }
	
	

}
