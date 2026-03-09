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

import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserApiIntegrationTest {

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

    private User testUser;
    private Company companyA;
    private Company companyB;
    private Customer customerInA;
    private Customer customerInB;
    private User userInB;
    private User companyAdminUser;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setName("Test Co");
        company.setCompanyType(CompanyType.COMPANY);
        company = companyRepository.save(company);
        companyA = company;

        companyB = new Company();
        companyB.setName("Company B");
        companyB.setCompanyType(CompanyType.COMPANY);
        companyB = companyRepository.save(companyB);

        Profile profile = new Profile();
        profile.setDescription("Requester");
        profile = profileRepository.save(profile);

        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setCompanyId(company.getId());
        customer.setEmail("usertest@test.com");
        customer = customerRepository.save(customer);
        customerInA = customer;

        customerInB = new Customer();
        customerInB.setFirstName("Other");
        customerInB.setLastName("User");
        customerInB.setCompanyId(companyB.getId());
        customerInB.setEmail("other@test.com");
        customerInB = customerRepository.save(customerInB);

        Site site = new Site();
        site.setName("Main");
        site.setCompanyId(company.getId());
        site = siteRepository.save(site);

        testUser = new User();
        testUser.setUsername("usrapitest");
        testUser.setPassword(passwordEncoder.encode("secret"));
        testUser.setProfileId(profile.getId());
        testUser.setCustomerId(customer.getId());
        testUser.setSiteId(site.getId());
        testUser.setLocale("es");
        testUser = userRepository.save(testUser);

        Site siteB = new Site();
        siteB.setName("Site B");
        siteB.setCompanyId(companyB.getId());
        siteB = siteRepository.save(siteB);

        userInB = new User();
        userInB.setUsername("userinb");
        userInB.setPassword(passwordEncoder.encode("secret"));
        userInB.setProfileId(profile.getId());
        userInB.setCustomerId(customerInB.getId());
        userInB.setSiteId(siteB.getId());
        userInB.setLocale("es");
        userInB = userRepository.save(userInB);

        Profile companyAdminProfile = profileRepository.findById(Constants.COMPANY_ADMIN_PROFILE_ID).orElseThrow();
        companyAdminUser = new User();
        companyAdminUser.setUsername("companyadmin");
        companyAdminUser.setPassword(passwordEncoder.encode("secret"));
        companyAdminUser.setProfileId(companyAdminProfile.getId());
        companyAdminUser.setCustomerId(customerInA.getId());
        companyAdminUser.setSiteId(site.getId());
        companyAdminUser.setLocale("es");
        companyAdminUser = userRepository.save(companyAdminUser);
    }

    @Test
    void putUserLocale_asAdmin_updatesLocaleAndReflectsInMe() throws Exception {
        String token = obtainToken();
        long userId = testUser.getId();

        mockMvc.perform(put("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("customerId", testUser.getCustomerId(), "profileId", testUser.getProfileId(),
                                        "siteId", testUser.getSiteId() != null ? testUser.getSiteId() : 0,
                                        "locale", "en"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale").value("en"));

        mockMvc.perform(get("/api/users/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale").value("en"));
    }

    private String obtainToken() throws Exception {
        return obtainTokenForUser("usrapitest", "secret");
    }

    private String obtainTokenForUser(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(loginResponse).get("token").asText();
    }

    @Test
    void create_asCompanyAdmin_withCustomerInOwnCompany_returns200() throws Exception {
        String token = obtainTokenForUser("companyadmin", "secret");
        Map<String, Object> body = Map.of(
                "username", "newuser",
                "password", "secret",
                "customerId", customerInA.getId(),
                "profileId", companyAdminUser.getProfileId(),
                "siteId", testUser.getSiteId(),
                "locale", "es"
        );
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.customerId").value(customerInA.getId().intValue()));
    }

    @Test
    void create_asCompanyAdmin_withCustomerInOtherCompany_returns403() throws Exception {
        String token = obtainTokenForUser("companyadmin", "secret");
        Map<String, Object> body = Map.of(
                "username", "newuser",
                "password", "secret",
                "customerId", customerInB.getId(),
                "profileId", companyAdminUser.getProfileId(),
                "siteId", 0,
                "locale", "es"
        );
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void list_asCompanyAdmin_returnsOnlyUsersFromOwnCompany() throws Exception {
        String token = obtainTokenForUser("companyadmin", "secret");
        mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].customerId").value(hasSize(2)));
    }

    @Test
    void update_asCompanyAdmin_targetUserInOtherCompany_returns403() throws Exception {
        String token = obtainTokenForUser("companyadmin", "secret");
        Map<String, Object> body = Map.of(
                "customerId", userInB.getCustomerId(),
                "profileId", userInB.getProfileId(),
                "siteId", userInB.getSiteId() != null ? userInB.getSiteId() : 0,
                "locale", "en"
        );
        mockMvc.perform(put("/api/users/" + userInB.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_asCompanyAdmin_targetUserInOtherCompany_returns403() throws Exception {
        String token = obtainTokenForUser("companyadmin", "secret");
        mockMvc.perform(delete("/api/users/" + userInB.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void get_asCompanyAdmin_targetUserInOtherCompany_returns403() throws Exception {
        String token = obtainTokenForUser("companyadmin", "secret");
        mockMvc.perform(get("/api/users/" + userInB.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
