package com.renx.mg.request.dto;

import com.renx.mg.request.model.*;

import java.util.stream.StreamSupport;

public final class DtoMapper {

    public static CompanyDTO toDto(Company e) {
        if (e == null) return null;
        CompanyDTO dto = new CompanyDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setCompanyType(e.getCompanyType());
        return dto;
    }

    public static CustomerDTO toDto(Customer e) {
        if (e == null) return null;
        CustomerDTO dto = new CustomerDTO();
        dto.setId(e.getId());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setGender(e.getGender());
        dto.setPhone(e.getPhone());
        dto.setEmail(e.getEmail());
        dto.setCompanyId(e.getCompanyId());
        dto.setEmployee(e.isEmployee());
        dto.setHourPrice(e.getHourPrice());
        return dto;
    }

    public static SiteDTO toDto(Site e) {
        if (e == null) return null;
        SiteDTO dto = new SiteDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setComments(e.getComments());
        dto.setAddress(e.getAddress());
        dto.setPhone(e.getPhone());
        dto.setCompanyId(e.getCompanyId());
        return dto;
    }

    public static RequestDTO toDto(Request e) {
        if (e == null) return null;
        RequestDTO dto = new RequestDTO();
        dto.setId(e.getId());
        dto.setServiceCategoryId(e.getServiceCategoryId());
        dto.setServiceSubCategoryId(e.getServiceSubCategoryId());
        dto.setLocation(e.getLocation());
        dto.setDescription(e.getDescription());
        dto.setSiteId(e.getSiteId());
        dto.setUserId(e.getUserId());
        dto.setRequestStatus(e.getRequestStatus());
        dto.setCreateDate(e.getCreateDate());
        dto.setPriority(e.getPriority());
        if (e.getSite() != null) {
            dto.setSiteName(e.getSite().getName());
            if (e.getSite().getCompany() != null) {
                dto.setCompanyName(e.getSite().getCompany().getName());
            }
        }
        if (e.getUser() != null) {
            var customer = e.getUser().getCustomer();
            if (customer != null && customer.getFirstName() != null && customer.getLastName() != null) {
                dto.setRequesterName(customer.getFirstName() + " " + customer.getLastName());
            } else {
                dto.setRequesterName(e.getUser().getUsername());
            }
        }
        return dto;
    }

    /** Enriches RequestDTO with assigned staff name. Call after toDto(Request). */
    public static RequestDTO withAssignedStaffName(RequestDTO dto, String assignedStaffName) {
        if (dto != null && assignedStaffName != null) {
            dto.setAssignedStaffName(assignedStaffName);
        }
        return dto;
    }

    public static UserDTO toDto(User e) {
        if (e == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(e.getId());
        dto.setUsername(e.getUsername());
        dto.setCustomerId(e.getCustomerId());
        dto.setProfileId(e.getProfileId());
        dto.setSiteId(e.getSiteId());
        dto.setLocale(e.getLocale());
        return dto;
    }

    public static ProfileDTO toDto(Profile e) {
        if (e == null) return null;
        ProfileDTO dto = new ProfileDTO();
        dto.setId(e.getId());
        dto.setDescription(e.getDescription());
        return dto;
    }

    public static ServiceCategoryDTO toDto(ServiceCategory e) {
        if (e == null) return null;
        ServiceCategoryDTO dto = new ServiceCategoryDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        return dto;
    }

    public static ServiceSubCategoryDTO toDto(ServiceSubCategory e) {
        if (e == null) return null;
        ServiceSubCategoryDTO dto = new ServiceSubCategoryDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        dto.setServiceCategoryId(e.getServiceCategoryId());
        return dto;
    }

    public static RequestAssignmentDTO toDto(RequestAssignment e) {
        if (e == null) return null;
        RequestAssignmentDTO dto = new RequestAssignmentDTO();
        dto.setId(e.getId());
        dto.setRequestId(e.getRequestId());
        dto.setUserId(e.getUserId());
        return dto;
    }
}
