package com.renx.mg.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.renx.mg.request.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

}
