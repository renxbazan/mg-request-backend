package com.renx.mg.request.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CompanyRepository;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.ProfileRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;

@Controller
public class CustomerController {
	
	@Autowired
	CustomerRepository repository;
	
	@Autowired
	CompanyRepository companyRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	SiteRepository siteRepository;
	
	@Autowired
	ProfileRepository profileRepository;
	
	@GetMapping("/customer")
	public String index(Model model) {
		model.addAttribute("customerList", repository.findAll());
		model.addAttribute("customer", new Customer());
		model.addAttribute("companyList", companyRepository.findAll());
		
		return "customer";
	}
	
	@PostMapping("/customer")
	public String save(Model model,@ModelAttribute("customer") Customer customer) {
		model.addAttribute("customerList", repository.findAll());
		model.addAttribute("customer", new Customer());
		model.addAttribute("companyList", companyRepository.findAll());
		
		repository.save(customer);
		
		return "redirect:/customer";
	}
	
	@GetMapping("/customer/{id}")
	public String findOne(Model model,@PathVariable("id") Long id) {
		model.addAttribute("customerList", repository.findAll());
		Optional<Customer> customer = repository.findById(id);
		model.addAttribute("customer",customer.get() );
		model.addAttribute("companyList", companyRepository.findAll());
		
		return "customer";
	}
	
	@GetMapping("/deleteCustomer/{id}")
	public String save(Model model,@PathVariable("id") Long id) {
		model.addAttribute("customerList", repository.findAll());
		model.addAttribute("customer", new Customer());
		model.addAttribute("companyList", companyRepository.findAll());
		repository.deleteById(id);
		
		return "redirect:/customer";
	}
	
	@GetMapping("/createUser/{customerId}")
	public String createUserPage(Model model,@PathVariable("customerId") Long customerId) {
		Customer customer = repository.findById(customerId).get();
		User user = userRepository.findByCustomerId(customerId);
		if(user==null) {
			user = new User();
			user.setCustomerId(customerId);
		}
		
		model.addAttribute("user",user);
		model.addAttribute("siteList",siteRepository.findByCompanyId(customer.getCompanyId()));
		model.addAttribute("profileList",profileRepository.findAll());
		model.addAttribute("customer",customer);
		return "user";
	}
	
	@PostMapping("/user")
	public String createUser(Model model,@ModelAttribute("user") User user) {
		userRepository.save(user);
		
		return "redirect:/customer";
	}
	
	

}
