package com.renx.mg.request.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.renx.mg.request.model.Site;

public interface SiteRepository extends JpaRepository<Site, Long>{
	
	List<Site> findByCompanyId(Long companyId);

}
