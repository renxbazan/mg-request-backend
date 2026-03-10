package com.renx.mg.request.repository;

import com.renx.mg.request.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);

	User findByCustomerId(Long customerId);

	List<User> findByCustomer_CompanyId(Long companyId);

	List<User> findByCustomer_CompanyIdAndProfileId(Long companyId, Long profileId);
}
