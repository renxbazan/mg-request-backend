/**
 * 
 */
package com.renx.mg.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.renx.mg.request.model.ServiceCategory;

/**
 * @author renx
 *
 */
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {

}
