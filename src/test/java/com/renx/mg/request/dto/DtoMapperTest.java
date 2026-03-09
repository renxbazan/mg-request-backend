package com.renx.mg.request.dto;

import com.renx.mg.request.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class DtoMapperTest {

    @Test
    void toDto_Company_null_returnsNull() {
        assertThat(DtoMapper.toDto((Company) null)).isNull();
    }

    @Test
    void toDto_Company_entity_returnsDto() {
        Company e = new Company();
        e.setId(1L);
        e.setName("Acme");
        e.setDescription("Desc");
        e.setCompanyType(CompanyType.COMPANY);
        CompanyDTO dto = DtoMapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Acme");
        assertThat(dto.getDescription()).isEqualTo("Desc");
        assertThat(dto.getCompanyType()).isEqualTo(CompanyType.COMPANY);
    }

    @Test
    void toDto_Customer_null_returnsNull() {
        assertThat(DtoMapper.toDto((Customer) null)).isNull();
    }

    @Test
    void toDto_Customer_entity_returnsDto() {
        Customer e = new Customer();
        e.setId(2L);
        e.setFirstName("John");
        e.setLastName("Doe");
        e.setEmail("j@x.com");
        e.setCompanyId(1L);
        e.setEmployee(true);
        e.setHourPrice(BigDecimal.TEN);
        CustomerDTO dto = DtoMapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getEmail()).isEqualTo("j@x.com");
        assertThat(dto.getCompanyId()).isEqualTo(1L);
        assertThat(dto.isEmployee()).isTrue();
    }

    @Test
    void toDto_Site_null_returnsNull() {
        assertThat(DtoMapper.toDto((Site) null)).isNull();
    }

    @Test
    void toDto_Site_entity_returnsDto() {
        Site e = new Site();
        e.setId(3L);
        e.setName("Main");
        e.setDescription("Desc");
        e.setCompanyId(1L);
        SiteDTO dto = DtoMapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getName()).isEqualTo("Main");
        assertThat(dto.getCompanyId()).isEqualTo(1L);
    }

    @Test
    void toDto_Request_null_returnsNull() {
        assertThat(DtoMapper.toDto((Request) null)).isNull();
    }

    @Test
    void toDto_Request_entity_returnsDto() {
        Request e = new Request();
        e.setId(4L);
        e.setDescription("Help");
        e.setRequestStatus(RequestStatusType.CREATED);
        e.setSiteId(1L);
        e.setUserId(2L);
        e.setCreateDate(new Date());
        e.setPriority("H");
        RequestDTO dto = DtoMapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(4L);
        assertThat(dto.getDescription()).isEqualTo("Help");
        assertThat(dto.getRequestStatus()).isEqualTo(RequestStatusType.CREATED);
        assertThat(dto.getPriority()).isEqualTo("H");
    }

    @Test
    void toDto_User_null_returnsNull() {
        assertThat(DtoMapper.toDto((User) null)).isNull();
    }

    @Test
    void toDto_User_entity_returnsDto() {
        User e = new User();
        e.setId(5L);
        e.setUsername("admin");
        e.setCustomerId(1L);
        e.setProfileId(2L);
        e.setSiteId(3L);
        e.setLocale("en");
        UserDTO dto = DtoMapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getUsername()).isEqualTo("admin");
        assertThat(dto.getCustomerId()).isEqualTo(1L);
        assertThat(dto.getProfileId()).isEqualTo(2L);
        assertThat(dto.getSiteId()).isEqualTo(3L);
        assertThat(dto.getLocale()).isEqualTo("en");
    }

    @Test
    void toDto_User_entity_withoutLocaleSet_returnsDefaultEs() {
        User e = new User();
        e.setId(6L);
        e.setUsername("other");
        e.setProfileId(1L);
        UserDTO dto = DtoMapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getLocale()).isEqualTo("es");
    }

    @Test
    void toDto_Profile_null_returnsNull() {
        assertThat(DtoMapper.toDto((Profile) null)).isNull();
    }

    @Test
    void toDto_Profile_entity_returnsDto() {
        Profile e = new Profile();
        e.setId(1L);
        e.setDescription("Admin");
        ProfileDTO dto = DtoMapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Admin");
    }

    @Test
    void toDto_ServiceCategory_null_returnsNull() {
        assertThat(DtoMapper.toDto((ServiceCategory) null)).isNull();
    }

    @Test
    void toDto_ServiceCategory_entity_returnsDto() {
        ServiceCategory e = new ServiceCategory();
        e.setId(1L);
        e.setName("Cat");
        e.setDescription("Cat desc");
        ServiceCategoryDTO dto = DtoMapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Cat");
    }

    @Test
    void toDto_ServiceSubCategory_null_returnsNull() {
        assertThat(DtoMapper.toDto((ServiceSubCategory) null)).isNull();
    }

    @Test
    void toDto_ServiceSubCategory_entity_returnsDto() {
        ServiceSubCategory e = new ServiceSubCategory();
        e.setId(1L);
        e.setName("Sub");
        e.setServiceCategoryId(2L);
        ServiceSubCategoryDTO dto = DtoMapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getServiceCategoryId()).isEqualTo(2L);
    }

    @Test
    void toDto_RequestAssignment_null_returnsNull() {
        assertThat(DtoMapper.toDto((RequestAssignment) null)).isNull();
    }

    @Test
    void toDto_RequestAssignment_entity_returnsDto() {
        RequestAssignment e = new RequestAssignment();
        e.setId(1L);
        e.setRequestId(10L);
        e.setUserId(20L);
        RequestAssignmentDTO dto = DtoMapper.toDto(e);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getRequestId()).isEqualTo(10L);
        assertThat(dto.getUserId()).isEqualTo(20L);
    }

    @Test
    void withAssignedStaffName_nullDto_returnsNull() {
        RequestDTO result = DtoMapper.withAssignedStaffName(null, "John Doe");
        assertThat(result).isNull();
    }

    @Test
    void withAssignedStaffName_nullName_doesNotSet() {
        RequestDTO dto = new RequestDTO();
        dto.setId(1L);
        DtoMapper.withAssignedStaffName(dto, null);
        assertThat(dto.getAssignedStaffName()).isNull();
    }

    @Test
    void withAssignedStaffName_validName_setsAssignedStaffName() {
        RequestDTO dto = new RequestDTO();
        dto.setId(1L);
        DtoMapper.withAssignedStaffName(dto, "Maria García");
        assertThat(dto.getAssignedStaffName()).isEqualTo("Maria García");
    }
}
