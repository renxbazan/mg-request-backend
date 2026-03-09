package com.renx.mg.request.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.HourRegistration;
import com.renx.mg.request.model.HourRegistrationRequest;
import com.renx.mg.request.model.HourRegistrationSumDTO;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.HourRegistrationRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.service.HourRegistrationService;

@Controller
public class HourRegistrationController {
	

	@Autowired
	private HourRegistrationService hourRegistrationService;
	
	@Autowired
	private HourRegistrationRepository hourRegistrationRepository;
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@GetMapping("/hour-registration")
	public String index(Model model,HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		HourRegistrationRequest hourRegistrationRequest = new HourRegistrationRequest();
		
		
		model.addAttribute("customerList",customerRepository.findByEmployee(true));
		model.addAttribute("siteList",siteRepository.findAll());
		model.addAttribute("hourRegistrationRequest",hourRegistrationRequest);
		model.addAttribute("workedHourList",new ArrayList());
		model.addAttribute("hourRegistration",new HourRegistration());
		model.addAttribute("hourRegistrationDTOList",new ArrayList());
		
		
		return "hour-registration";
	}
	
	@PostMapping("/hour-registration")
	public String register(Model model,@ModelAttribute("hourRegistrationRequest") HourRegistrationRequest hourRegistrationRequest,HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		//HourRegistrationRequest hourRegistrationRequest = new HourRegistrationRequest();
		
		
		model.addAttribute("customerList",customerRepository.findByEmployee(true));
		model.addAttribute("siteList",siteRepository.findAll());
		model.addAttribute("hourRegistrationRequest",new HourRegistrationRequest());
		model.addAttribute("hourRegistration",new HourRegistration());
		
		
		List<HourRegistration> hourRegistrationList = hourRegistrationService.register(hourRegistrationRequest);
		
		hourRegistrationList.forEach(hr -> {hr.setCustomer(customerRepository.findById(hr.getCustomerId()).get());
											hr.setSite(siteRepository.findById(hr.getSiteId()).orElse(null));
		});
		model.addAttribute("workedHourList",hourRegistrationList);
		
		
		
		return "hour-registration";
	}
	
	@GetMapping("/hour-registration/{id}")
	public String index(Model model,HttpServletRequest request, @PathVariable("id")Long id) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		HourRegistration hourRegistration = hourRegistrationRepository.findById(id).get();
		
		
		model.addAttribute("customerList",customerRepository.findByEmployee(true));
		model.addAttribute("siteList",siteRepository.findAll());
		model.addAttribute("hourRegistration",hourRegistration);
		model.addAttribute("hourRegistrationRequest",new HourRegistrationRequest());
		model.addAttribute("workedHourList",new ArrayList());
		model.addAttribute("hourRegistrationDTOList",new ArrayList());
		
		
		return "hour-registration";
	}
	
	
	@PostMapping("/update-hour-registration")
	public String register(Model model,@ModelAttribute("hourRegistration") HourRegistration hourRegistration) {
		
		
		
		
		model.addAttribute("customerList",customerRepository.findByEmployee(true));
		model.addAttribute("siteList",siteRepository.findAll());
		model.addAttribute("hourRegistrationRequest",new HourRegistrationRequest());
		model.addAttribute("hourRegistration",new HourRegistration());
		
		
		if(hourRegistration.getDate()==null) {
			DateTimeFormatter formatter =
	                DateTimeFormatter.ofPattern("dd/MM/yyyy");
			ZoneId defaultZoneId = ZoneId.systemDefault();
			LocalDate dateFrom  = LocalDate.parse(hourRegistration.getDateString(), formatter);
			
			hourRegistration.setDate(Date.from(dateFrom.atStartOfDay(defaultZoneId).toInstant()));
			
		}
		
		
		
		hourRegistrationRepository.save(hourRegistration);
		
		List<HourRegistration> hourRegistrationList = new ArrayList<HourRegistration>();
		hourRegistration.setCustomer(customerRepository.findById(hourRegistration.getCustomerId()).get());
		hourRegistration.setSite(siteRepository.findById(hourRegistration.getSiteId()).orElse(null));
		hourRegistrationList.add(hourRegistration);
		model.addAttribute("workedHourList",hourRegistrationList);
		model.addAttribute("hourRegistrationDTOList",new ArrayList());
		
		
		return "hour-registration";
	}
	
	@GetMapping("/deleteHourRegistration/{id}")
	public String delete(Model model,HttpServletRequest request,@PathVariable("id")Long id) {
		
		hourRegistrationRepository.deleteById(id);
		
		
		return "redirect:/hour-registration";
	}
	

	@PostMapping("/hour-registration-search")
	public String search(Model model,@ModelAttribute("hourRegistrationRequest") HourRegistrationRequest hourRegistrationRequest,HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		//HourRegistrationRequest hourRegistrationRequest = new HourRegistrationRequest();
		
		List<Customer> employeeList = customerRepository.findByEmployee(true);
		
		model.addAttribute("customerList",employeeList);
		model.addAttribute("siteList",siteRepository.findAll());
		model.addAttribute("hourRegistrationRequest",hourRegistrationRequest);
		model.addAttribute("hourRegistration",new HourRegistration());
		
		
		List<HourRegistration> hourRegistrationList = new ArrayList<HourRegistration>();
		List<HourRegistrationSumDTO> hourRegistrationSumDTOList = new ArrayList<HourRegistrationSumDTO>();
		
		if(!hourRegistrationRequest.getCustomerId().equals(Long.valueOf(-99))) {
         hourRegistrationList = hourRegistrationService.search(hourRegistrationRequest);
		}else {
			hourRegistrationSumDTOList = 	hourRegistrationService.searchSUM(hourRegistrationRequest,employeeList);
		}

	
		
		
		
		
		//hourRegistrationList.forEach(hr -> hr.setCustomer(customerRepository.findById(hr.getCustomerId()).get()));
		model.addAttribute("workedHourList",hourRegistrationList);
		model.addAttribute("hourRegistrationDTOList",hourRegistrationSumDTOList);
		
		
		
		return "hour-registration";
	}
	
	
}
