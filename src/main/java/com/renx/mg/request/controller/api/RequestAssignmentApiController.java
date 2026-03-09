package com.renx.mg.request.controller.api;

import com.renx.mg.request.dto.DtoMapper;
import com.renx.mg.request.dto.RequestAssignmentDTO;
import com.renx.mg.request.model.Request;
import com.renx.mg.request.model.RequestAssignment;
import com.renx.mg.request.model.RequestStatusType;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.RequestAssignmentRepository;
import com.renx.mg.request.repository.RequestRepository;
import com.renx.mg.request.repository.UserRepository;
import com.renx.mg.request.security.CurrentUserService;
import com.renx.mg.request.service.RequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/request-assignments")
public class RequestAssignmentApiController {

    private final RequestAssignmentRepository requestAssignmentRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final RequestService requestService;

    public RequestAssignmentApiController(RequestAssignmentRepository requestAssignmentRepository,
                                          RequestRepository requestRepository,
                                          UserRepository userRepository,
                                          CurrentUserService currentUserService,
                                          RequestService requestService) {
        this.requestAssignmentRepository = requestAssignmentRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.requestService = requestService;
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<RequestAssignmentDTO> getByRequestId(@PathVariable Long requestId) {
        return Optional.ofNullable(requestAssignmentRepository.findByRequestId(requestId))
                .map(DtoMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RequestAssignmentDTO> assign(@RequestBody AssignRequest body) {
        User current = currentUserService.getCurrentUser();
        if (current == null) return ResponseEntity.status(403).build();
        Request request = requestRepository.findById(body.requestId).orElse(null);
        User assignedUser = userRepository.findById(body.userId).orElse(null);
        if (request == null || assignedUser == null) {
            return ResponseEntity.badRequest().build();
        }
        RequestAssignment assignment = new RequestAssignment();
        assignment.setRequestId(body.requestId);
        assignment.setUserId(assignedUser.getId());
        assignment = requestAssignmentRepository.save(assignment);
        requestService.changeRequestStatus(body.requestId, RequestStatusType.ASSIGNED, current.getId(), null, null);
        requestService.sendEmailOnAssign(body.requestId, assignedUser.getId());
        return ResponseEntity.ok(DtoMapper.toDto(assignment));
    }

    public static class AssignRequest {
        public Long requestId;
        public Long userId;
    }
}
