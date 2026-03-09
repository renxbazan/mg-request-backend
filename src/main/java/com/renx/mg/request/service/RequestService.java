package com.renx.mg.request.service;

import java.util.Date;

import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.Request;
import com.renx.mg.request.model.RequestHistory;
import com.renx.mg.request.model.RequestStatusType;
import com.renx.mg.request.model.Site;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.RequestAssignmentRepository;
import com.renx.mg.request.repository.RequestHistoryRepository;
import com.renx.mg.request.repository.RequestRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;

@Service
public class RequestService {

	private static final Logger log = LoggerFactory.getLogger(RequestService.class);

	@Autowired
	private RequestRepository requestRepository;

	@Autowired
	private RequestHistoryRepository requestHistoryRepository;

	@Autowired
	private RequestAssignmentRepository requestAssignmentRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private CustomerRepository customerRepository;


	
	@Transactional
	public Request createRequest(Request request, Long profileId) {
		request.setRequestStatus(RequestStatusType.CREATED);
		if (profileId != null && profileId.equals(Constants.REQUESTER_PROFILE_ID)) {
			request.setRequestStatus(RequestStatusType.PENDING_APPROVAL);
		}
		request.setCreateDate(new Date());
		request = requestRepository.save(request);
		RequestHistory requestHistory = convertToRequestHistory(request, request.getUserId());
		requestHistoryRepository.save(requestHistory);
		sendEmailOnCreate(request);
		return request;
	}
	
	public Request updateRequest(Request request) {
		
		
		return requestRepository.save(request);
	}
	
	
	@Transactional
	public Request changeRequestStatus(Long requestId, RequestStatusType requestStatustype, Long userId, String comment, Long rating) {
		Request request = requestRepository.findById(requestId).get();
		request.setRequestStatus(requestStatustype);
		requestRepository.save(request);
		RequestHistory requestHistory = convertToRequestHistory(request, userId);
		requestHistory.setComments(comment);
		requestHistory.setRating(rating);
		requestHistoryRepository.save(requestHistory);
		sendEmailOnStatusChange(request, requestStatustype);
		return request;
	}

	/**
	 * Envía email al worker asignado (igual que en la versión antigua).
	 */
	public void sendEmailOnAssign(Long requestId, Long assignedUserId) {
		try {
			Request request = requestRepository.findById(requestId).orElse(null);
			User assignedUser = userRepository.findById(assignedUserId).orElse(null);
			if (request == null || assignedUser == null) return;
			Site site = siteRepository.findById(request.getSiteId()).orElse(null);
			if (site == null || site.getCompany() == null) return;
			Customer customer = assignedUser.getCustomerId() != null ? customerRepository.findById(assignedUser.getCustomerId()).orElse(null) : null;
			if (customer == null || customer.getEmail() == null) return;
			String[] to = { customer.getEmail() };
			String subject = "New Request for " + site.getCompany().getName() + " - " + site.getName() + " priority : " + request.getPriority();
			String mailBody = "New Request to " + site.getCompany().getName() + " - " + site.getName() + "\nRequest Detail: " + request.getDescription();
			emailService.sendMessage(to, subject, mailBody);
		} catch (Exception e) {
			log.error("Error sending email on assign, requestId={}, assignedUserId={}: {}", requestId, assignedUserId, e.getMessage(), e);
		}
	}

	private void sendEmailOnCreate(Request request) {
		try {
			Site site = siteRepository.findById(request.getSiteId()).orElse(null);
			if (site == null || site.getCompany() == null) return;
			String companyName = site.getCompany().getName();
			String siteName = site.getName();
			String subject = "New Request for " + companyName + " - " + siteName + " priority : " + request.getPriority();
			String mailBody = "New Request to " + companyName + " - " + siteName + "\nRequest Detail: " + request.getDescription();
			if (request.getRequestStatus() == RequestStatusType.CREATED) {
				String[] to = { "aacosta@mgservicesunlimited.com" };
				emailService.sendMessage(to, subject, mailBody);
			} else {
				String[] to = emailService.adminCompanyEmail(site.getCompanyId());
				subject += " APPROVAL PENDING";
				mailBody += "\nApproval Pending";
				emailService.sendMessage(to, subject, mailBody);
			}
		} catch (Exception e) {
			log.error("Error sending email on create, requestId={}: {}", request.getId(), e.getMessage(), e);
		}
	}

	private void sendEmailOnStatusChange(Request request, RequestStatusType newStatus) {
		try {
			User requester = request.getUserId() != null ? userRepository.findById(request.getUserId()).orElse(null) : null;
			if (requester == null || requester.getCustomerId() == null) return;
			Customer requesterCustomer = customerRepository.findById(requester.getCustomerId()).orElse(null);
			if (requesterCustomer == null || requesterCustomer.getEmail() == null) return;
			String[] to = { requesterCustomer.getEmail(), "aacosta@mgservicesunlimited.com" };
			String detail = "Request Detail: " + request.getDescription() + ",\n";
			if (newStatus == RequestStatusType.CREATED) {
				emailService.sendMessage(to, "Request Approved", "Dear " + requesterCustomer.getFirstName() + ",\n" + detail + "Has been approved!");
			} else if (newStatus == RequestStatusType.REJECTED) {
				emailService.sendMessage(to, "Request rejected", "Dear " + requesterCustomer.getFirstName() + ",\n" + detail + "Has been Rejected!");
			} else if (newStatus == RequestStatusType.DONE) {
				emailService.sendMessage(new String[] { requesterCustomer.getEmail() }, "Request Completion", "Dear " + requesterCustomer.getFirstName() + ",\n" + detail + "Has been completed!");
			}
		} catch (Exception e) {
			log.error("Error sending email on status change, requestId={}, newStatus={}: {}", request.getId(), newStatus, e.getMessage(), e);
		}
	}

	private RequestHistory convertToRequestHistory(Request request, Long userId) {
		
		RequestHistory requestHistory = new RequestHistory();
		requestHistory.setRequestId(request.getId());
		requestHistory.setRequestStatus(request.getRequestStatus());
		requestHistory.setUserId(userId);
		requestHistory.setCreateDate(new Date());
		
		return requestHistory;
		
	}
	
	
}
