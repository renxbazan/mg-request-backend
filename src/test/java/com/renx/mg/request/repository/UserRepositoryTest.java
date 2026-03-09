package com.renx.mg.request.repository;

import com.renx.mg.request.model.Company;
import com.renx.mg.request.model.CompanyType;
import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.Profile;
import com.renx.mg.request.model.Site;
import com.renx.mg.request.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private SiteRepository siteRepository;

    @Test
    void findByUsername_returnsUserWhenExists() {
        Profile profile = new Profile();
        profile.setDescription("Test");
        profile = profileRepository.save(profile);
        Company company = new Company();
        company.setName("C");
        company.setCompanyType(CompanyType.COMPANY);
        company = companyRepository.save(company);
        Customer customer = new Customer();
        customer.setFirstName("F");
        customer.setLastName("L");
        customer.setCompanyId(company.getId());
        customer = customerRepository.save(customer);
        Site site = new Site();
        site.setName("S");
        site.setCompanyId(company.getId());
        site = siteRepository.save(site);

        User user = new User();
        user.setUsername("johndoe");
        user.setPassword("secret");
        user.setProfileId(profile.getId());
        user.setCustomerId(customer.getId());
        user.setSiteId(site.getId());
        userRepository.save(user);

        User found = userRepository.findByUsername("johndoe");
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("johndoe");
    }

    @Test
    void findByUsername_returnsNullWhenNotExists() {
        User found = userRepository.findByUsername("nonexistent");
        assertThat(found).isNull();
    }
}
