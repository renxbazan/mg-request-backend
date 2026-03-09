package com.renx.mg.request.controller.api;

import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.dto.ServiceCategoryDTO;
import com.renx.mg.request.model.ServiceCategory;
import com.renx.mg.request.repository.ServiceCategoryRepository;
import com.renx.mg.request.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/service-categories")
public class ServiceCategoryApiController {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final CurrentUserService currentUserService;

    public ServiceCategoryApiController(ServiceCategoryRepository serviceCategoryRepository, CurrentUserService currentUserService) {
        this.serviceCategoryRepository = serviceCategoryRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ServiceCategoryDTO> list() {
        return StreamSupport.stream(serviceCategoryRepository.findAll().spliterator(), false)
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceCategoryDTO> get(@PathVariable Long id) {
        return serviceCategoryRepository.findById(id)
                .map(DtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServiceCategoryDTO> create(@RequestBody ServiceCategory category) {
        if (currentUserService.getCurrentUserCompanyIdIfCompanyAdmin() != null) {
            return ResponseEntity.status(403).build();
        }
        ServiceCategory saved = serviceCategoryRepository.save(category);
        return ResponseEntity.status(201).body(DtoMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceCategoryDTO> update(@PathVariable Long id, @RequestBody ServiceCategory body) {
        if (currentUserService.getCurrentUserCompanyIdIfCompanyAdmin() != null) {
            return ResponseEntity.status(403).build();
        }
        return serviceCategoryRepository.findById(id)
                .map(existing -> {
                    if (body.getName() != null) existing.setName(body.getName());
                    if (body.getDescription() != null) existing.setDescription(body.getDescription());
                    return ResponseEntity.ok(DtoMapper.toDto(serviceCategoryRepository.save(existing)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (currentUserService.getCurrentUserCompanyIdIfCompanyAdmin() != null) {
            return ResponseEntity.status(403).build();
        }
        if (!serviceCategoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        serviceCategoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
