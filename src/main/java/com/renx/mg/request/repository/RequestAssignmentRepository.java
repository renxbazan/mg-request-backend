package com.renx.mg.request.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.renx.mg.request.model.RequestAssignment;

@Repository
public interface RequestAssignmentRepository extends JpaRepository<RequestAssignment, Long> {
	
	List<RequestAssignment> findByUserId(Long userId);

	RequestAssignment findByRequestId(Long requestId);

	List<RequestAssignment> findByRequestIdIn(Collection<Long> requestIds);

}
