package com.renx.mg.request.service;

import com.renx.mg.request.dto.RequestApproverDTO;
import com.renx.mg.request.model.Company;
import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.RequestApprover;
import com.renx.mg.request.model.Site;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.RequestApproverRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestApproverServiceTest {

    @Mock
    private RequestApproverRepository requestApproverRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SiteRepository siteRepository;

    private RequestApproverService requestApproverService;

    @BeforeEach
    void setUp() {
        requestApproverService = new RequestApproverService(
                requestApproverRepository,
                userRepository,
                siteRepository
        );
    }

    @Test
    void listByCompany_returnsMappedDtos() {
        Long companyId = 1L;
        RequestApprover ra = new RequestApprover();
        ra.setId(10L);
        ra.setUserId(100L);
        ra.setCompanyId(companyId);
        ra.setScopeType(RequestApprover.SCOPE_COMPANY);
        ra.setSiteId(null);
        User user = new User();
        user.setUsername("admin");
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Admin");
        user.setCustomer(customer);
        ra.setUser(user);
        when(requestApproverRepository.findByCompanyIdWithUserAndSite(companyId)).thenReturn(List.of(ra));

        List<RequestApproverDTO> result = requestApproverService.listByCompany(companyId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getUserId()).isEqualTo(100L);
        assertThat(result.get(0).getScope()).isEqualTo("COMPANY");
        assertThat(result.get(0).getUserName()).isEqualTo("John Admin");
    }

    @Test
    void findUserIdsWhoCanApprove_delegatesToRepository() {
        when(requestApproverRepository.findUserIdsWhoCanApprove(1L, 2L)).thenReturn(List.of(10L, 20L));

        List<Long> result = requestApproverService.findUserIdsWhoCanApprove(1L, 2L);

        assertThat(result).containsExactly(10L, 20L);
        verify(requestApproverRepository).findUserIdsWhoCanApprove(1L, 2L);
    }

    @Test
    void hasApproversFor_delegatesToRepository() {
        when(requestApproverRepository.hasApproversFor(1L, 2L)).thenReturn(true);

        boolean result = requestApproverService.hasApproversFor(1L, 2L);

        assertThat(result).isTrue();
        verify(requestApproverRepository).hasApproversFor(1L, 2L);
    }

    @Test
    void addApprover_companyLevel_whenNotExists_savesAndReturnsDto() {
        Long companyId = 1L;
        Long userId = 100L;
        when(requestApproverRepository.existsByUserIdAndCompanyIdAndScopeTypeAndSiteId(
                userId, companyId, RequestApprover.SCOPE_COMPANY, null)).thenReturn(false);
        RequestApprover saved = new RequestApprover();
        saved.setId(1L);
        saved.setUserId(userId);
        saved.setCompanyId(companyId);
        saved.setScopeType(RequestApprover.SCOPE_COMPANY);
        saved.setSiteId(null);
        when(requestApproverRepository.save(any(RequestApprover.class))).thenReturn(saved);

        RequestApproverDTO result = requestApproverService.addApprover(companyId, userId, true, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getScope()).isEqualTo("COMPANY");
        verify(requestApproverRepository).save(any(RequestApprover.class));
    }

    @Test
    void addApprover_companyLevel_whenAlreadyExists_returnsExisting() {
        Long companyId = 1L;
        Long userId = 100L;
        when(requestApproverRepository.existsByUserIdAndCompanyIdAndScopeTypeAndSiteId(
                userId, companyId, RequestApprover.SCOPE_COMPANY, null)).thenReturn(true);
        RequestApprover existing = new RequestApprover();
        existing.setId(5L);
        existing.setUserId(userId);
        existing.setCompanyId(companyId);
        existing.setScopeType(RequestApprover.SCOPE_COMPANY);
        existing.setSiteId(null);
        when(requestApproverRepository.findByUserIdAndCompanyIdAndScopeTypeAndSiteId(
                userId, companyId, RequestApprover.SCOPE_COMPANY, null)).thenReturn(Optional.of(existing));

        RequestApproverDTO result = requestApproverService.addApprover(companyId, userId, true, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        verify(requestApproverRepository).findByUserIdAndCompanyIdAndScopeTypeAndSiteId(
                userId, companyId, RequestApprover.SCOPE_COMPANY, null);
        // save no se llama cuando ya existe
    }

    @Test
    void addApprover_siteLevel_whenSiteBelongsToCompany_savesAndReturnsDto() {
        Long companyId = 1L;
        Long userId = 100L;
        Long siteId = 2L;
        Site site = new Site();
        site.setId(siteId);
        site.setCompanyId(companyId);
        when(siteRepository.findById(siteId)).thenReturn(Optional.of(site));
        when(requestApproverRepository.existsByUserIdAndCompanyIdAndScopeTypeAndSiteId(
                userId, companyId, RequestApprover.SCOPE_SITE, siteId)).thenReturn(false);
        RequestApprover saved = new RequestApprover();
        saved.setId(2L);
        saved.setUserId(userId);
        saved.setCompanyId(companyId);
        saved.setScopeType(RequestApprover.SCOPE_SITE);
        saved.setSiteId(siteId);
        saved.setSite(site);
        site.setName("Site A");
        when(requestApproverRepository.save(any(RequestApprover.class))).thenReturn(saved);

        RequestApproverDTO result = requestApproverService.addApprover(companyId, userId, false, siteId);

        assertThat(result).isNotNull();
        assertThat(result.getScope()).isEqualTo("SITE");
        assertThat(result.getSiteId()).isEqualTo(siteId);
        assertThat(result.getSiteName()).isEqualTo("Site A");
        verify(requestApproverRepository).save(any(RequestApprover.class));
    }

    @Test
    void addApprover_siteLevel_whenSiteIdNull_throwsIllegalArgumentException() {
        assertThatThrownBy(() ->
                requestApproverService.addApprover(1L, 100L, false, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("siteId required");
    }

    @Test
    void addApprover_siteLevel_whenSiteNotInCompany_throwsIllegalArgumentException() {
        Long siteId = 2L;
        Site site = new Site();
        site.setId(siteId);
        site.setCompanyId(999L); // different company
        when(siteRepository.findById(siteId)).thenReturn(Optional.of(site));

        assertThatThrownBy(() ->
                requestApproverService.addApprover(1L, 100L, false, siteId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("site does not belong to company");
    }

    @Test
    void addApprover_siteLevel_whenSiteNotFound_throwsIllegalArgumentException() {
        when(siteRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                requestApproverService.addApprover(1L, 100L, false, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("site does not belong to company");
    }

    @Test
    void removeApprover_companyLevel_deletesWithCompanyScope() {
        Long companyId = 1L;
        Long userId = 100L;
        requestApproverService.removeApprover(companyId, userId, true, null);

        verify(requestApproverRepository).deleteByUserIdAndCompanyIdAndScopeTypeAndSiteId(
                userId, companyId, RequestApprover.SCOPE_COMPANY, null);
    }

    @Test
    void removeApprover_siteLevel_deletesWithSiteScope() {
        Long companyId = 1L;
        Long userId = 100L;
        Long siteId = 2L;
        requestApproverService.removeApprover(companyId, userId, false, siteId);

        verify(requestApproverRepository).deleteByUserIdAndCompanyIdAndScopeTypeAndSiteId(
                userId, companyId, RequestApprover.SCOPE_SITE, siteId);
    }

    @Test
    void removeApprover_whenCompanyLevelNullAndSiteIdNull_treatsAsCompanyLevel() {
        Long companyId = 1L;
        Long userId = 100L;
        requestApproverService.removeApprover(companyId, userId, null, null);

        verify(requestApproverRepository).deleteByUserIdAndCompanyIdAndScopeTypeAndSiteId(
                eq(userId), eq(companyId), eq(RequestApprover.SCOPE_COMPANY), eq(null));
    }
}
