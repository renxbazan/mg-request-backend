/**
 * 
 */
package com.renx.mg.request.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.HourRegistration;
import com.renx.mg.request.model.HourRegistrationRequest;
import com.renx.mg.request.model.HourRegistrationSumDTO;
import com.renx.mg.request.repository.HourRegistrationRepository;

/**
 * @author renzobazan
 *
 */
@Service
public class HourRegistrationService {

	@Autowired
	private HourRegistrationRepository hourRegistrationRepository;

	public List<HourRegistration> register(HourRegistrationRequest request) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		long days = 0;

		LocalDate dateFrom = LocalDate.parse(request.getFromDate(), formatter);
		LocalDate dateTo = request.getToDate() != null && request.getToDate().length() > 0
				? LocalDate.parse(request.getToDate(), formatter)
				: null;

		ZoneId defaultZoneId = ZoneId.systemDefault();

		LocalTime timeFrom = LocalTime.parse(request.getFromHour(), timeFormatter);
		LocalTime timeTo = LocalTime.parse(request.getToHour(), timeFormatter);

		Long workedHours = timeTo.getLong(ChronoField.HOUR_OF_DAY) - timeFrom.getLong(ChronoField.HOUR_OF_DAY);

		if (workedHours > 5) {
			workedHours = workedHours - 1;
		}

		List<HourRegistration> hourRegistrationList = new ArrayList<HourRegistration>();
		HourRegistration hourRegistration = new HourRegistration();
		hourRegistration.setCustomerId(request.getCustomerId());
		hourRegistration.setDate(Date.from(dateFrom.atStartOfDay(defaultZoneId).toInstant()));
		hourRegistration.setHour(new BigDecimal(workedHours));
		hourRegistrationList.add(hourRegistration);
		hourRegistration.setSiteId(request.getSiteId());

		if (dateTo != null) {

			do {
				days++;
				hourRegistration = new HourRegistration();
				hourRegistration.setCustomerId(request.getCustomerId());
				hourRegistration.setDate(Date.from(dateFrom.plusDays(days).atStartOfDay(defaultZoneId).toInstant()));
				hourRegistration.setHour(new BigDecimal(workedHours));
				hourRegistration.setSiteId(request.getSiteId());
				if (isValidDay(dateFrom.plusDays(days))) {
					hourRegistrationList.add(hourRegistration);
				}
			} while (dateFrom.plusDays(days).isBefore(dateTo));

		}

		hourRegistrationRepository.saveAll(hourRegistrationList);

		return hourRegistrationList;
	}

	private boolean isValidDay(LocalDate localDate) {

		if (localDate.getDayOfWeek().equals(DayOfWeek.SATURDAY) || localDate.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
			return false;
		}
		return true;

	}

	public List<HourRegistration> search(HourRegistrationRequest request) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		LocalDate loclDateFrom = LocalDate.parse(request.getFromDate(), formatter);
		LocalDate localDateTo = request.getToDate() != null && request.getToDate().length() > 0
				? LocalDate.parse(request.getToDate(), formatter)
				: null;
		ZoneId defaultZoneId = ZoneId.systemDefault();
		Date dateFrom = Date.from(loclDateFrom.atStartOfDay(defaultZoneId).toInstant());
		Date dateTo = Date.from(localDateTo.atStartOfDay(defaultZoneId).toInstant());

		return hourRegistrationRepository.findByCustomerIdAndDateBetweenOrderByDate(request.getCustomerId(), dateFrom,
				dateTo);
	}

	public List<HourRegistrationSumDTO> searchSUM(HourRegistrationRequest hourRegistrationRequest,
			List<Customer> employeeList) {
		// TODO Auto-generated method stub

		List<HourRegistrationSumDTO> hourRegistrationDTOList = new ArrayList<HourRegistrationSumDTO>();

		for (Customer customer : employeeList) {
			HourRegistrationRequest newHourRegistrationRequest = new HourRegistrationRequest();
			newHourRegistrationRequest.setFromDate(hourRegistrationRequest.getFromDate());
			newHourRegistrationRequest.setToDate(hourRegistrationRequest.getToDate());
			newHourRegistrationRequest.setCustomerId(customer.getId()); 
			List<HourRegistration> hourRegistrations = search(newHourRegistrationRequest);

			hourRegistrationDTOList.add(convertHourRegistrationListToRegistrationSUM(hourRegistrations));
		}

		return hourRegistrationDTOList;
	}

	private HourRegistrationSumDTO convertHourRegistrationListToRegistrationSUM(
			List<HourRegistration> hourRegistrations) {

		HourRegistrationSumDTO dto = new HourRegistrationSumDTO();
		BigDecimal totalHours = BigDecimal.ZERO;
		BigDecimal totalPayment = BigDecimal.ZERO;

		if (hourRegistrations != null && hourRegistrations.size() > 0) {
			dto.setFullName(hourRegistrations.get(0).getCustomer().getFirstName());
			for (HourRegistration hourRegistration : hourRegistrations) {
				totalHours = totalHours.add(hourRegistration.getHour());
				totalPayment = totalPayment.add(hourRegistration.getPaymentAmount());
			}
			dto.setWorkedHour(totalHours);
			dto.setPaymentAmount(totalPayment);

		}
		return dto;
	}

}
