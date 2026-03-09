package com.renx.mg.request.controller.api;

import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.dto.ServiceSubCategoryDTO;
import com.renx.mg.request.model.ServiceSubCategory;
import com.renx.mg.request.repository.ServiceSubCategoryRepository;
import com.renx.mg.request.security.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/service-sub-categories")
public class ServiceSubCategoryApiController {

    private final ServiceSubCategoryRepository serviceSubCategoryRepository;
    private final CurrentUserService currentUserService;

    public ServiceSubCategoryApiController(ServiceSubCategoryRepository serviceSubCategoryRepository, CurrentUserService currentUserService) {
        this.serviceSubCategoryRepository = serviceSubCategoryRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<ServiceSubCategoryDTO> list(@RequestParam(required = false) Long serviceCategoryId) {
        if (serviceCategoryId != null) {
            return serviceSubCategoryRepository.findByServiceCategoryId(serviceCategoryId).stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());
        }
        return serviceSubCategoryRepository.findAll().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceSubCategoryDTO> get(@PathVariable Long id) {
        return serviceSubCategoryRepository.findById(id)
                .map(DtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ServiceSubCategoryDTO> create(@RequestBody ServiceSubCategory subCategory) {
        if (currentUserService.getCurrentUserCompanyIdIfCompanyAdmin() != null) {
            return ResponseEntity.status(403).build();
        }
        ServiceSubCategory saved = serviceSubCategoryRepository.save(subCategory);
        return ResponseEntity.status(201).body(DtoMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceSubCategoryDTO> update(@PathVariable Long id, @RequestBody ServiceSubCategory body) {
        if (currentUserService.getCurrentUserCompanyIdIfCompanyAdmin() != null) {
            return ResponseEntity.status(403).build();
        }
        return serviceSubCategoryRepository.findById(id)
                .map(existing -> {
                    if (body.getName() != null) existing.setName(body.getName());
                    if (body.getDescription() != null) existing.setDescription(body.getDescription());
                    if (body.getServiceCategoryId() != null) existing.setServiceCategoryId(body.getServiceCategoryId());
                    return ResponseEntity.ok(DtoMapper.toDto(serviceSubCategoryRepository.save(existing)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (currentUserService.getCurrentUserCompanyIdIfCompanyAdmin() != null) {
            return ResponseEntity.status(403).body((Void) null);
        }
        if (!serviceSubCategoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        serviceSubCategoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
