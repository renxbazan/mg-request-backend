package com.renx.mg.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

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

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setName("Test Co");
        company.setCompanyType(CompanyType.COMPANY);
        company = companyRepository.save(company);

        Profile profile = new Profile();
        profile.setDescription("Requester");
        profile = profileRepository.save(profile);

        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setCompanyId(company.getId());
        customer.setEmail("test@test.com");
        customer.setEmployee(false);
        customer = customerRepository.save(customer);

        Site site = new Site();
        site.setName("Main");
        site.setCompanyId(company.getId());
        site = siteRepository.save(site);

        User user = new User();
        user.setUsername("apitest");
        user.setPassword(passwordEncoder.encode("secret"));
        user.setProfileId(profile.getId());
        user.setCustomerId(customer.getId());
        user.setSiteId(site.getId());
        userRepository.save(user);
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("nobody");
        request.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", not(emptyOrNullString())));
    }

    @Test
    void login_withValidCredentials_returns200AndToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("apitest");
        request.setPassword("secret");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("apitest"))
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.profileId").isNumber())
                .andExpect(jsonPath("$.locale").exists())
                .andExpect(jsonPath("$.locale").value(anyOf(equalTo("es"), equalTo("en"))));
    }

    @Test
    void getMe_withoutToken_returns403() throws Exception {
        // Sin token, Spring Security responde 403 Forbidden (no 401)
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMe_withValidToken_returns200AndUsername() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("apitest");
        loginRequest.setPassword("secret");
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String token = objectMapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("apitest"))
                .andExpect(jsonPath("$.locale").exists())
                .andExpect(jsonPath("$.locale").value(anyOf(equalTo("es"), equalTo("en"))))
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.profileId").isNumber())
                .andExpect(jsonPath("$.employee").value(false));
    }

    @Test
    void getMe_withEmployeeCustomer_returnsEmployeeTrue() throws Exception {
        Customer employeeCustomer = new Customer();
        employeeCustomer.setFirstName("Worker");
        employeeCustomer.setLastName("Employee");
        employeeCustomer.setCompanyId(companyRepository.findAll().get(0).getId());
        employeeCustomer.setEmployee(true);
        employeeCustomer = customerRepository.save(employeeCustomer);

        User employeeUser = new User();
        employeeUser.setUsername("worker");
        employeeUser.setPassword(passwordEncoder.encode("pass"));
        employeeUser.setProfileId(profileRepository.findAll().get(0).getId());
        employeeUser.setCustomerId(employeeCustomer.getId());
        employeeUser = userRepository.save(employeeUser);

        String token = obtainTokenForUser("worker", "pass");
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("worker"))
                .andExpect(jsonPath("$.employee").value(true));
    }

    @Test
    void putMeLocale_withValidTokenAndValidLocale_returns204() throws Exception {
        String token = obtainToken();
        mockMvc.perform(put("/api/auth/me/locale")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locale\":\"en\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locale").value("en"));
    }

    @Test
    void putMeLocale_withInvalidLocale_returns400() throws Exception {
        String token = obtainToken();
        mockMvc.perform(put("/api/auth/me/locale")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locale\":\"fr\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putMeLocale_withoutToken_returns401() throws Exception {
        mockMvc.perform(put("/api/auth/me/locale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locale\":\"en\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void menu_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/auth/menu"))
                .andExpect(status().isForbidden());
    }

    @Test
    void menu_withValidToken_returns200AndArray() throws Exception {
        String token = obtainToken();
        mockMvc.perform(get("/api/auth/menu").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private String obtainToken() throws Exception {
        return obtainTokenForUser("apitest", "secret");
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
}
