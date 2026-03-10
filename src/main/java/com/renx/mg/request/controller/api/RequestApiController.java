package com.renx.mg.request.controller.api;

import com.renx.mg.request.common.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.dto.RequestAttachmentDTO;
import com.renx.mg.request.dto.RequestCreateDTO;
import com.renx.mg.request.dto.RequestDTO;
import com.renx.mg.request.dto.RequestHistoryDTO;
import com.renx.mg.request.model.Request;
import com.renx.mg.request.model.RequestAttachment;
import com.renx.mg.request.model.RequestHistory;
import com.renx.mg.request.model.RequestAssignment;
import com.renx.mg.request.model.RequestStatusType;
import com.renx.mg.request.model.Site;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.RequestAssignmentRepository;
import com.renx.mg.request.repository.RequestHistoryRepository;
import com.renx.mg.request.repository.RequestAttachmentRepository;
import com.renx.mg.request.repository.RequestRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.security.CurrentUserService;
import com.renx.mg.request.service.AttachmentStorageService;
import com.renx.mg.request.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.renx.mg.request.model.Customer;
import com.renx.mg.request.dto.PageResult;
import com.renx.mg.request.repository.RequestSpecifications;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@RestController
@RequestMapping("/api/requests")
public class RequestApiController {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final RequestRepository requestRepository;
    private final RequestAssignmentRepository requestAssignmentRepository;
    private final RequestHistoryRepository requestHistoryRepository;
    private final RequestAttachmentRepository requestAttachmentRepository;
    private final RequestService requestService;
    private final AttachmentStorageService attachmentStorageService;
    private final CurrentUserService currentUserService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final ObjectMapper objectMapper;

    @Value("${mg.attachments.max-file-size-bytes:2097152}")
    private long maxFileSizeBytes;
    @Value("${mg.attachments.max-files-per-request:5}")
    private int maxFilesPerRequest;

    public RequestApiController(RequestRepository requestRepository,
                                RequestAssignmentRepository requestAssignmentRepository,
                                RequestHistoryRepository requestHistoryRepository,
                                RequestAttachmentRepository requestAttachmentRepository,
                                RequestService requestService,
                                AttachmentStorageService attachmentStorageService,
                                CurrentUserService currentUserService,
                                CustomerRepository customerRepository,
                                UserRepository userRepository,
                                SiteRepository siteRepository,
                                ObjectMapper objectMapper) {
        this.requestRepository = requestRepository;
        this.requestAssignmentRepository = requestAssignmentRepository;
        this.requestHistoryRepository = requestHistoryRepository;
        this.requestAttachmentRepository = requestAttachmentRepository;
        this.requestService = requestService;
        this.attachmentStorageService = attachmentStorageService;
        this.currentUserService = currentUserService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
        this.objectMapper = objectMapper;
    }

    private boolean canCurrentUserRate(User current, Request request) {
        if (current == null || request == null) return false;
        if (current.getId().equals(request.getUserId())) return true;
        if (!Constants.COMPANY_ADMIN_PROFILE_ID.equals(current.getProfileId()) || current.getCustomerId() == null) return false;
        Long requestCompanyId = siteRepository.findById(request.getSiteId()).map(Site::getCompanyId).orElse(null);
        Long userCompanyId = customerRepository.findById(current.getCustomerId()).map(Customer::getCompanyId).orElse(null);
        return requestCompanyId != null && requestCompanyId.equals(userCompanyId);
    }

    private void enrichWithRateInfo(RequestDTO dto, Request request, User current) {
        if (dto == null || current == null) return;
        dto.setCanRate(request.getRequestStatus() == RequestStatusType.DONE && canCurrentUserRate(current, request));
        if (request.getRequestStatus() == RequestStatusType.RATED) {
            requestHistoryRepository.findFirstByRequestIdAndRatingIsNotNullOrderByCreateDateDesc(request.getId())
                    .map(rh -> rh.getRating())
                    .ifPresent(dto::setRating);
        }
    }

    private void enrichWithAttachments(RequestDTO dto, Long requestId) {
        if (dto == null || requestId == null) return;
        List<RequestAttachment> attachments = requestAttachmentRepository.findByRequestIdOrderByCreatedAtAsc(requestId);
        if (attachments.isEmpty()) return;
        List<RequestAttachmentDTO> dtos = attachments.stream().map(a -> {
            RequestAttachmentDTO adto = new RequestAttachmentDTO();
            adto.setId(a.getId());
            adto.setUrl(attachmentStorageService.getPresignedUrl(a.getStorageKey()));
            return adto;
        }).toList();
        dto.setAttachments(dtos);
    }

