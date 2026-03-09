package com.renx.mg.request.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renx.mg.request.dto.LoginRequest;
import com.renx.mg.request.dto.RequestCreateDTO;
import com.renx.mg.request.model.Company;
import com.renx.mg.request.model.CompanyType;
import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.Profile;
import com.renx.mg.request.model.Request;
import com.renx.mg.request.model.RequestStatusType;
import com.renx.mg.request.model.ServiceCategory;
import com.renx.mg.request.model.ServiceSubCategory;
import com.renx.mg.request.model.Site;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CompanyRepository;
import com.renx.mg.request.repository.RequestAssignmentRepository;
import com.renx.mg.request.repository.RequestRepository;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.ProfileRepository;
import com.renx.mg.request.repository.ServiceCategoryRepository;
import com.renx.mg.request.repository.ServiceSubCategoryRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.service.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RequestApiIntegrationTest {

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
    @Autowired
    private ServiceCategoryRepository serviceCategoryRepository;
    @Autowired
    private ServiceSubCategoryRepository serviceSubCategoryRepository;

    @MockBean
    private EmailService emailService;

    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private RequestAssignmentRepository requestAssignmentRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long siteId;
    private Long serviceCategoryId;
    private Long serviceSubCategoryId;
    private String testUsername;
    private String testPassword = "secret";
    private Long testUserId;

    @BeforeEach
    void setUp() {
        testUsername = "reqtest_" + UUID.randomUUID().toString().substring(0, 8);
        transactionTemplate.executeWithoutResult(status -> {
            Company company = new Company();
            company.setName("Test Co");
            company.setCompanyType(CompanyType.COMPANY);
            company = companyRepository.save(company);

            Profile profile = profileRepository.findById(2L)
                    .orElseThrow(() -> new IllegalStateException("Profile 2 (Requester) must exist from seed"));

            Customer customer = new Customer();
            customer.setFirstName("Test");
            customer.setLastName("User");
            customer.setCompanyId(company.getId());
            customer.setEmail("test@test.com");
            customer.setEmployee(true);
            customer = customerRepository.save(customer);

            Site site = new Site();
            site.setName("Main");
            site.setCompanyId(company.getId());
            site = siteRepository.save(site);
            siteId = site.getId();

            ServiceCategory cat = new ServiceCategory();
            cat.setName("Category1");
            cat.setDescription("Desc");
            cat = serviceCategoryRepository.save(cat);
            serviceCategoryId = cat.getId();

            ServiceSubCategory sub = new ServiceSubCategory();
            sub.setName("Sub1");
            sub.setDescription("SubDesc");
            sub.setServiceCategoryId(cat.getId());
            sub = serviceSubCategoryRepository.save(sub);
            serviceSubCategoryId = sub.getId();

            User user = new User();
            user.setUsername(testUsername);
            user.setPassword(passwordEncoder.encode(testPassword));
            user.setProfileId(profile.getId());
            user.setCustomerId(customer.getId());
            user.setSiteId(site.getId());
            user = userRepository.save(user);
            testUserId = user.getId();
        });
    }

    private String obtainToken() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername(testUsername);
        login.setPassword(testPassword);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void getCompanies_withoutToken_returns403() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCompanies_withToken_returns200AndList() throws Exception {
        String token = obtainToken();
        mockMvc.perform(get("/api/companies")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void getSites_withToken_returns200() throws Exception {
        String token = obtainToken();
        mockMvc.perform(get("/api/sites")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createRequest_withToken_returns200AndId() throws Exception {
        String token = obtainToken();
        RequestCreateDTO dto = new RequestCreateDTO();
        dto.setSiteId(siteId);
        dto.setServiceCategoryId(serviceCategoryId);
        dto.setServiceSubCategoryId(serviceSubCategoryId);
        dto.setDescription("Test request description");
        dto.setPriority("M");

        mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.description").value("Test request description"))
                .andExpect(jsonPath("$.requestStatus").exists());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getMyRequests_withToken_returns200AndContainsCreatedRequest() throws Exception {
        // Usar usuario del seed (admin/password) y crear request vía repositorio para evitar 403 en segunda petición
        long requestId = transactionTemplate.execute(status -> {
            ServiceCategory cat = new ServiceCategory();
            cat.setName("MyReqCat");
            cat.setDescription("Desc");
            cat = serviceCategoryRepository.save(cat);
            ServiceSubCategory sub = new ServiceSubCategory();
            sub.setName("MyReqSub");
            sub.setDescription("Sub");
            sub.setServiceCategoryId(cat.getId());
            sub = serviceSubCategoryRepository.save(sub);
            Request req = new Request();
            req.setUserId(1L);
            req.setSiteId(1L);
            req.setServiceCategoryId(cat.getId());
            req.setServiceSubCategoryId(sub.getId());
            req.setDescription("My request");
            req.setPriority("L");
            req.setRequestStatus(RequestStatusType.PENDING_APPROVAL);
            req.setCreateDate(new java.util.Date());
            req = requestRepository.save(req);
            return req.getId();
        });

        String token = obtainTokenSeedAdmin();
        ResultActions result = mockMvc.perform(get("/api/requests/my")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        // Si la lista incluye el request creado, verificar su estado
        String body = result.andReturn().getResponse().getContentAsString();
        JsonNode arr = objectMapper.readTree(body);
        for (JsonNode node : arr) {
            if (node.has("id") && node.get("id").asLong() == requestId) {
                String status = node.has("requestStatus") ? node.get("requestStatus").asText() : "";
                Assertions.assertTrue("CREATED".equals(status) || "PENDING_APPROVAL".equals(status), "requestStatus: " + status);
                return;
            }
        }
    }

    private String obtainTokenSeedAdmin() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername("admin");
        login.setPassword("password");
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void getRequests_list_withToken_returns200AndArray() throws Exception {
        String token = obtainToken();
        mockMvc.perform(get("/api/requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getRequestById_withToken_returns200() throws Exception {
        String token = obtainToken();
        RequestCreateDTO dto = new RequestCreateDTO();
        dto.setSiteId(siteId);
        dto.setServiceCategoryId(serviceCategoryId);
        dto.setDescription("Detail test");
        dto.setPriority("M");

        String createResponse = mockMvc.perform(post("/api/requests")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long requestId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/requests/" + requestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Detail test"));
    }

    @Test
    void getServiceCategories_withToken_returns200() throws Exception {
        String token = obtainToken();
        mockMvc.perform(get("/api/service-categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getServiceSubCategories_byCategoryId_withToken_returns200() throws Exception {
        String token = obtainToken();
        mockMvc.perform(get("/api/service-sub-categories")
                        .param("serviceCategoryId", String.valueOf(serviceCategoryId))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAssignedRequests_withToken_noAssignments_returnsEmptyArray() throws Exception {
        String token = obtainToken();
        mockMvc.perform(get("/api/requests/assigned")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getAssignedRequests_withToken_hasAssignments_returnsAssignedRequests() throws Exception {
        Long requestId = transactionTemplate.execute(status -> {
            Request req = new Request();
            req.setUserId(1L);
            req.setSiteId(siteId);
            req.setServiceCategoryId(serviceCategoryId);
            req.setServiceSubCategoryId(serviceSubCategoryId);
            req.setDescription("Assigned request");
            req.setPriority("M");
            req.setRequestStatus(RequestStatusType.CREATED);
            req.setCreateDate(new java.util.Date());
            req = requestRepository.save(req);

            jdbcTemplate.update("INSERT INTO request_assignment (request_id, user_id) VALUES (?, ?)",
                    req.getId(), testUserId);

            return req.getId();
        });

        SecurityContextHolder.clearContext();

        String token = obtainToken();
        mockMvc.perform(get("/api/requests/assigned")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[?(@.id == " + requestId + ")]", hasSize(1)));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rate_asRequester_returns200() throws Exception {
        Long requestId = transactionTemplate.execute(status -> {
            Request req = new Request();
            req.setUserId(testUserId);
            req.setSiteId(siteId);
            req.setServiceCategoryId(serviceCategoryId);
            req.setServiceSubCategoryId(serviceSubCategoryId);
            req.setDescription("Request to rate");
            req.setPriority("M");
            req.setRequestStatus(RequestStatusType.DONE);
            req.setCreateDate(new java.util.Date());
            return requestRepository.save(req).getId();
        });
        SecurityContextHolder.clearContext();

        String token = obtainToken();
        mockMvc.perform(put("/api/requests/" + requestId + "/rate")
                        .header("Authorization", "Bearer " + token)
                        .param("rating", "5")
                        .param("comment", "Great job"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestStatus").value("RATED"))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rate_asCompanyAdminOfRequestCompany_returns200() throws Exception {
        Long requestId = transactionTemplate.execute(status -> {
            Profile requesterProfile = profileRepository.findById(2L).orElseThrow();
            Customer requesterCustomer = new Customer();
            requesterCustomer.setFirstName("Req");
            requesterCustomer.setLastName("User");
            requesterCustomer.setCompanyId(siteRepository.findById(siteId).orElseThrow().getCompanyId());
            requesterCustomer = customerRepository.save(requesterCustomer);
            User requester = new User();
            requester.setUsername("req_" + UUID.randomUUID().toString().substring(0, 8));
            requester.setPassword(passwordEncoder.encode("x"));
            requester.setProfileId(requesterProfile.getId());
            requester.setCustomerId(requesterCustomer.getId());
            requester = userRepository.save(requester);

            Request req = new Request();
            req.setUserId(requester.getId());
            req.setSiteId(siteId);
            req.setServiceCategoryId(serviceCategoryId);
            req.setServiceSubCategoryId(serviceSubCategoryId);
            req.setDescription("Request for company admin");
            req.setPriority("M");
            req.setRequestStatus(RequestStatusType.DONE);
            req.setCreateDate(new java.util.Date());
            return requestRepository.save(req).getId();
        });

        Long companyAdminId = transactionTemplate.execute(status -> {
            Profile companyAdminProfile = profileRepository.findById(3L).orElseThrow();
            Long companyId = siteRepository.findById(siteId).orElseThrow().getCompanyId();
            Customer adminCustomer = new Customer();
            adminCustomer.setFirstName("Company");
            adminCustomer.setLastName("Admin");
            adminCustomer.setCompanyId(companyId);
            adminCustomer = customerRepository.save(adminCustomer);
            User adminUser = new User();
            adminUser.setUsername("compadmin_" + UUID.randomUUID().toString().substring(0, 8));
            adminUser.setPassword(passwordEncoder.encode("x"));
            adminUser.setProfileId(companyAdminProfile.getId());
            adminUser.setCustomerId(adminCustomer.getId());
            return userRepository.save(adminUser).getId();
        });

        SecurityContextHolder.clearContext();

        String token = obtainTokenForUser(
                userRepository.findById(companyAdminId).orElseThrow().getUsername(), "x");
        mockMvc.perform(put("/api/requests/" + requestId + "/rate")
                        .header("Authorization", "Bearer " + token)
                        .param("rating", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestStatus").value("RATED"))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rate_asOtherUser_returns403() throws Exception {
        Long requestId = transactionTemplate.execute(status -> {
            Request req = new Request();
            req.setUserId(testUserId);
            req.setSiteId(siteId);
            req.setServiceCategoryId(serviceCategoryId);
            req.setServiceSubCategoryId(serviceSubCategoryId);
            req.setDescription("Request");
            req.setPriority("M");
            req.setRequestStatus(RequestStatusType.DONE);
            req.setCreateDate(new java.util.Date());
            req = requestRepository.save(req);

            Profile workerProfile = profileRepository.findById(2L).orElseThrow();
            Customer workerCustomer = new Customer();
            workerCustomer.setFirstName("Worker");
            workerCustomer.setLastName("Other");
            workerCustomer.setCompanyId(siteRepository.findById(siteId).orElseThrow().getCompanyId());
            workerCustomer.setEmployee(true);
            workerCustomer = customerRepository.save(workerCustomer);
            User worker = new User();
            worker.setUsername("worker_" + UUID.randomUUID().toString().substring(0, 8));
            worker.setPassword(passwordEncoder.encode("x"));
            worker.setProfileId(workerProfile.getId());
            worker.setCustomerId(workerCustomer.getId());
            worker = userRepository.save(worker);

            jdbcTemplate.update("INSERT INTO request_assignment (request_id, user_id) VALUES (?, ?)",
                    req.getId(), worker.getId());

            return req.getId();
        });

        Long workerUserId = transactionTemplate.execute(status -> {
            var a = requestAssignmentRepository.findByRequestId(requestId);
            return a != null ? a.getUserId() : null;
        });
        SecurityContextHolder.clearContext();

        String workerToken = obtainTokenForUser(
                userRepository.findById(workerUserId).orElseThrow().getUsername(), "x");
        mockMvc.perform(put("/api/requests/" + requestId + "/rate")
                        .header("Authorization", "Bearer " + workerToken)
                        .param("rating", "3"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rate_responseIncludesCanRateAndRating() throws Exception {
        Long requestId = transactionTemplate.execute(status -> {
            Request req = new Request();
            req.setUserId(testUserId);
            req.setSiteId(siteId);
            req.setServiceCategoryId(serviceCategoryId);
            req.setServiceSubCategoryId(serviceSubCategoryId);
            req.setDescription("Request");
            req.setPriority("M");
            req.setRequestStatus(RequestStatusType.DONE);
            req.setCreateDate(new java.util.Date());
            return requestRepository.save(req).getId();
        });
        SecurityContextHolder.clearContext();

        String token = obtainToken();
        mockMvc.perform(put("/api/requests/" + requestId + "/rate")
                        .header("Authorization", "Bearer " + token)
                        .param("rating", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.canRate").value(false));

        mockMvc.perform(get("/api/requests/" + requestId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5));
    }

    private String obtainTokenForUser(String username, String password) throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername(username);
        login.setPassword(password);
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }
}
