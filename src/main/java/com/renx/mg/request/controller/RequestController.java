package com.renx.mg.request.controller;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.model.Request;
import com.renx.mg.request.model.RequestAssignment;
import com.renx.mg.request.model.RequestHistory;
import com.renx.mg.request.model.RequestStatusType;
import com.renx.mg.request.model.Site;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.RequestAssignmentRepository;
import com.renx.mg.request.repository.RequestHistoryRepository;
import com.renx.mg.request.repository.RequestRepository;
import com.renx.mg.request.repository.ServiceCategoryRepository;
import com.renx.mg.request.repository.ServiceSubCategoryRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.service.EmailService;
import com.renx.mg.request.service.RequestService;

@Controller
public class RequestController {
	
	@Autowired
	SiteRepository siteRepository;
	
	@Autowired
	ServiceCategoryRepository serviceCategoryRepository;
	
	@Autowired
	ServiceSubCategoryRepository serviceSubCategoryRepository;
	
	@Autowired
	RequestService service;
	
	@Autowired
	RequestAssignmentRepository requestAssignmentRepository;
	
	@Autowired
	RequestRepository requestRepository;
	
	@Autowired
	RequestHistoryRepository requestHistoryRepository;
	
	@Autowired
	EmailService mailService;
	
	@Autowired
	UserRepository userRepository;

	@GetMapping("/request")
	public String index(Model model,HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		Request mgRequest = new Request();
		
		List<Site> siteList =  siteRepository.findByCompanyId(loggedUser.getCustomer().getCompanyId());
		
		if(loggedUser.getProfileId().equals(Constants.REQUESTER_PROFILE_ID)) {
			siteList = new ArrayList<Site>();
			siteList.add(siteRepository.findById(loggedUser.getSiteId()).get());
			
		}
		
        if(loggedUser!=null) {
		model.addAttribute("siteList",siteList);
		mgRequest.setUserId(loggedUser.getId());
        }
		
		model.addAttribute("mgRequest",mgRequest);
		model.addAttribute("serviceCategoryList", serviceCategoryRepository.findAll());
		model.addAttribute("mgRequest",mgRequest);
		
		
		
		return "request";
	}
	
	@PostMapping("/request")
	public String save(Model model,@ModelAttribute("mgRequest") Request request, HttpServletRequest SessRequest) {
		Site site = siteRepository.findById(request.getSiteId()).get();
		request.setSite(site);
		
		HttpSession session = SessRequest.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		
		
		
		request = 	service.createRequest(request,loggedUser.getProfileId());
		
		 request = requestRepository.findById(request.getId()).get();
		
		if(request.getRequestStatus().equals(RequestStatusType.CREATED)) {
		
		String[] to = {"aacosta@mgservicesunlimited.com"};
		String subject = "New Request for "+request.getSite().getCompany().getName()+ " - " +request.getSite().getName()+ "priority : "+request.getPriority();
		String mailBody = "New Request to "+request.getSite().getCompany().getName()+ " - " +request.getSite().getName()+"\n"; 
		mailBody+= "Request Detail: "+request.getDescription();
		mailService.sendMessage(to, subject, mailBody);
		}else {
			
			
			String[] to = mailService.adminCompanyEmail(request.getSite().getCompanyId());
			String subject = "New Request for "+request.getSite().getCompany().getName()+ " - " +request.getSite().getName()+ "priority : "+request.getPriority() +" APPROVAL PENDING";
			String mailBody = "New Request to "+request.getSite().getCompany().getName()+ " - " +request.getSite().getName()+"\n"; 
			mailBody+= "Request Detail: "+request.getDescription();
			mailBody+= "Approval Pending";
			mailService.sendMessage(to, subject, mailBody);	
			
		}
		
		
		return "redirect:/my-request";
	}
	
	
	@GetMapping("/assigned-request")
	public String assignedRequest(Model model,HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		 if(loggedUser!=null) {
		List<RequestAssignment> requestAssignmentList = requestAssignmentRepository.findByUserId(loggedUser.getId());
		
		model.addAttribute("requestAssignmentList", requestAssignmentList);
		 }
		return "assigned-request";
	}
	
	
	@GetMapping("/request-list/{priority}")
	public String requestListByPriority(Model model,@PathVariable("priority") String priority) {
		
		List<Request> requestList = requestRepository.findByPriorityAndRequestStatus(priority,RequestStatusType.CREATED);
		
		model.addAttribute("requestList", requestList);
		
		return "request-list";
	}
	
