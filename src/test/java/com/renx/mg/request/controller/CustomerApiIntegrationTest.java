package com.renx.mg.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renx.mg.request.common.Constants;
import com.renx.mg.request.dto.LoginRequest;
import com.renx.mg.request.model.Company;
import com.renx.mg.request.model.CompanyType;
import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.Profile;
import com.renx.mg.request.model.Site;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CompanyRepository;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.ProfileRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CustomerApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private SiteRepository siteRepository;

    private Company companyA;
    private Company companyB;
    private Customer customerInA;
    private Customer customerInB;
    private User superAdminUser;
    private User companyAdminUser;

    @BeforeEach
    void setUp() {
        companyA = new Company();
        companyA.setName("Company A");
        companyA.setCompanyType(CompanyType.COMPANY);
        companyA = companyRepository.save(companyA);

        companyB = new Company();
        companyB.setName("Company B");
        companyB.setCompanyType(CompanyType.COMPANY);
        companyB = companyRepository.save(companyB);

        customerInA = new Customer();
        customerInA.setFirstName("Person");
        customerInA.setLastName("InA");
        customerInA.setCompanyId(companyA.getId());
        customerInA.setEmployee(false);
        customerInA = customerRepository.save(customerInA);

        customerInB = new Customer();
        customerInB.setFirstName("Person");
        customerInB.setLastName("InB");
        customerInB.setCompanyId(companyB.getId());
        customerInB.setEmployee(false);
        customerInB = customerRepository.save(customerInB);

        Profile superAdminProfile = profileRepository.findById(Constants.SUPER_ADMIN_PROFILE_ID).orElseThrow();
        Profile companyAdminProfile = profileRepository.findById(Constants.COMPANY_ADMIN_PROFILE_ID).orElseThrow();

        Site siteA = new Site();
        siteA.setName("Site A");
        siteA.setCompanyId(companyA.getId());
        siteA = siteRepository.save(siteA);

        superAdminUser = new User();
        superAdminUser.setUsername("superadmin");
        superAdminUser.setPassword(passwordEncoder.encode("secret"));
        superAdminUser.setProfileId(superAdminProfile.getId());
        superAdminUser.setCustomerId(customerInA.getId());
        superAdminUser.setSiteId(siteA.getId());
        superAdminUser = userRepository.save(superAdminUser);

        companyAdminUser = new User();
        companyAdminUser.setUsername("companyadmin");
        companyAdminUser.setPassword(passwordEncoder.encode("secret"));
        companyAdminUser.setProfileId(companyAdminProfile.getId());
        companyAdminUser.setCustomerId(customerInA.getId());
        companyAdminUser.setSiteId(siteA.getId());
        companyAdminUser = userRepository.save(companyAdminUser);
    }

    private String tokenFor(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        String res = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(res).get("token").asText();
    }

    @Test
    void list_asCompanyAdmin_returnsOnlyCustomersFromOwnCompany() throws Exception {
        String token = tokenFor("companyadmin", "secret");
        mockMvc.perform(get("/api/customers").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].companyId").value(companyA.getId().intValue()));
        mockMvc.perform(get("/api/customers?companyId=" + companyB.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].companyId").value(companyA.getId().intValue()));
    }

    @Test
    void create_asCompanyAdmin_withOwnCompany_returns201() throws Exception {
        String token = tokenFor("companyadmin", "secret");
        Customer newCustomer = new Customer();
        newCustomer.setFirstName("New");
        newCustomer.setLastName("Person");
        newCustomer.setCompanyId(companyA.getId());
        newCustomer.setEmployee(false);
        mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(companyA.getId().intValue()));
    }

    @Test
    void create_asCompanyAdmin_withOtherCompany_returns403() throws Exception {
        String token = tokenFor("companyadmin", "secret");
        Customer newCustomer = new Customer();
        newCustomer.setFirstName("New");
        newCustomer.setLastName("Person");
        newCustomer.setCompanyId(companyB.getId());
        newCustomer.setEmployee(false);
        mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_asCompanyAdmin_targetInOwnCompany_returns200() throws Exception {
        String token = tokenFor("companyadmin", "secret");
        Customer body = new Customer();
        body.setFirstName("Updated");
        body.setLastName("InA");
        body.setCompanyId(companyA.getId());
        body.setEmployee(false);
        mockMvc.perform(put("/api/customers/" + customerInA.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void update_asCompanyAdmin_targetInOtherCompany_returns403() throws Exception {
        String token = tokenFor("companyadmin", "secret");
        Customer body = new Customer();
        body.setFirstName("Updated");
        body.setLastName("InB");
        body.setCompanyId(companyB.getId());
        body.setEmployee(false);
        mockMvc.perform(put("/api/customers/" + customerInB.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_asCompanyAdmin_targetInOwnCompany_returns204() throws Exception {
        Customer toDelete = new Customer();
        toDelete.setFirstName("ToDelete");
        toDelete.setLastName("InA");
        toDelete.setCompanyId(companyA.getId());
        toDelete.setEmployee(false);
        toDelete = customerRepository.save(toDelete);
        String token = tokenFor("companyadmin", "secret");
        mockMvc.perform(delete("/api/customers/" + toDelete.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_asCompanyAdmin_targetInOtherCompany_returns403() throws Exception {
        String token = tokenFor("companyadmin", "secret");
        mockMvc.perform(delete("/api/customers/" + customerInB.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_asSuperAdmin_anyCompany_succeeds() throws Exception {
        String token = tokenFor("superadmin", "secret");
        Customer newCustomer = new Customer();
        newCustomer.setFirstName("New");
        newCustomer.setLastName("Person");
        newCustomer.setCompanyId(companyB.getId());
        newCustomer.setEmployee(false);
        mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyId").value(companyB.getId().intValue()));
    }
}
