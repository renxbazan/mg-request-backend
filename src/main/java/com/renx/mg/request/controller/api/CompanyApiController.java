package com.renx.mg.request.controller.api;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.dto.CompanyDTO;
import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.dto.RequestApproverDTO;
import com.renx.mg.request.model.Company;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CompanyRepository;
import com.renx.mg.request.security.CurrentUserService;
import com.renx.mg.request.service.RequestApproverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/companies")
public class CompanyApiController {

    private final CompanyRepository companyRepository;
    private final CurrentUserService currentUserService;
    private final RequestApproverService requestApproverService;

    public CompanyApiController(CompanyRepository companyRepository,
                                CurrentUserService currentUserService,
                                RequestApproverService requestApproverService) {
        this.companyRepository = companyRepository;
        this.currentUserService = currentUserService;
        this.requestApproverService = requestApproverService;
    }

    private boolean isSuperAdmin() {
        User u = currentUserService.getCurrentUser();
        return u != null && Constants.SUPER_ADMIN_PROFILE_ID.equals(u.getProfileId());
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

    @GetMapping("/{id}/approvers")
    public ResponseEntity<List<RequestApproverDTO>> listApprovers(@PathVariable Long id) {
        if (!isSuperAdmin()) return ResponseEntity.status(403).build();
        if (!companyRepository.existsById(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(requestApproverService.listByCompany(id));
    }

    @PostMapping("/{id}/approvers")
    public ResponseEntity<RequestApproverDTO> addApprover(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (!isSuperAdmin()) return ResponseEntity.status(403).build();
        if (!companyRepository.existsById(id)) return ResponseEntity.notFound().build();
        Long userId = body.get("userId") != null ? Long.valueOf(body.get("userId").toString()) : null;
        if (userId == null) return ResponseEntity.badRequest().build();
        boolean companyLevel = "COMPANY".equalsIgnoreCase(String.valueOf(body.get("scope")));
        Long siteId = body.get("siteId") != null ? Long.valueOf(body.get("siteId").toString()) : null;
        try {
            RequestApproverDTO dto = requestApproverService.addApprover(id, userId, companyLevel, siteId);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/approvers")
    public ResponseEntity<Void> removeApprover(@PathVariable Long id,
                                               @RequestParam Long userId,
                                               @RequestParam(required = false) Boolean companyLevel,
                                               @RequestParam(required = false) Long siteId) {
        if (!isSuperAdmin()) return ResponseEntity.status(403).build();
        if (!companyRepository.existsById(id)) return ResponseEntity.notFound().build();
        requestApproverService.removeApprover(id, userId, companyLevel, siteId);
        return ResponseEntity.noContent().build();
    }
}