	@GetMapping("/request-list")
	public String requestList(Model model,HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		List<Request> requestList = null;
		if(loggedUser.getProfileId().equals(Constants.SUPER_ADMIN_PROFILE_ID)){
		requestList = requestRepository.findTop100ByOrderByIdDesc();
		}
		
		
		if(loggedUser.getProfileId().equals(Constants.COMPANY_ADMIN_PROFILE_ID)){
			 requestList = requestRepository.findTop100BySiteCompanyIdOrderByIdDesc(loggedUser.getCustomer().getCompanyId());
		}
		model.addAttribute("requestList", requestList);
		
		return "request-list";
	}
	
	@GetMapping("/assigned-request-detail/{requestId}")
	public String assignedRequest(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		
		Request mgRequest = requestRepository.findById(requestId).get();
		mgRequest.setUserId(loggedUser.getId());
		model.addAttribute("mgRequest",mgRequest);
		model.addAttribute("siteList", siteRepository.findAll());
		
		model.addAttribute("serviceCategoryList", serviceCategoryRepository.findAll());
		model.addAttribute("serviceSubCategoryList", serviceSubCategoryRepository.findAll());
		model.addAttribute("mgRequest",mgRequest);
		
		
		return "assigned-request-detail";
	}
	
	@PutMapping("/attend-assigned-request/{requestId}")
	public @ResponseBody Request attendAssignedRequest(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		
	
		return service.changeRequestStatus(requestId, RequestStatusType.IN_TRANSIT,loggedUser.getId(),null,null);
	}
	
	@GetMapping("/close-assigned-request/{requestId}")
	public String closeAssignedRequestPage(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		
		
		
		Request mgRequest = requestRepository.findById(requestId).get();
		
		model.addAttribute("mgRequest", mgRequest);
		
	
		return "close-assigned-request";
	}
	
	@PutMapping("/close-assigned-request/{requestId}")
	public @ResponseBody Request closeAssignedRequest(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		
		String comment = request.getParameter("comment");
		Request objRequest = requestRepository.findById(requestId).get();
		
		User requester = userRepository.findById(objRequest.getUserId()).get();
		
		
	
		try {
			
			String[] to = {requester.getCustomer().getEmail()};
			String subject = "Request Completion";
			String mailBody = "Dear "+requester.getCustomer().getFirstName() +",\n"; 
			mailBody+= "Request Detail: "+objRequest.getDescription() +",\n";
			mailBody+= "Has been completed!";
			mailService.sendMessage(to, subject, mailBody);
			}catch(Exception ex) {
				System.out.println("error al enviar mail"+ex.getMessage());
			}
		
		return service.changeRequestStatus(requestId, RequestStatusType.DONE, loggedUser.getId(), comment,null);
		
	}
	
	@GetMapping("/pendingRequest/{requestId}")
	public String pendingRequest(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		

		
		Request mgRequest = requestRepository.findById(requestId).get();
		
		model.addAttribute("mgRequest", mgRequest);
		
	
		return "close-assigned-request";
	}
	
	@PutMapping("/approve-request/{requestId}")
	public @ResponseBody Request approveRequest(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		
		
		Request objRequest = requestRepository.findById(requestId).get();
		
		User requester = userRepository.findById(objRequest.getUserId()).get();
		
		
	
		try {
			
			String[] to = {requester.getCustomer().getEmail(),"aacosta@mgservicesunlimited.com"};
			String subject = "Request Approved";
			String mailBody = "Dear "+requester.getCustomer().getFirstName() +",\n"; 
			mailBody+= "Request Detail: "+objRequest.getDescription() +",\n";
			mailBody+= "Has been approved!";
			mailService.sendMessage(to, subject, mailBody);
			}catch(Exception ex) {
				System.out.println("error al enviar mail"+ex.getMessage());
			}
		
		return service.changeRequestStatus(requestId, RequestStatusType.CREATED, loggedUser.getId(), null,null);
		
	}
	
	
	@PutMapping("/reject-request/{requestId}")
	public @ResponseBody Request rejectRequest(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		
		
		Request objRequest = requestRepository.findById(requestId).get();
		
		User requester = userRepository.findById(objRequest.getUserId()).get();
		
		
	
		try {
			
			String[] to = {requester.getCustomer().getEmail(),"aacosta@mgservicesunlimited.com"};
			String subject = "Request rejected";
			String mailBody = "Dear "+requester.getCustomer().getFirstName() +",\n"; 
			mailBody+= "Request Detail: "+objRequest.getDescription() +",\n";
			mailBody+= "Has been Rejected!";
			mailService.sendMessage(to, subject, mailBody);
			}catch(Exception ex) {
				System.out.println("error al enviar mail"+ex.getMessage());
			}
		
		return service.changeRequestStatus(requestId, RequestStatusType.REJECTED, loggedUser.getId(), null,null);
		
	}
	
