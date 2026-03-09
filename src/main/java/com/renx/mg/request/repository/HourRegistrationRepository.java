/**
 * 
 */
package com.renx.mg.request.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.renx.mg.request.model.HourRegistration;

/**
 * @author renzobazan
 *
 */
public interface HourRegistrationRepository extends JpaRepository<HourRegistration, Long> {

	List<HourRegistration> findByCustomerIdAndDateBetweenOrderByDate(Long customerId, Date dateFrom, Date dateTo);

}
