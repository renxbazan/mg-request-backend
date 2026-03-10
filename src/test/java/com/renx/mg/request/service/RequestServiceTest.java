package com.renx.mg.request.service;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.model.Company;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;
    @Mock
    private RequestHistoryRepository requestHistoryRepository;
    @Mock
    private RequestAssignmentRepository requestAssignmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private com.renx.mg.request.service.EmailService emailService;
    @Mock
    private SiteRepository siteRepository;
    @Mock
    private CustomerRepository customerRepository;

    private RequestService requestService;

    @BeforeEach
    void setUp() {
        requestService = new RequestService();
        org.springframework.test.util.ReflectionTestUtils.setField(requestService, "requestRepository", requestRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(requestService, "requestHistoryRepository", requestHistoryRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(requestService, "requestAssignmentRepository", requestAssignmentRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(requestService, "userRepository", userRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(requestService, "emailService", emailService);
        org.springframework.test.util.ReflectionTestUtils.setField(requestService, "siteRepository", siteRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(requestService, "customerRepository", customerRepository);
    }

    @Test
    void createRequest_withRequesterProfile_setsPendingApproval() {
        Request request = new Request();
        request.setUserId(1L);
        request.setSiteId(1L);
        Request saved = new Request();
        saved.setId(10L);
        saved.setUserId(1L);
        saved.setSiteId(1L);
        saved.setRequestStatus(RequestStatusType.PENDING_APPROVAL);
        when(requestRepository.save(any(Request.class))).thenReturn(saved);
        Site site = new Site();
        site.setCompanyId(1L);
        Company company = new Company();
        company.setName("Test Co");
        site.setCompany(company);
        when(siteRepository.findById(anyLong())).thenReturn(Optional.of(site));

        Request result = requestService.createRequest(request, 2L);

        assertThat(result.getRequestStatus()).isEqualTo(RequestStatusType.PENDING_APPROVAL);
        ArgumentCaptor<RequestHistory> historyCaptor = ArgumentCaptor.forClass(RequestHistory.class);
        verify(requestHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getRequestStatus()).isEqualTo(RequestStatusType.PENDING_APPROVAL);
    }

    @Test
    void createRequest_withNonRequesterProfile_setsCreated() {
        Request request = new Request();
        request.setUserId(1L);
        request.setSiteId(1L);
        Request saved = new Request();
        saved.setId(10L);
        saved.setUserId(1L);
        saved.setSiteId(1L);
        saved.setRequestStatus(RequestStatusType.CREATED);
        when(requestRepository.save(any(Request.class))).thenReturn(saved);
        Site site = new Site();
        site.setCompanyId(1L);
        Company company = new Company();
        company.setName("Test Co");
        site.setCompany(company);
        when(siteRepository.findById(anyLong())).thenReturn(Optional.of(site));

        Request result = requestService.createRequest(request, 1L);

        assertThat(result.getRequestStatus()).isEqualTo(RequestStatusType.CREATED);
    }

    @Test
    void createRequest_withWorkerProfile_andCompanyHasApprovers_setsPendingApproval() {
        Request request = new Request();
        request.setUserId(1L);
        request.setSiteId(1L);
        Request saved = new Request();
        saved.setId(10L);
        saved.setUserId(1L);
        saved.setSiteId(1L);
        saved.setRequestStatus(RequestStatusType.PENDING_APPROVAL);
        when(requestRepository.save(any(Request.class))).thenReturn(saved);
        Site site = new Site();
        site.setId(1L);
        site.setCompanyId(100L);
        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(userRepository.findByCustomer_CompanyIdAndProfileId(100L, Constants.COMPANY_ADMIN_PROFILE_ID))
                .thenReturn(List.of(new User()));

        Request result = requestService.createRequest(request, Constants.WORKER_PROFILE_ID);

        assertThat(result.getRequestStatus()).isEqualTo(RequestStatusType.PENDING_APPROVAL);
        ArgumentCaptor<RequestHistory> historyCaptor = ArgumentCaptor.forClass(RequestHistory.class);
        verify(requestHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getRequestStatus()).isEqualTo(RequestStatusType.PENDING_APPROVAL);
    }

    @Test
    void createRequest_withWorkerProfile_andCompanyHasNoApprovers_setsCreated() {
        Request request = new Request();
        request.setUserId(1L);
        request.setSiteId(1L);
        Request saved = new Request();
        saved.setId(10L);
        saved.setUserId(1L);
        saved.setSiteId(1L);
        saved.setRequestStatus(RequestStatusType.CREATED);
        when(requestRepository.save(any(Request.class))).thenReturn(saved);
        Site site = new Site();
        site.setId(1L);
        site.setCompanyId(100L);
        when(siteRepository.findById(1L)).thenReturn(Optional.of(site));
        when(userRepository.findByCustomer_CompanyIdAndProfileId(100L, Constants.COMPANY_ADMIN_PROFILE_ID))
                .thenReturn(List.of());

        Request result = requestService.createRequest(request, Constants.WORKER_PROFILE_ID);

        assertThat(result.getRequestStatus()).isEqualTo(RequestStatusType.CREATED);
        ArgumentCaptor<RequestHistory> historyCaptor = ArgumentCaptor.forClass(RequestHistory.class);
        verify(requestHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getRequestStatus()).isEqualTo(RequestStatusType.CREATED);
    }

    @Test
    void createRequest_withWorkerProfile_andSiteNotFound_setsCreated() {
        Request request = new Request();
        request.setUserId(1L);
        request.setSiteId(999L);
        Request saved = new Request();
        saved.setId(10L);
        saved.setUserId(1L);
        saved.setSiteId(999L);
        saved.setRequestStatus(RequestStatusType.CREATED);
        when(requestRepository.save(any(Request.class))).thenReturn(saved);
        when(siteRepository.findById(999L)).thenReturn(Optional.empty());

        Request result = requestService.createRequest(request, Constants.WORKER_PROFILE_ID);

        assertThat(result.getRequestStatus()).isEqualTo(RequestStatusType.CREATED);
    }

    @Test
    void changeRequestStatus_updatesRequestAndSavesHistory() {
        Request request = new Request();
        request.setId(5L);
        request.setUserId(1L);
        request.setRequestStatus(RequestStatusType.CREATED);
        when(requestRepository.findById(5L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(Request.class))).thenAnswer(i -> i.getArgument(0));

        Request result = requestService.changeRequestStatus(5L, RequestStatusType.ASSIGNED, 2L, null, null);

        assertThat(result.getRequestStatus()).isEqualTo(RequestStatusType.ASSIGNED);
        ArgumentCaptor<RequestHistory> historyCaptor = ArgumentCaptor.forClass(RequestHistory.class);
        verify(requestHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getRequestStatus()).isEqualTo(RequestStatusType.ASSIGNED);
        assertThat(historyCaptor.getValue().getRequestId()).isEqualTo(5L);
    }
}
