package com.renx.mg.request.controller.api;

import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.dto.ProfileDTO;
import com.renx.mg.request.model.Profile;
import com.renx.mg.request.repository.ProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/profiles")
public class ProfileApiController {

    private final ProfileRepository profileRepository;

    public ProfileApiController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @GetMapping
    public List<ProfileDTO> list() {
        return StreamSupport.stream(profileRepository.findAll().spliterator(), false)
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileDTO> get(@PathVariable Long id) {
        return profileRepository.findById(id)
                .map(DtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ProfileDTO create(@RequestBody Profile profile) {
        Profile saved = profileRepository.save(profile);
        return DtoMapper.toDto(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfileDTO> update(@PathVariable Long id, @RequestBody Profile body) {
        return profileRepository.findById(id)
                .map(existing -> {
                    if (body.getDescription() != null) existing.setDescription(body.getDescription());
                    return ResponseEntity.ok(DtoMapper.toDto(profileRepository.save(existing)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!profileRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        profileRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
