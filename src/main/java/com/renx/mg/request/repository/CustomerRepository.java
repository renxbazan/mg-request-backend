package com.renx.mg.request.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.renx.mg.request.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	List<Customer> findByEmployee(boolean isEmployee);
	List<Customer> findByCompanyId(Long companyId);
	List<Customer> findByCompanyIdAndEmployee(Long companyId, boolean employee);

}
