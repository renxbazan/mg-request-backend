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

	@Autowired
	private RequestApproverService requestApproverService;


	
	@Transactional
	public Request createRequest(Request request, Long profileId) {
		request.setRequestStatus(RequestStatusType.CREATED);
		if (profileId != null && profileId.equals(Constants.REQUESTER_PROFILE_ID)) {
			request.setRequestStatus(RequestStatusType.PENDING_APPROVAL);
		}
		if (profileId != null && profileId.equals(Constants.WORKER_PROFILE_ID)) {
			boolean companyHasApprovers = companyHasApprovers(request.getSiteId());
			if (companyHasApprovers) {
				request.setRequestStatus(RequestStatusType.PENDING_APPROVAL);
			}
			// si no tiene aprobadores, se queda en CREATED
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
	 * Envía email al worker asignado (async, HTML con plantilla de marca).
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
			String companyName = site.getCompany().getName();
			String siteName = site.getName();
			String priorityLabel = EmailService.priorityToDisplayText(request.getPriority());
			String subject = "New Request for " + companyName + " - " + siteName + " (priority: " + priorityLabel + ")";
			String bodyHtml = emailService.buildNewRequestBody(companyName, siteName, request.getPriority(), request.getDescription(), requestId);
			emailService.sendHtmlMessageAsync(to, subject, "New request assigned to you", bodyHtml);
		} catch (Exception e) {
			log.error("Error sending email on assign, requestId={}, assignedUserId={}: {}", requestId, assignedUserId, e.getMessage(), e);
		}
	}

	private void sendEmailOnCreate(Request request) {
		try {
			if (request.getSiteId() == null) return;
			Site site = siteRepository.findById(request.getSiteId()).orElse(null);
			if (site == null || site.getCompany() == null) return;
			String companyName = site.getCompany().getName();
			String siteName = site.getName();
			Long requestId = request.getId();
			String priorityLabel = EmailService.priorityToDisplayText(request.getPriority());
			String bodyHtml = emailService.buildNewRequestBody(companyName, siteName, request.getPriority(), request.getDescription(), requestId);
			if (request.getRequestStatus() == RequestStatusType.CREATED) {
				String[] to = { "aacosta@mgservicesunlimited.com" };
				String subject = "New Request for " + companyName + " - " + siteName + " (priority: " + priorityLabel + ")";
				emailService.sendHtmlMessageAsync(to, subject, "New request", bodyHtml);
			} else {
				java.util.List<Long> approverIds = requestApproverService.findUserIdsWhoCanApprove(site.getCompanyId(), site.getId());
				java.util.List<String> emails = new java.util.ArrayList<>();
				for (Long uid : approverIds) {
					User u = userRepository.findById(uid).orElse(null);
					if (u != null && u.getCustomerId() != null) {
						Customer c = customerRepository.findById(u.getCustomerId()).orElse(null);
						if (c != null && c.getEmail() != null && !c.getEmail().isBlank())
							emails.add(c.getEmail());
					}
				}
				if (emails.isEmpty()) emails.add("aacosta@mgserviceunlimited.com");
				String[] to = emails.toArray(new String[0]);
				String subject = "New Request for " + companyName + " - " + siteName + " — APPROVAL PENDING";
				String bodyWithPending = bodyHtml + "<p style=\"margin-top:16px;padding:12px;background:#e8f4fc;border-left:4px solid #61a1d9;border-radius:4px;\"><strong>Approval pending.</strong></p>";
				emailService.sendHtmlMessageAsync(to, subject, "New request — approval pending", bodyWithPending);
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
			String detail = request.getDescription() != null ? request.getDescription() : "";
			Long requestId = request.getId();
			if (newStatus == RequestStatusType.CREATED) {
				String[] to = { requesterCustomer.getEmail(), "aacosta@mgservicesunlimited.com" };
				String body = emailService.buildStatusChangeBody(requesterCustomer.getFirstName(), "Your request has been <strong>approved</strong>.", detail, requestId);
				emailService.sendHtmlMessageAsync(to, "Request approved", "Request approved", body);
			} else if (newStatus == RequestStatusType.REJECTED) {
				String[] to = { requesterCustomer.getEmail(), "aacosta@mgservicesunlimited.com" };
				String body = emailService.buildStatusChangeBody(requesterCustomer.getFirstName(), "Your request has been <strong>rejected</strong>.", detail, requestId);
				emailService.sendHtmlMessageAsync(to, "Request rejected", "Request rejected", body);
			} else if (newStatus == RequestStatusType.DONE) {
				String body = emailService.buildStatusChangeBody(requesterCustomer.getFirstName(), "Your request has been <strong>completed</strong>.", detail, requestId);
				emailService.sendHtmlMessageAsync(new String[] { requesterCustomer.getEmail() }, "Request completed", "Request completed", body);
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

	/**
	 * Indica si hay al menos un aprobador asignado (en request_approver) para ese site o su company.
	 */
	private boolean companyHasApprovers(Long siteId) {
		if (siteId == null) return false;
		Long companyId = siteRepository.findById(siteId).map(Site::getCompanyId).orElse(null);
		if (companyId == null) return false;
		return requestApproverService.hasApproversFor(companyId, siteId);
	}
}
