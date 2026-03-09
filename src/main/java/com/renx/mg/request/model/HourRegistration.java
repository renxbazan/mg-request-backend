/**
 * 
 */
package com.renx.mg.request.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;


/**
 * @author renzobazan
 *
 */

@Entity
public class HourRegistration {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	Long id;
	
	private Date date;
	
	private BigDecimal hour;
	
	private Long customerId;
	
	@Transient
	private String dateString;
	
	@OneToOne
	@JoinColumn(name = "customerId", referencedColumnName = "id", unique = true, nullable = true,
	updatable = false, insertable = false)
	private Customer customer;
	
	@Transient
	private BigDecimal paymentAmount = BigDecimal.ZERO;
	
	private Long siteId;
	
	@OneToOne
	@JoinColumn(name = "siteId", referencedColumnName = "id", unique = true, nullable = true,
	updatable = false, insertable = false)
	private Site site;
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		if(date==null) {
			DateTimeFormatter formatter =
	                DateTimeFormatter.ofPattern("dd/MM/yyyy");
			ZoneId defaultZoneId = ZoneId.systemDefault();
			LocalDate dateFrom  = LocalDate.parse(dateString, formatter);
			
			date= Date.from(dateFrom.atStartOfDay(defaultZoneId).toInstant());
			
		}
			
		
		
		this.date = date;
	}

	public BigDecimal getHour() {
		return hour;
	}

	public void setHour(BigDecimal hour) {
		this.hour = hour;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public String getDateString() {
		
		if(date!=null) {
			DateTimeFormatter formatter =
	                DateTimeFormatter.ofPattern("dd/MM/yyyy");
			ZoneId defaultZoneId = ZoneId.systemDefault();
			return date.toInstant().atZone(defaultZoneId).toLocalDate().format(formatter).toString();
		}
		
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	public BigDecimal getPaymentAmount() {
		if(customer==null)
			return BigDecimal.ZERO;
		return hour.multiply(customer.getHourPrice()).setScale(2);
	}

	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

}
