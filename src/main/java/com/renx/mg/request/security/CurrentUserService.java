package com.renx.mg.request.security;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Obtiene el usuario actual autenticado (JWT) desde el contexto de seguridad.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public CurrentUserService(UserRepository userRepository, CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName();
        return userRepository.findByUsername(username);
    }

    /**
     * Si el usuario actual es Company Admin (profileId = 3), devuelve el companyId de su customer.
     * En caso contrario devuelve null.
     */
    public Long getCurrentUserCompanyIdIfCompanyAdmin() {
        User user = getCurrentUser();
        if (user == null || user.getCustomerId() == null
                || !Constants.COMPANY_ADMIN_PROFILE_ID.equals(user.getProfileId())) {
            return null;
        }
        return customerRepository.findById(user.getCustomerId())
                .map(Customer::getCompanyId)
                .orElse(null);
    }
}
