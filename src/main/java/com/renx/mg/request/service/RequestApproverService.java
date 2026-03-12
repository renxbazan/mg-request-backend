package com.renx.mg.request.service;

import com.renx.mg.request.dto.RequestApproverDTO;
import com.renx.mg.request.model.RequestApprover;
import com.renx.mg.request.repository.RequestApproverRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestApproverService {

    private final RequestApproverRepository requestApproverRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    public RequestApproverService(RequestApproverRepository requestApproverRepository,
                                 UserRepository userRepository,
                                 SiteRepository siteRepository) {
        this.requestApproverRepository = requestApproverRepository;
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
    }

    public List<RequestApproverDTO> listByCompany(Long companyId) {
        return requestApproverRepository.findByCompanyIdWithUserAndSite(companyId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** User IDs that can approve a request for the given company and site. */
    public List<Long> findUserIdsWhoCanApprove(Long companyId, Long siteId) {
        return requestApproverRepository.findUserIdsWhoCanApprove(companyId, siteId);
    }

    public boolean hasApproversFor(Long companyId, Long siteId) {
        return requestApproverRepository.hasApproversFor(companyId, siteId);
    }

    @Transactional
    public RequestApproverDTO addApprover(Long companyId, Long userId, boolean companyLevel, Long siteId) {
        if (companyLevel) {
            if (requestApproverRepository.existsByUserIdAndCompanyIdAndScopeTypeAndSiteId(userId, companyId, RequestApprover.SCOPE_COMPANY, null)) {
                return requestApproverRepository.findByUserIdAndCompanyIdAndScopeTypeAndSiteId(userId, companyId, RequestApprover.SCOPE_COMPANY, null)
                        .map(this::toDto).orElse(null);
            }
            RequestApprover ra = new RequestApprover();
            ra.setUserId(userId);
            ra.setCompanyId(companyId);
            ra.setScopeType(RequestApprover.SCOPE_COMPANY);
            ra.setSiteId(null);
            return toDto(requestApproverRepository.save(ra));
        } else {
            if (siteId == null) throw new IllegalArgumentException("siteId required for site-level approver");
            if (!siteRepository.findById(siteId).filter(s -> companyId.equals(s.getCompanyId())).isPresent()) {
                throw new IllegalArgumentException("site does not belong to company");
            }
            if (requestApproverRepository.existsByUserIdAndCompanyIdAndScopeTypeAndSiteId(userId, companyId, RequestApprover.SCOPE_SITE, siteId)) {
                return requestApproverRepository.findByUserIdAndCompanyIdAndScopeTypeAndSiteId(userId, companyId, RequestApprover.SCOPE_SITE, siteId)
                        .map(this::toDto).orElse(null);
            }
            RequestApprover ra = new RequestApprover();
            ra.setUserId(userId);
            ra.setCompanyId(companyId);
            ra.setScopeType(RequestApprover.SCOPE_SITE);
            ra.setSiteId(siteId);
            return toDto(requestApproverRepository.save(ra));
        }
    }

    @Transactional
    public void removeApprover(Long companyId, Long userId, Boolean companyLevel, Long siteId) {
        if (Boolean.TRUE.equals(companyLevel) || (siteId == null && companyLevel == null)) {
            requestApproverRepository.deleteByUserIdAndCompanyIdAndScopeTypeAndSiteId(userId, companyId, RequestApprover.SCOPE_COMPANY, null);
        } else if (siteId != null) {
            requestApproverRepository.deleteByUserIdAndCompanyIdAndScopeTypeAndSiteId(userId, companyId, RequestApprover.SCOPE_SITE, siteId);
        }
    }

    private RequestApproverDTO toDto(RequestApprover ra) {
        RequestApproverDTO dto = new RequestApproverDTO();
        dto.setId(ra.getId());
        dto.setUserId(ra.getUserId());
        dto.setCompanyId(ra.getCompanyId());
        dto.setScope(ra.getScopeType() == RequestApprover.SCOPE_COMPANY ? "COMPANY" : "SITE");
        dto.setSiteId(ra.getSiteId());
        if (ra.getUser() != null) {
            String name = ra.getUser().getUsername();
            if (ra.getUser().getCustomer() != null && ra.getUser().getCustomer().getFirstName() != null) {
                name = ra.getUser().getCustomer().getFirstName() + " " + ra.getUser().getCustomer().getLastName();
            }
            dto.setUserName(name);
        }
        if (ra.getSite() != null) dto.setSiteName(ra.getSite().getName());
        return dto;
    }
}
