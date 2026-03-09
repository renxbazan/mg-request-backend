package com.renx.mg.request.controller.api;

import com.renx.mg.request.dto.CustomerDTO;
import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.model.Customer;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/customers")
public class CustomerApiController {

    private final CustomerRepository customerRepository;
    private final CurrentUserService currentUserService;

    public CustomerApiController(CustomerRepository customerRepository, CurrentUserService currentUserService) {
        this.customerRepository = customerRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<CustomerDTO> list(@RequestParam(required = false) Long companyId,
                                  @RequestParam(required = false) Boolean employeesOnly) {
        Long restrictedCompanyId = currentUserService.getCurrentUserCompanyIdIfCompanyAdmin();
        if (restrictedCompanyId != null) {
            companyId = restrictedCompanyId;
        }
        if (companyId != null && Boolean.TRUE.equals(employeesOnly)) {
            return customerRepository.findByCompanyIdAndEmployee(companyId, true).stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());
        }
        if (companyId != null) {
            return customerRepository.findByCompanyId(companyId).stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());
        }
        if (Boolean.TRUE.equals(employeesOnly)) {
            return customerRepository.findByEmployee(true).stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());
        }
        return StreamSupport.stream(customerRepository.findAll().spliterator(), false)
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> get(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(DtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> create(@RequestBody Customer customer) {
        Long allowedCompanyId = currentUserService.getCurrentUserCompanyIdIfCompanyAdmin();
        if (allowedCompanyId != null) {
            if (customer.getCompanyId() == null || !customer.getCompanyId().equals(allowedCompanyId)) {
                return forbiddenCustomer();
            }
        }
        Customer saved = customerRepository.save(customer);
        return ResponseEntity.status(201).body(DtoMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> update(@PathVariable Long id, @RequestBody Customer body) {
        Customer existing = customerRepository.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        Long allowedCompanyId = currentUserService.getCurrentUserCompanyIdIfCompanyAdmin();
        if (allowedCompanyId != null) {
            if (!allowedCompanyId.equals(existing.getCompanyId())) {
                return forbiddenCustomer();
            }
            if (body.getCompanyId() != null && !body.getCompanyId().equals(allowedCompanyId)) {
                return forbiddenCustomer();
            }
        }
        if (body.getFirstName() != null) existing.setFirstName(body.getFirstName());
        if (body.getLastName() != null) existing.setLastName(body.getLastName());
        if (body.getEmail() != null) existing.setEmail(body.getEmail());
        if (body.getPhone() != null) existing.setPhone(body.getPhone());
        if (body.getCompanyId() != null) existing.setCompanyId(body.getCompanyId());
        existing.setEmployee(body.isEmployee());
        return ResponseEntity.ok(DtoMapper.toDto(customerRepository.save(existing)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id).orElse(null);
        if (customer == null) return ResponseEntity.notFound().build();
        Long allowedCompanyId = currentUserService.getCurrentUserCompanyIdIfCompanyAdmin();
        if (allowedCompanyId != null && !allowedCompanyId.equals(customer.getCompanyId())) {
            return forbiddenVoid();
        }
        customerRepository.delete(customer);
        return ResponseEntity.noContent().build();
    }

    private static ResponseEntity<CustomerDTO> forbiddenCustomer() {
        return ResponseEntity.status(403).body((CustomerDTO) null);
    }

    private static ResponseEntity<Void> forbiddenVoid() {
        return ResponseEntity.status(403).body((Void) null);
    }
}