    private Map<Long, String> buildAssignedStaffNameMap(List<Long> requestIds) {
        if (requestIds == null || requestIds.isEmpty()) return Map.of();
        List<RequestAssignment> assignments = requestAssignmentRepository.findByRequestIdIn(requestIds);
        if (assignments.isEmpty()) return Map.of();
        List<Long> userIds = assignments.stream().map(RequestAssignment::getUserId).distinct().toList();
        Map<Long, User> usersById = userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, String> result = new HashMap<>();
        for (var a : assignments) {
            var user = usersById.get(a.getUserId());
            String name = user != null ? resolveStaffDisplayName(user) : null;
            if (name != null) result.put(a.getRequestId(), name);
        }
        return result;
    }

    private String resolveStaffDisplayName(User user) {
        var customer = user.getCustomer();
        if (customer != null && customer.getFirstName() != null && customer.getLastName() != null) {
            return customer.getFirstName() + " " + customer.getLastName();
        }
        return user.getUsername();
    }

    private List<RequestHistoryDTO> mapHistoryToDtos(List<RequestHistory> historyList) {
        if (historyList == null || historyList.isEmpty()) return List.of();
        List<RequestHistoryDTO> result = new ArrayList<>();
        for (RequestHistory rh : historyList) {
            RequestHistoryDTO dto = new RequestHistoryDTO();
            dto.setId(rh.getId());
            dto.setRequestStatus(rh.getRequestStatus());
            dto.setComments(rh.getComments());
            dto.setRating(rh.getRating());
            dto.setCreateDate(rh.getCreateDate());
            if (rh.getUserId() != null) {
                userRepository.findById(rh.getUserId())
                        .map(this::resolveStaffDisplayName)
                        .ifPresent(dto::setUserName);
            }
            result.add(dto);
        }
        return result;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Object list(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "25") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long companyId) {
        User current = currentUserService.getCurrentUser();
        if (current == null) {
            return new PageResult<RequestDTO>(List.of(), 0, size, 0);
        }
        if (!Constants.SUPER_ADMIN_PROFILE_ID.equals(current.getProfileId()) && !Constants.COMPANY_ADMIN_PROFILE_ID.equals(current.getProfileId())) {
            List<Request> list = requestRepository.findByUserId(current.getId());
            List<Long> requestIds = list.stream().map(Request::getId).toList();
            Map<Long, String> assignedMap = buildAssignedStaffNameMap(requestIds);
            List<RequestDTO> items = list.stream().map(r -> {
                RequestDTO dto = DtoMapper.toDto(r);
                DtoMapper.withAssignedStaffName(dto, assignedMap.get(r.getId()));
                enrichWithRateInfo(dto, r, current);
                return dto;
            }).collect(Collectors.toList());
            return new PageResult<>(items, 0, items.size(), items.size());
        }
        Long filterCompanyId = companyId;
        Long userCompanyId = null;
        if (Constants.COMPANY_ADMIN_PROFILE_ID.equals(current.getProfileId()) && current.getCustomerId() != null) {
            userCompanyId = customerRepository.findById(current.getCustomerId()).map(Customer::getCompanyId).orElse(null);
            if (userCompanyId == null) return new PageResult<RequestDTO>(List.of(), page, size, 0);
            filterCompanyId = userCompanyId;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        RequestStatusType statusEnum = parseStatus(status);
        Specification<Request> spec = Specification
                .where(RequestSpecifications.withStatus(statusEnum))
                .and(RequestSpecifications.withPriority(priority))
                .and(RequestSpecifications.withCompanyId(filterCompanyId));
        var result = requestRepository.findAll(spec, pageable);
        List<Long> requestIds = result.getContent().stream().map(Request::getId).toList();
        Map<Long, String> assignedMap = buildAssignedStaffNameMap(requestIds);
        List<RequestDTO> items = result.getContent().stream().map(r -> {
            RequestDTO dto = DtoMapper.toDto(r);
            DtoMapper.withAssignedStaffName(dto, assignedMap.get(r.getId()));
            enrichWithRateInfo(dto, r, current);
            return dto;
        }).collect(Collectors.toList());
        return new PageResult<>(items, result.getNumber(), result.getSize(), result.getTotalElements());
    }

    private RequestStatusType parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return RequestStatusType.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public List<RequestDTO> myRequests() {
        User current = currentUserService.getCurrentUser();
        if (current == null) return List.of();
        List<Request> list = requestRepository.findByUserId(current.getId());
        List<Long> requestIds = list.stream().map(Request::getId).toList();
        Map<Long, String> assignedMap = buildAssignedStaffNameMap(requestIds);
        return list.stream().map(r -> {
            RequestDTO dto = DtoMapper.toDto(r);
            DtoMapper.withAssignedStaffName(dto, assignedMap.get(r.getId()));
            enrichWithRateInfo(dto, r, current);
            return dto;
        }).collect(Collectors.toList());
    }

    /** Requests assigned to the current user (as worker). */
    @GetMapping("/assigned")
    @Transactional(readOnly = true)
    public List<RequestDTO> assignedRequests() {
        User current = currentUserService.getCurrentUser();
        if (current == null) return List.of();
        List<Long> requestIds = requestAssignmentRepository.findByUserId(current.getId()).stream()
                .map(RequestAssignment::getRequestId)
                .toList();
        if (requestIds.isEmpty()) return List.of();
        List<Request> list = requestRepository.findAllById(requestIds);
        Map<Long, String> assignedMap = buildAssignedStaffNameMap(requestIds);
        return list.stream().map(r -> {
            RequestDTO dto = DtoMapper.toDto(r);
            DtoMapper.withAssignedStaffName(dto, assignedMap.get(r.getId()));
            enrichWithRateInfo(dto, r, current);
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<RequestDTO> get(@PathVariable Long id) {
        User current = currentUserService.getCurrentUser();
        return requestRepository.findById(id)
                .map(r -> {
                    RequestDTO dto = DtoMapper.toDto(r);
                    Map<Long, String> assignedMap = buildAssignedStaffNameMap(List.of(id));
                    DtoMapper.withAssignedStaffName(dto, assignedMap.get(id));
                    Optional.ofNullable(requestAssignmentRepository.findByRequestId(id)).ifPresent(a -> dto.setAssignedUserId(a.getUserId()));
                    if (current != null) enrichWithRateInfo(dto, r, current);
                    List<RequestHistory> historyList = requestHistoryRepository.findByRequestIdOrderByCreateDateDesc(id);
                    dto.setHistory(mapHistoryToDtos(historyList));
                    enrichWithAttachments(dto, id);
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RequestDTO> create(@Valid @RequestBody RequestCreateDTO dto) {
        User current = currentUserService.getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(403).build();
        }
        Request request = new Request();
        request.setSiteId(dto.getSiteId());
        request.setServiceCategoryId(dto.getServiceCategoryId());
        request.setServiceSubCategoryId(dto.getServiceSubCategoryId());
        request.setLocation(dto.getLocation());
        request.setDescription(dto.getDescription());
        request.setPriority(dto.getPriority() != null ? dto.getPriority() : "M");
        request.setUserId(current.getId());
        request = requestService.createRequest(request, current.getProfileId());
        RequestDTO respDto = DtoMapper.toDto(request);
        Map<Long, String> assignedMap = buildAssignedStaffNameMap(List.of(request.getId()));
        DtoMapper.withAssignedStaffName(respDto, assignedMap.get(request.getId()));
        enrichWithRateInfo(respDto, request, current);
        enrichWithAttachments(respDto, request.getId());
        return ResponseEntity.ok(respDto);
    }

    @PostMapping(value = "/with-attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createWithAttachments(
            @RequestPart("payload") String payloadJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        User current = currentUserService.getCurrentUser();
        if (current == null) {
            return ResponseEntity.status(403).build();
        }
        RequestCreateDTO dto;
        try {
            dto = objectMapper.readValue(payloadJson, RequestCreateDTO.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid payload JSON"));
        }
        List<MultipartFile> fileList = files != null ? files : List.of();
        if (fileList.size() > maxFilesPerRequest) {
            return ResponseEntity.badRequest().body(Map.of("error", "Too many files; max " + maxFilesPerRequest));
        }
        for (MultipartFile f : fileList) {
            if (f.getSize() > maxFileSizeBytes) {
                return ResponseEntity.badRequest().body(Map.of("error", "File too large: " + f.getOriginalFilename()));
            }
            String ct = f.getContentType();
            if (ct == null || !ALLOWED_IMAGE_TYPES.contains(ct.split(";")[0].trim().toLowerCase())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid content type: " + ct));
            }
        }
        Request request = new Request();
        request.setSiteId(dto.getSiteId());
        request.setServiceCategoryId(dto.getServiceCategoryId());
        request.setServiceSubCategoryId(dto.getServiceSubCategoryId());
        request.setLocation(dto.getLocation());
        request.setDescription(dto.getDescription());
        request.setPriority(dto.getPriority() != null ? dto.getPriority() : "M");
        request.setUserId(current.getId());
        request = requestService.createRequest(request, current.getProfileId());
        Long requestId = request.getId();
        for (MultipartFile f : fileList) {
            if (f.isEmpty()) continue;
            try {
                String contentType = f.getContentType();
                if (contentType == null) contentType = "image/jpeg";
                else contentType = contentType.split(";")[0].trim();
                String key = attachmentStorageService.upload(requestId, f.getInputStream(), contentType, f.getSize());
                if (key != null) {
                    RequestAttachment att = new RequestAttachment();
                    att.setRequestId(requestId);
                    att.setStorageKey(key);
                    att.setContentType(contentType);
                    att.setFileSizeBytes(f.getSize());
                    att.setCreatedAt(new Date());
                    requestAttachmentRepository.save(att);
                } else {
                    org.slf4j.LoggerFactory.getLogger(RequestApiController.class)
                            .warn("Attachment upload skipped (S3 disabled or upload returned null) for requestId={}", requestId);
                }
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(RequestApiController.class)
                        .error("Failed to upload attachment for requestId={}: {}", requestId, e.getMessage(), e);
            }
        }
        RequestDTO respDto = DtoMapper.toDto(request);
        Map<Long, String> assignedMap = buildAssignedStaffNameMap(List.of(requestId));
        DtoMapper.withAssignedStaffName(respDto, assignedMap.get(requestId));
        enrichWithRateInfo(respDto, request, current);
        enrichWithAttachments(respDto, requestId);
        return ResponseEntity.ok(respDto);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<RequestDTO> approve(@PathVariable Long id) {
        User current = currentUserService.getCurrentUser();
        if (current == null) return ResponseEntity.status(403).build();
        return requestRepository.findById(id)
                .map(r -> requestService.changeRequestStatus(id, RequestStatusType.CREATED, current.getId(), null, null))
                .map(r -> {
                    RequestDTO dto = DtoMapper.toDto(r);
                    Map<Long, String> assignedMap = buildAssignedStaffNameMap(List.of(id));
                    DtoMapper.withAssignedStaffName(dto, assignedMap.get(id));
                    enrichWithRateInfo(dto, r, current);
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<RequestDTO> reject(@PathVariable Long id) {
        User current = currentUserService.getCurrentUser();
        if (current == null) return ResponseEntity.status(403).build();
        return requestRepository.findById(id)
                .map(r -> requestService.changeRequestStatus(id, RequestStatusType.REJECTED, current.getId(), null, null))
                .map(r -> {
                    RequestDTO dto = DtoMapper.toDto(r);
                    Map<Long, String> assignedMap = buildAssignedStaffNameMap(List.of(id));
                    DtoMapper.withAssignedStaffName(dto, assignedMap.get(id));
                    enrichWithRateInfo(dto, r, current);
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/attend")
    public ResponseEntity<RequestDTO> attend(@PathVariable Long id) {
        User current = currentUserService.getCurrentUser();
        if (current == null) return ResponseEntity.status(403).build();
        return requestRepository.findById(id)
                .map(r -> requestService.changeRequestStatus(id, RequestStatusType.IN_TRANSIT, current.getId(), null, null))
                .map(r -> {
                    RequestDTO dto = DtoMapper.toDto(r);
                    Map<Long, String> assignedMap = buildAssignedStaffNameMap(List.of(id));
                    DtoMapper.withAssignedStaffName(dto, assignedMap.get(id));
                    enrichWithRateInfo(dto, r, current);
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<RequestDTO> close(@PathVariable Long id, @RequestParam(required = false) String comment) {
        User current = currentUserService.getCurrentUser();
        if (current == null) return ResponseEntity.status(403).build();
        return requestRepository.findById(id)
                .map(r -> requestService.changeRequestStatus(id, RequestStatusType.DONE, current.getId(), comment, null))
                .map(r -> {
                    RequestDTO dto = DtoMapper.toDto(r);
                    Map<Long, String> assignedMap = buildAssignedStaffNameMap(List.of(id));
                    DtoMapper.withAssignedStaffName(dto, assignedMap.get(id));
                    enrichWithRateInfo(dto, r, current);
                    return dto;
                })
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/rate")
    public ResponseEntity<RequestDTO> rate(@PathVariable Long id,
                                           @RequestParam Long rating,
                                           @RequestParam(required = false) String comment) {
        User current = currentUserService.getCurrentUser();
        if (current == null) return ResponseEntity.status(403).build();
        Request request = requestRepository.findById(id).orElse(null);
        if (request == null) return ResponseEntity.notFound().build();
        if (!canCurrentUserRate(current, request)) return ResponseEntity.status(403).build();
        Request updated = requestService.changeRequestStatus(id, RequestStatusType.RATED, current.getId(), comment, rating);
        RequestDTO dto = DtoMapper.toDto(updated);
        Map<Long, String> assignedMap = buildAssignedStaffNameMap(List.of(id));
        DtoMapper.withAssignedStaffName(dto, assignedMap.get(id));
        enrichWithRateInfo(dto, updated, current);
        dto.setRating(rating);
        return ResponseEntity.ok(dto);
    }
}
