/**
 * 
 */
package com.renx.mg.request.model;

import java.math.BigDecimal;

/**
 * @author renzobazan
 *
 */
public class HourRegistrationSumDTO {
	
	
	private String fullName;
	private BigDecimal workedHour;
	private BigDecimal paymentAmount;
	
	
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public BigDecimal getWorkedHour() {
		return workedHour;
	}
	public void setWorkedHour(BigDecimal workedHour) {
		this.workedHour = workedHour;
	}
	public BigDecimal getPaymentAmount() {
		return paymentAmount;
	}
	public void setPaymentAmount(BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
	}
	
	

}
