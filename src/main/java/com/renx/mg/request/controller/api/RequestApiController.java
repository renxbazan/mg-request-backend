package com.renx.mg.request.controller.api;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.dto.RequestCreateDTO;
import com.renx.mg.request.dto.RequestDTO;
import com.renx.mg.request.dto.RequestHistoryDTO;
import com.renx.mg.request.model.Request;
import com.renx.mg.request.model.RequestHistory;
import com.renx.mg.request.model.RequestAssignment;
import com.renx.mg.request.model.RequestStatusType;
import com.renx.mg.request.model.Site;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.RequestAssignmentRepository;
import com.renx.mg.request.repository.RequestHistoryRepository;
import com.renx.mg.request.repository.RequestRepository;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.security.CurrentUserService;
import com.renx.mg.request.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.renx.mg.request.model.Customer;

@RestController
@RequestMapping("/api/requests")
public class RequestApiController {

    private final RequestRepository requestRepository;
    private final RequestAssignmentRepository requestAssignmentRepository;
    private final RequestHistoryRepository requestHistoryRepository;
    private final RequestService requestService;
    private final CurrentUserService currentUserService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    public RequestApiController(RequestRepository requestRepository,
                                RequestAssignmentRepository requestAssignmentRepository,
                                RequestHistoryRepository requestHistoryRepository,
                                RequestService requestService,
                                CurrentUserService currentUserService,
                                CustomerRepository customerRepository,
                                UserRepository userRepository,
                                SiteRepository siteRepository) {
        this.requestRepository = requestRepository;
        this.requestAssignmentRepository = requestAssignmentRepository;
        this.requestHistoryRepository = requestHistoryRepository;
        this.requestService = requestService;
        this.currentUserService = currentUserService;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
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
    public List<RequestDTO> list() {
        User current = currentUserService.getCurrentUser();
        if (current == null) {
            return List.of();
        }
        List<Request> list;
        if (Constants.SUPER_ADMIN_PROFILE_ID.equals(current.getProfileId())) {
            list = requestRepository.findTop100ByOrderByIdDesc();
        } else if (Constants.COMPANY_ADMIN_PROFILE_ID.equals(current.getProfileId()) && current.getCustomerId() != null) {
            Long companyId = customerRepository.findById(current.getCustomerId()).map(c -> c.getCompanyId()).orElse(null);
            if (companyId != null) {
                list = requestRepository.findTop100BySiteCompanyIdOrderByIdDesc(companyId);
            } else {
                list = List.of();
            }
        } else {
            list = requestRepository.findByUserId(current.getId());
        }
        List<Long> requestIds = list.stream().map(Request::getId).toList();
        Map<Long, String> assignedMap = buildAssignedStaffNameMap(requestIds);
        return list.stream().map(r -> {
            RequestDTO dto = DtoMapper.toDto(r);
            DtoMapper.withAssignedStaffName(dto, assignedMap.get(r.getId()));
            enrichWithRateInfo(dto, r, current);
            return dto;
        }).collect(Collectors.toList());
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
                    if (current != null) enrichWithRateInfo(dto, r, current);
                    List<RequestHistory> historyList = requestHistoryRepository.findByRequestIdOrderByCreateDateDesc(id);
                    dto.setHistory(mapHistoryToDtos(historyList));
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
