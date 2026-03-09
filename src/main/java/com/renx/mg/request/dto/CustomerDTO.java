package com.renx.mg.request.dto;

import java.math.BigDecimal;

public class CustomerDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String gender;
    private String phone;
    private String email;
    private Long companyId;
    private boolean employee;
    private BigDecimal hourPrice;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public boolean isEmployee() { return employee; }
    public void setEmployee(boolean employee) { this.employee = employee; }
    public BigDecimal getHourPrice() { return hourPrice; }
    public void setHourPrice(BigDecimal hourPrice) { this.hourPrice = hourPrice; }
}
