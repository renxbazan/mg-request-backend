package com.renx.mg.request.controller.api;

import com.renx.mg.request.dto.CompanyDTO;
import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.model.Company;
import com.renx.mg.request.repository.CompanyRepository;
import com.renx.mg.request.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/companies")
public class CompanyApiController {

    private final CompanyRepository companyRepository;
    private final CurrentUserService currentUserService;

    public CompanyApiController(CompanyRepository companyRepository, CurrentUserService currentUserService) {
        this.companyRepository = companyRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<CompanyDTO> list() {
        return StreamSupport.stream(companyRepository.findAll().spliterator(), false)
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> get(@PathVariable Long id) {
        return companyRepository.findById(id)
                .map(DtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CompanyDTO> create(@RequestBody Company company) {
        if (currentUserService.getCurrentUserCompanyIdIfCompanyAdmin() != null) {
            return ResponseEntity.status(403).build();
        }
        Company saved = companyRepository.save(company);
        return ResponseEntity.status(201).body(DtoMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDTO> update(@PathVariable Long id, @RequestBody Company body) {
        if (currentUserService.getCurrentUserCompanyIdIfCompanyAdmin() != null) {
            return ResponseEntity.status(403).build();
        }
        return companyRepository.findById(id)
                .map(existing -> {
                    if (body.getName() != null) existing.setName(body.getName());
                    if (body.getDescription() != null) existing.setDescription(body.getDescription());
                    if (body.getCompanyType() != null) existing.setCompanyType(body.getCompanyType());
                    return ResponseEntity.ok(DtoMapper.toDto(companyRepository.save(existing)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (currentUserService.getCurrentUserCompanyIdIfCompanyAdmin() != null) {
            return ResponseEntity.status(403).body((Void) null);
        }
        if (!companyRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        companyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
