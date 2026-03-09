package com.renx.mg.request.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.Request;
import com.renx.mg.request.model.RequestAssignment;
import com.renx.mg.request.model.RequestHistory;
import com.renx.mg.request.model.RequestStatusType;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.RequestAssignmentRepository;
import com.renx.mg.request.repository.RequestHistoryRepository;
import com.renx.mg.request.repository.RequestRepository;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.service.EmailService;

@Controller
public class RequestAssignmentController {
	
	@Autowired
	RequestAssignmentRepository requestAssignmentRepository;
	
	@Autowired
	RequestHistoryRepository requestHistoryRepository;
	
	@Autowired
	RequestRepository requestRepository;
	
	@Autowired
	CustomerRepository customerRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	EmailService mailService;
	
	
	@PostMapping("/request-assignment")
	public String save(Model model,@ModelAttribute("requestAssignment") RequestAssignment requestAssignment) {
		
		
		Request request = requestRepository.findById(requestAssignment.getRequestId()).get();
		requestAssignment.setRequest(request);
		
		
		User user = userRepository.findByCustomerId(requestAssignment.getUserId());
		
		if(user==null) {
			throw new RuntimeException("Worker must be a user");
		}
		
	    requestAssignment.setUserId(user.getId());
	    
		
		requestAssignmentRepository.save(requestAssignment);
		
		request.setRequestStatus(RequestStatusType.ASSIGNED);
	
		
		RequestHistory requestHistory = convertToRequestHistory(request,request.getUserId());
		requestHistoryRepository.save(requestHistory);
		
		
		try {
		
		String[] to = {user.getCustomer().getEmail()};
		String subject = "New Request for "+request.getSite().getCompany().getName()+ " - " +request.getSite().getName()+ "priority : "+request.getPriority();
		String mailBody = "New Request to "+request.getSite().getCompany().getName()+ " - " +request.getSite().getName()+"\n"; 
		mailBody+= "Request Detail: "+request.getDescription();
		mailService.sendMessage(to, subject, mailBody);
		}catch(Exception ex) {
			System.out.println("error al enviar mail"+ex.getMessage());
		}
		
		
		return "redirect:/admin-home";
	}
	
	
	@GetMapping("/request-assignment/{requestId}")
	public String requestAssignmentIndex(Model model,@PathVariable("requestId")Long requestId) {
		
		
		RequestAssignment requestAssigment = requestAssignmentRepository.findByRequestId(requestId);
		
			if(requestAssigment==null) {
			Request request = requestRepository.findById(requestId).get();
		
		    requestAssigment = new RequestAssignment();
			requestAssigment.setRequestId(requestId);
			requestAssigment.setRequest(request);
			}
			List<Customer> workerList = customerRepository.findByEmployee(true);
			
			model.addAttribute("workerList",workerList);
			model.addAttribute("requestAssignment",requestAssigment);
			
			
		
		
		
		return "request-assignment";
	}
	
	
	
	
private RequestHistory convertToRequestHistory(Request request, Long userId) {
		
		RequestHistory requestHistory = new RequestHistory();
		requestHistory.setRequestId(request.getId());
		requestHistory.setRequestStatus(request.getRequestStatus());
		
		
		requestHistory.setUserId(userId);
		
		return requestHistory;
		
	}

}
