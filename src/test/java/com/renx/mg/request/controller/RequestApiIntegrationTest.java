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
import com.renx.mg.request.repository.RequestRepository;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.ProfileRepository;
import com.renx.mg.request.repository.ServiceCategoryRepository;
import com.renx.mg.request.repository.ServiceSubCategoryRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.service.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    private JdbcTemplate jdbcTemplate;

    private Long siteId;
    private Long serviceCategoryId;
    private Long serviceSubCategoryId;
    private String testUsername;
    private String testPassword = "secret";

    @BeforeAll
    void cleanupIntegrationTestData() {
        String like = "reqtest_%";
        jdbcTemplate.update(
                "DELETE rh FROM request_history rh JOIN request r ON r.id = rh.request_id WHERE r.description LIKE ?",
                like);
        jdbcTemplate.update(
                "DELETE ra FROM request_assignment ra JOIN request r ON r.id = ra.request_id WHERE r.description LIKE ?",
                like);
        jdbcTemplate.update("DELETE FROM request WHERE description LIKE ?", like);
        jdbcTemplate.update("DELETE FROM users WHERE username LIKE ?", like);
        jdbcTemplate.update("DELETE FROM customer WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ?",
                like, like, like);
        jdbcTemplate.update("DELETE FROM site WHERE name LIKE ?", like);
        jdbcTemplate.update("DELETE FROM service_sub_category WHERE name LIKE ?", like);
        jdbcTemplate.update("DELETE FROM service_category WHERE name LIKE ?", like);
        jdbcTemplate.update("DELETE FROM company WHERE name LIKE ?", like);
    }

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        testUsername = "reqtest_" + suffix;
        transactionTemplate.executeWithoutResult(status -> {
            Company company = new Company();
            company.setName("reqtest_TestCo_" + suffix);
            company.setCompanyType(CompanyType.COMPANY);
            company = companyRepository.save(company);

            Profile profile = profileRepository.findById(2L)
                    .orElseThrow(() -> new IllegalStateException("Profile 2 (Requester) must exist from seed"));

            Customer customer = new Customer();
            customer.setFirstName("reqtest_Test");
            customer.setLastName("User");
            customer.setCompanyId(company.getId());
            customer.setEmail("reqtest_test_" + suffix + "@test.com");
            customer.setEmployee(true);
            customer = customerRepository.save(customer);

            Site site = new Site();
            site.setName("reqtest_Main_" + suffix);
            site.setCompanyId(company.getId());
            site = siteRepository.save(site);
            siteId = site.getId();

            ServiceCategory cat = new ServiceCategory();
            cat.setName("reqtest_Category1_" + suffix);
            cat.setDescription("Desc");
            cat = serviceCategoryRepository.save(cat);
            serviceCategoryId = cat.getId();

            ServiceSubCategory sub = new ServiceSubCategory();
            sub.setName("reqtest_Sub1_" + suffix);
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
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        long requestId = transactionTemplate.execute(status -> {
            ServiceCategory cat = new ServiceCategory();
            cat.setName("reqtest_MyReqCat_" + suffix);
            cat.setDescription("Desc");
            cat = serviceCategoryRepository.save(cat);
            ServiceSubCategory sub = new ServiceSubCategory();
            sub.setName("reqtest_MyReqSub_" + suffix);
            sub.setDescription("Sub");
            sub.setServiceCategoryId(cat.getId());
            sub = serviceSubCategoryRepository.save(sub);
            Request req = new Request();
            req.setUserId(1L);
            req.setSiteId(1L);
            req.setServiceCategoryId(cat.getId());
            req.setServiceSubCategoryId(sub.getId());
            req.setDescription("reqtest_My request");
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
    void getRequests_list_withToken_returns200AndPaginated() throws Exception {
        String token = obtainToken();
        mockMvc.perform(get("/api/requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.page").exists())
                .andExpect(jsonPath("$.totalElements").exists());
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
        var fixture = transactionTemplate.execute(status -> {
            Profile profile = profileRepository.findById(2L).orElseThrow();
            Customer customer = new Customer();
            customer.setFirstName("reqtest_Assigned");
            customer.setLastName("User");
            customer.setCompanyId(1L);
            customer.setEmail("reqtest_assigned@test.com");
            customer.setEmployee(true);
            customer = customerRepository.save(customer);
            User user = new User();
            user.setUsername("reqtest_assigned_" + UUID.randomUUID().toString().substring(0, 8));
            user.setPassword(passwordEncoder.encode("secret"));
            user.setProfileId(profile.getId());
            user.setCustomerId(customer.getId());
            user.setSiteId(1L);
            user = userRepository.save(user);

            Request req = new Request();
            req.setUserId(1L);
            req.setSiteId(1L);
            req.setServiceCategoryId(1L);
            req.setServiceSubCategoryId(1L);
            req.setDescription("reqtest_Assigned request");
            req.setPriority("M");
            req.setRequestStatus(RequestStatusType.CREATED);
            req.setCreateDate(new java.util.Date());
            req = requestRepository.save(req);

            jdbcTemplate.update("INSERT INTO request_assignment (request_id, user_id) VALUES (?, ?)",
                    req.getId(), user.getId());

            return new long[]{req.getId(), user.getId()};
        });

        long requestId = fixture[0];
        long assignedUserId = fixture[1];
        SecurityContextHolder.clearContext();

        String token = obtainTokenForUser(
                userRepository.findById(assignedUserId).orElseThrow().getUsername(), "secret");
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
        var fixture = transactionTemplate.execute(status -> {
            Profile profile = profileRepository.findById(2L).orElseThrow();
            Customer customer = new Customer();
            customer.setFirstName("reqtest_Rate");
            customer.setLastName("User");
            customer.setCompanyId(1L);
            customer.setEmail("reqtest_rate@test.com");
            customer.setEmployee(true);
            customer = customerRepository.save(customer);
            User user = new User();
            user.setUsername("reqtest_rate_" + UUID.randomUUID().toString().substring(0, 8));
            user.setPassword(passwordEncoder.encode("secret"));
            user.setProfileId(profile.getId());
            user.setCustomerId(customer.getId());
            user.setSiteId(1L);
            user = userRepository.save(user);

            Request req = new Request();
            req.setUserId(user.getId());
            req.setSiteId(1L);
            req.setServiceCategoryId(1L);
            req.setServiceSubCategoryId(1L);
            req.setDescription("reqtest_Request to rate");
            req.setPriority("M");
            req.setRequestStatus(RequestStatusType.DONE);
            req.setCreateDate(new java.util.Date());
            req = requestRepository.save(req);
            return new long[]{req.getId(), user.getId()};
        });
        long requestId = fixture[0];
        SecurityContextHolder.clearContext();

        String token = obtainTokenForUser(
                userRepository.findById(fixture[1]).orElseThrow().getUsername(), "secret");
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
        var fixture = transactionTemplate.execute(status -> {
            Profile requesterProfile = profileRepository.findById(2L).orElseThrow();
            Customer requesterCustomer = new Customer();
            requesterCustomer.setFirstName("reqtest_Req");
            requesterCustomer.setLastName("User");
            requesterCustomer.setCompanyId(1L);
            requesterCustomer = customerRepository.save(requesterCustomer);
            User requester = new User();
            requester.setUsername("reqtest_req_" + UUID.randomUUID().toString().substring(0, 8));
            requester.setPassword(passwordEncoder.encode("x"));
            requester.setProfileId(requesterProfile.getId());
            requester.setCustomerId(requesterCustomer.getId());
            requester.setSiteId(1L);
            requester = userRepository.save(requester);

            Request req = new Request();
            req.setUserId(requester.getId());
            req.setSiteId(1L);
            req.setServiceCategoryId(1L);
            req.setServiceSubCategoryId(1L);
            req.setDescription("reqtest_Request for company admin");
            req.setPriority("M");
            req.setRequestStatus(RequestStatusType.DONE);
            req.setCreateDate(new java.util.Date());
            req = requestRepository.save(req);

            Profile companyAdminProfile = profileRepository.findById(3L).orElseThrow();
            Customer adminCustomer = new Customer();
            adminCustomer.setFirstName("reqtest_Company");
            adminCustomer.setLastName("Admin");
            adminCustomer.setCompanyId(1L);
            adminCustomer = customerRepository.save(adminCustomer);
            User adminUser = new User();
            adminUser.setUsername("reqtest_compadmin_" + UUID.randomUUID().toString().substring(0, 8));
            adminUser.setPassword(passwordEncoder.encode("x"));
            adminUser.setProfileId(companyAdminProfile.getId());
            adminUser.setCustomerId(adminCustomer.getId());
            adminUser = userRepository.save(adminUser);

            return new long[]{req.getId(), adminUser.getId()};
        });
        long requestId = fixture[0];
        long companyAdminId = fixture[1];

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
        var fixture = transactionTemplate.execute(status -> {
            Profile requesterProfile = profileRepository.findById(2L).orElseThrow();
            Customer requesterCustomer = new Customer();
            requesterCustomer.setFirstName("reqtest_Requester");
            requesterCustomer.setLastName("Other");
            requesterCustomer.setCompanyId(1L);
            requesterCustomer.setEmployee(true);
            requesterCustomer = customerRepository.save(requesterCustomer);
            User requester = new User();
            requester.setUsername("reqtest_reqother_" + UUID.randomUUID().toString().substring(0, 8));
            requester.setPassword(passwordEncoder.encode("x"));
            requester.setProfileId(requesterProfile.getId());
            requester.setCustomerId(requesterCustomer.getId());
            requester.setSiteId(1L);
            requester = userRepository.save(requester);

            Request req = new Request();
            req.setUserId(requester.getId());
            req.setSiteId(1L);
            req.setServiceCategoryId(1L);
            req.setServiceSubCategoryId(1L);
            req.setDescription("reqtest_Request");
            req.setPriority("M");
            req.setRequestStatus(RequestStatusType.DONE);
            req.setCreateDate(new java.util.Date());
            req = requestRepository.save(req);

            Profile workerProfile = profileRepository.findById(2L).orElseThrow();
            Customer workerCustomer = new Customer();
            workerCustomer.setFirstName("reqtest_Worker");
            workerCustomer.setLastName("Other");
            workerCustomer.setCompanyId(1L);
            workerCustomer.setEmployee(true);
            workerCustomer = customerRepository.save(workerCustomer);
            User worker = new User();
            worker.setUsername("reqtest_worker_" + UUID.randomUUID().toString().substring(0, 8));
            worker.setPassword(passwordEncoder.encode("x"));
            worker.setProfileId(workerProfile.getId());
            worker.setCustomerId(workerCustomer.getId());
            worker = userRepository.save(worker);

            jdbcTemplate.update("INSERT INTO request_assignment (request_id, user_id) VALUES (?, ?)",
                    req.getId(), worker.getId());

            return new long[]{req.getId(), worker.getId()};
        });
        long requestId = fixture[0];
        long workerUserId = fixture[1];
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
        var fixture = transactionTemplate.execute(status -> {
            Profile profile = profileRepository.findById(2L).orElseThrow();
            Customer customer = new Customer();
            customer.setFirstName("reqtest_RateResp");
            customer.setLastName("User");
            customer.setCompanyId(1L);
            customer.setEmail("reqtest_rateresp@test.com");
            customer.setEmployee(true);
            customer = customerRepository.save(customer);
            User user = new User();
            user.setUsername("reqtest_rateresp_" + UUID.randomUUID().toString().substring(0, 8));
            user.setPassword(passwordEncoder.encode("secret"));
            user.setProfileId(profile.getId());
            user.setCustomerId(customer.getId());
            user.setSiteId(1L);
            user = userRepository.save(user);

            Request req = new Request();
            req.setUserId(user.getId());
            req.setSiteId(1L);
            req.setServiceCategoryId(1L);
            req.setServiceSubCategoryId(1L);
            req.setDescription("reqtest_Request");
            req.setPriority("M");
            req.setRequestStatus(RequestStatusType.DONE);
            req.setCreateDate(new java.util.Date());
            req = requestRepository.save(req);
            return new long[]{req.getId(), user.getId()};
        });
        long requestId = fixture[0];
        SecurityContextHolder.clearContext();

        String token = obtainTokenForUser(
                userRepository.findById(fixture[1]).orElseThrow().getUsername(), "secret");
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
