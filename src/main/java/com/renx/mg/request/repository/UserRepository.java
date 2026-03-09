package com.renx.mg.request.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.renx.mg.request.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	
	
	public User findByUsernameAndPassword(String username,String password);
	
	public User findByCustomerId(Long customerId);
	
	public User findByUsername(String username);
	
	public List<User> findByCustomer_CompanyId(Long companyId);

	public List<User> findByCustomer_CompanyIdAndProfileId(Long companyId, Long profileId);

}
