/**
 * 
 */
package com.renx.mg.request.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.renx.mg.request.model.ServiceSubCategory;

/**
 * @author renx
 *
 */
public interface ServiceSubCategoryRepository extends JpaRepository<ServiceSubCategory, Long> {
	
	List<ServiceSubCategory> findByServiceCategoryId(Long serviceCategoryId);

}
