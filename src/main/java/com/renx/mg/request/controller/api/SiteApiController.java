package com.renx.mg.request.controller.api;

import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.dto.SiteDTO;
import com.renx.mg.request.model.Site;
import com.renx.mg.request.repository.SiteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sites")
public class SiteApiController {

    private final SiteRepository siteRepository;

    public SiteApiController(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @GetMapping
    public List<SiteDTO> list(@RequestParam(required = false) Long companyId) {
        if (companyId != null) {
            return siteRepository.findByCompanyId(companyId).stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());
        }
        return siteRepository.findAll().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteDTO> get(@PathVariable Long id) {
        return siteRepository.findById(id)
                .map(DtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public SiteDTO create(@RequestBody Site site) {
        Site saved = siteRepository.save(site);
        return DtoMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteDTO> update(@PathVariable Long id, @RequestBody Site body) {
        return siteRepository.findById(id)
                .map(existing -> {
                    if (body.getName() != null) existing.setName(body.getName());
                    if (body.getDescription() != null) existing.setDescription(body.getDescription());
                    if (body.getCompanyId() != null) existing.setCompanyId(body.getCompanyId());
                    return ResponseEntity.ok(DtoMapper.toDto(siteRepository.save(existing)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!siteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        siteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
