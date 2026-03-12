package com.renx.mg.request.repository;

import com.renx.mg.request.model.RequestApprover;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RequestApproverRepository extends JpaRepository<RequestApprover, Long> {

    @Query("SELECT ra FROM RequestApprover ra LEFT JOIN FETCH ra.user u LEFT JOIN FETCH u.customer LEFT JOIN FETCH ra.site WHERE ra.companyId = :companyId ORDER BY ra.userId")
    List<RequestApprover> findByCompanyIdWithUserAndSite(@Param("companyId") Long companyId);

    List<RequestApprover> findByCompanyIdOrderByUserId(Long companyId);

    boolean existsByUserIdAndCompanyIdAndScopeTypeAndSiteId(Long userId, Long companyId, Integer scopeType, Long siteId);

    Optional<RequestApprover> findByUserIdAndCompanyIdAndScopeTypeAndSiteId(Long userId, Long companyId, Integer scopeType, Long siteId);

    void deleteByUserIdAndCompanyIdAndScopeTypeAndSiteId(Long userId, Long companyId, Integer scopeType, Long siteId);

    /** User IDs that can approve a request for the given company and site (company-level or site-level approvers). */
    @Query("SELECT DISTINCT ra.userId FROM RequestApprover ra WHERE ra.companyId = :companyId AND (ra.scopeType = 0 OR (ra.scopeType = 1 AND ra.siteId = :siteId))")
    List<Long> findUserIdsWhoCanApprove(@Param("companyId") Long companyId, @Param("siteId") Long siteId);

    /** Whether the company/site has at least one approver (for companyHasApprovers). */
    @Query("SELECT COUNT(ra.id) > 0 FROM RequestApprover ra WHERE ra.companyId = :companyId AND (ra.scopeType = 0 OR (ra.scopeType = 1 AND ra.siteId = :siteId))")
    boolean hasApproversFor(@Param("companyId") Long companyId, @Param("siteId") Long siteId);
}
