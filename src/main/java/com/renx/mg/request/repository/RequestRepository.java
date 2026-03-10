package com.renx.mg.request.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.renx.mg.request.model.Request;
import com.renx.mg.request.model.RequestStatusType;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long>, JpaSpecificationExecutor<Request> {

	List<Request> findByUserId(Long userId);

	List<Request> findByPriorityAndRequestStatus(String priority, RequestStatusType requestStatus);

	List<Request> findBySiteCompanyIdAndCreateDateBetween(Long companyId, Date startDate, Date endDate);

	List<Request> findBySiteCompanyId(Long companyId);

	List<Request> findTop100ByOrderByIdDesc();

	List<Request> findByRequestStatusAndSiteCompanyId(RequestStatusType requestStatusType, Long companyId);

	List<Request> findTop100BySiteCompanyIdOrderByIdDesc(Long companyId);

	Page<Request> findAllByOrderByIdDesc(Pageable pageable);

	Page<Request> findBySiteCompanyIdOrderByIdDesc(Long companyId, Pageable pageable);
}
