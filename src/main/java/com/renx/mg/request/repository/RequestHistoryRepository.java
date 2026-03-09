package com.renx.mg.request.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.renx.mg.request.model.RequestHistory;

@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistory, Long> {
	
	List<RequestHistory> findByRequestId(Long requestId);

	List<RequestHistory> findByRequestIdOrderByCreateDateDesc(Long requestId);

	Optional<RequestHistory> findFirstByRequestIdAndRatingIsNotNullOrderByCreateDateDesc(Long requestId);

}