	@GetMapping("/my-request")
	public String myRequestPage(Model model,HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		 if(loggedUser!=null) {
		List<Request> requestList = requestRepository.findByUserId(loggedUser.getId());
		model.addAttribute("requestList", requestList);
		 }
		
		return "my-request";
	}
	
	@GetMapping("/rate-request/{requestId}")
	public String rateRequestPage(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		
		
		
		Request mgRequest = requestRepository.findById(requestId).get();
		
		model.addAttribute("mgRequest", mgRequest);
		
	
		return "rate-request";
	}
	
	@PutMapping("/rate-request/{requestId}")
	public @ResponseBody Request rateRequest(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		
		String comment = request.getParameter("comment");
		
		Long rating = Long.valueOf(request.getParameter("rating"));
		
	
		return service.changeRequestStatus(requestId, RequestStatusType.RATED, loggedUser.getId(), comment,rating);
	}
	
	@GetMapping("/request-detail/{requestId}")
	public String requestDetail(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		
	
		
		Request mgRequest = requestRepository.findById(requestId).get();
		List<RequestHistory> requestHistoryList = requestHistoryRepository.findByRequestId(requestId);
		
		model.addAttribute("mgRequest", mgRequest);
		model.addAttribute("requestHistoryList", requestHistoryList);
		
	
		return "request-detail";
	}
	
	
	
	@GetMapping("/admin-home")
	public String adminHome(Model model,HttpServletRequest request) {
		
		List<Request> lowRequestList = requestRepository.findByPriorityAndRequestStatus("L",RequestStatusType.CREATED);
		List<Request> mediumRequestList = requestRepository.findByPriorityAndRequestStatus("M",RequestStatusType.CREATED);
		List<Request> highRequestList = requestRepository.findByPriorityAndRequestStatus("H",RequestStatusType.CREATED);
		
		model.addAttribute("lowRequestSize",lowRequestList.size());
		model.addAttribute("mediumRequestSize",mediumRequestList.size());
		model.addAttribute("highRequestSize",highRequestList.size());
		
		
		return "home_admin";
	}
	
	@GetMapping("/company-admin-home")
	public String companyAdminHome(Model model,HttpServletRequest request) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");

		
		List<Request> requestList =  requestRepository.findByRequestStatusAndSiteCompanyId(RequestStatusType.PENDING_APPROVAL, loggedUser.getCustomer().getCompanyId());
		
		model.addAttribute("requestList",requestList);

		
		
		return "approval-pending-request";
	}
	
	@GetMapping("/approval-pending-request-detail/{requestId}")
	public String approvalPendingRequestDetail(Model model,HttpServletRequest request,@PathVariable("requestId") Long requestId ) {
		
		HttpSession session = request.getSession();
		User loggedUser = (User) session.getAttribute("usuarioLogueado");
		
		Request mgRequest = requestRepository.findById(requestId).get();
		mgRequest.setUserId(loggedUser.getId());
		model.addAttribute("mgRequest",mgRequest);
		model.addAttribute("siteList", siteRepository.findByCompanyId(loggedUser.getCustomer().getCompanyId()));
		
		model.addAttribute("serviceCategoryList", serviceCategoryRepository.findAll());
		model.addAttribute("serviceSubCategoryList", serviceSubCategoryRepository.findAll());
		model.addAttribute("mgRequest",mgRequest);
		
		
		return "approval-pending-request-detail";
	}
	
}
