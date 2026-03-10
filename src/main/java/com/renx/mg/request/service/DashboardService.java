package com.renx.mg.request.service;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.model.Customer;
import com.renx.mg.request.model.Request;
import com.renx.mg.request.model.RequestAssignment;
import com.renx.mg.request.model.RequestHistory;
import com.renx.mg.request.model.RequestStatusType;
import com.renx.mg.request.model.User;
import com.renx.mg.request.model.Company;
import com.renx.mg.request.model.Site;
import com.renx.mg.request.repository.CompanyRepository;
import com.renx.mg.request.repository.CustomerRepository;
import com.renx.mg.request.repository.RequestAssignmentRepository;
import com.renx.mg.request.repository.RequestHistoryRepository;
import com.renx.mg.request.repository.RequestRepository;
import com.renx.mg.request.repository.RequestSpecifications;
import com.renx.mg.request.repository.SiteRepository;
import com.renx.mg.request.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final ZoneId ZONE = ZoneId.of(Constants.MG_LOCAL_TIMEZONE);

    private final RequestRepository requestRepository;
    private final RequestAssignmentRepository requestAssignmentRepository;
    private final RequestHistoryRepository requestHistoryRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final CompanyRepository companyRepository;

    public DashboardService(RequestRepository requestRepository,
                            RequestAssignmentRepository requestAssignmentRepository,
                            RequestHistoryRepository requestHistoryRepository,
                            CustomerRepository customerRepository,
                            UserRepository userRepository,
                            SiteRepository siteRepository,
                            CompanyRepository companyRepository) {
        this.requestRepository = requestRepository;
        this.requestAssignmentRepository = requestAssignmentRepository;
        this.requestHistoryRepository = requestHistoryRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
        this.companyRepository = companyRepository;
    }

    public Map<String, Object> getStats(User current, String range) {
        if (current == null) return emptyStats();

        Date[] dateRange = computeDateRange(range);
        Date from = dateRange[0];
        Date to = dateRange[1];

        Specification<Request> baseSpec = buildBaseSpec(current, from, to);
        List<Request> requests = requestRepository.findAll(baseSpec);

        Map<String, Object> result = new HashMap<>();
        result.put("byStatus", countByStatus(requests));
        result.put("byPriority", countByPriority(requests));

        if (Constants.SUPER_ADMIN_PROFILE_ID.equals(current.getProfileId())
                || Constants.WORKER_PROFILE_ID.equals(current.getProfileId())) {
            result.put("ratingsByWorker", computeRatingsByWorker(current, requests));
        }

        if (Constants.SUPER_ADMIN_PROFILE_ID.equals(current.getProfileId())
                || Constants.COMPANY_ADMIN_PROFILE_ID.equals(current.getProfileId())) {
            result.put("byCompany", computeByCompany(current, requests));
        }

        return result;
    }

    private List<Map<String, Object>> computeByCompany(User current, List<Request> requests) {
        if (requests.isEmpty()) return List.of();

        java.util.Set<Long> siteIds = requests.stream()
                .map(Request::getSiteId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        if (siteIds.isEmpty()) return List.of();

        Map<Long, Site> siteMap = siteRepository.findAllById(siteIds).stream()
                .collect(Collectors.toMap(Site::getId, s -> s));
        java.util.Set<Long> companyIds = siteMap.values().stream()
                .map(Site::getCompanyId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Company> companyMap = companyRepository.findAllById(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, c -> c));

        Long filterCompanyId = null;
        if (Constants.COMPANY_ADMIN_PROFILE_ID.equals(current.getProfileId()) && current.getCustomerId() != null) {
            filterCompanyId = customerRepository.findById(current.getCustomerId())
                    .map(Customer::getCompanyId).orElse(null);
        }

        Map<Long, Map<Long, List<Request>>> byCompanySite = new java.util.LinkedHashMap<>();
        for (Request r : requests) {
            if (r.getSiteId() == null) continue;
            Site site = siteMap.get(r.getSiteId());
            if (site == null || site.getCompanyId() == null) continue;
            Long companyId = site.getCompanyId();
            if (filterCompanyId != null && !filterCompanyId.equals(companyId)) continue;
            byCompanySite
                    .computeIfAbsent(companyId, k -> new java.util.LinkedHashMap<>())
                    .computeIfAbsent(r.getSiteId(), k -> new ArrayList<>())
                    .add(r);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, List<Request>>> ce : byCompanySite.entrySet()) {
            Long companyId = ce.getKey();
            Company company = companyMap.get(companyId);
            String companyName = company != null ? company.getName() : "Company " + companyId;

            List<Request> companyRequests = ce.getValue().values().stream().flatMap(List::stream).toList();
            Map<String, Long> companyByStatus = countByStatus(companyRequests);

            List<Map<String, Object>> sites = new ArrayList<>();
            for (Map.Entry<Long, List<Request>> se : ce.getValue().entrySet()) {
                Long siteId = se.getKey();
                List<Request> siteRequests = se.getValue();
                Site site = siteMap.get(siteId);
                String siteName = site != null ? site.getName() : "Site " + siteId;
                Map<String, Object> siteEntry = new HashMap<>();
                siteEntry.put("siteId", siteId);
                siteEntry.put("siteName", siteName);
                siteEntry.put("total", (long) siteRequests.size());
                siteEntry.put("byStatus", countByStatus(siteRequests));
                sites.add(siteEntry);
            }
            sites.sort((a, b) -> Long.compare((Long) b.get("total"), (Long) a.get("total")));

            Map<String, Object> companyEntry = new HashMap<>();
            companyEntry.put("companyId", companyId);
            companyEntry.put("companyName", companyName);
            companyEntry.put("total", (long) companyRequests.size());
            companyEntry.put("byStatus", companyByStatus);
            companyEntry.put("sites", sites);
            result.add(companyEntry);
        }
        result.sort((a, b) -> Long.compare((Long) b.get("total"), (Long) a.get("total")));
        return result;
    }

    private Map<String, Object> emptyStats() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("byStatus", Map.<String, Long>of());
        empty.put("byPriority", Map.<String, Long>of());
        return empty;
    }

    private Specification<Request> buildBaseSpec(User current, Date from, Date to) {
        Specification<Request> spec = RequestSpecifications.withCreateDateBetween(from, to);

        if (Constants.SUPER_ADMIN_PROFILE_ID.equals(current.getProfileId())) {
            return spec;
        }
        if (Constants.COMPANY_ADMIN_PROFILE_ID.equals(current.getProfileId())) {
            if (current.getCustomerId() == null) return spec;
            Long companyId = customerRepository.findById(current.getCustomerId())
                    .map(Customer::getCompanyId).orElse(null);
            if (companyId == null) return spec;
            return spec.and(RequestSpecifications.withCompanyId(companyId));
        }
        if (Constants.REQUESTER_PROFILE_ID.equals(current.getProfileId())) {
            return spec.and((root, query, cb) -> cb.equal(root.get("userId"), current.getId()));
        }
        if (Constants.WORKER_PROFILE_ID.equals(current.getProfileId())) {
            List<Long> assignedIds = requestAssignmentRepository.findByUserId(current.getId()).stream()
                    .map(RequestAssignment::getRequestId).toList();
            if (assignedIds.isEmpty()) return spec.and((root, query, cb) -> cb.disjunction());
            return spec.and((root, query, cb) -> root.get("id").in(assignedIds));
        }
        return spec;
    }

    private Map<String, Long> countByStatus(List<Request> requests) {
        Map<String, Long> counts = new HashMap<>();
        for (RequestStatusType s : RequestStatusType.values()) {
            counts.put(s.name(), 0L);
        }
        for (Request r : requests) {
            if (r.getRequestStatus() != null) {
                counts.merge(r.getRequestStatus().name(), 1L, Long::sum);
            }
        }
        return counts;
    }

    private Map<String, Long> countByPriority(List<Request> requests) {
        Map<String, Long> counts = new HashMap<>();
        for (Request r : requests) {
            String p = r.getPriority();
            if (p == null) p = "unknown";
            else p = p.toUpperCase();
            counts.merge(p, 1L, Long::sum);
        }
        return counts;
    }

    private List<Map<String, Object>> computeRatingsByWorker(User current, List<Request> allRequests) {
        List<Request> ratedRequests = allRequests.stream()
                .filter(r -> r.getRequestStatus() == RequestStatusType.RATED)
                .toList();
        if (ratedRequests.isEmpty()) return List.of();

        List<Long> ratedIds = ratedRequests.stream().map(Request::getId).toList();
        List<RequestAssignment> assignments = requestAssignmentRepository.findByRequestIdIn(ratedIds);
        Map<Long, Long> requestToWorker = assignments.stream()
                .collect(Collectors.toMap(RequestAssignment::getRequestId, RequestAssignment::getUserId, (a, b) -> a));

        Map<Long, Long> requestToRating = new HashMap<>();
        for (Long reqId : ratedIds) {
            requestHistoryRepository.findFirstByRequestIdAndRatingIsNotNullOrderByCreateDateDesc(reqId)
                    .map(RequestHistory::getRating)
                    .ifPresent(rating -> requestToRating.put(reqId, rating));
        }

        Map<Long, List<Long>> workerToRatings = new HashMap<>();
        for (Request r : ratedRequests) {
            Long workerId = requestToWorker.get(r.getId());
            Long rating = requestToRating.get(r.getId());
            if (workerId == null || rating == null) continue;
            if (Constants.WORKER_PROFILE_ID.equals(current.getProfileId()) && !workerId.equals(current.getId())) continue;
            workerToRatings.computeIfAbsent(workerId, k -> new ArrayList<>()).add(rating);
        }

        Set<Long> workerIds = workerToRatings.keySet();
        if (workerIds.isEmpty()) return List.of();

        Map<Long, String> workerNames = resolveWorkerNames(workerIds);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Long workerId : workerIds) {
            List<Long> ratings = workerToRatings.get(workerId);
            long count = ratings.size();
            double avg = ratings.stream().mapToLong(Long::longValue).average().orElse(0);
            Map<String, Object> entry = new HashMap<>();
            entry.put("workerId", workerId);
            entry.put("workerName", workerNames.getOrDefault(workerId, "Worker " + workerId));
            entry.put("count", count);
            entry.put("avgRating", Math.round(avg * 10) / 10.0);
            result.add(entry);
        }
        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));
        return result;
    }

    private Map<Long, String> resolveWorkerNames(Set<Long> userIds) {
        Map<Long, String> names = new HashMap<>();
        List<User> users = userRepository.findAllById(userIds);
        for (User u : users) {
            if (u == null) continue;
            String name;
            if (u.getCustomer() != null) {
                Customer c = u.getCustomer();
                String first = c.getFirstName() != null ? c.getFirstName() : "";
                String last = c.getLastName() != null ? c.getLastName() : "";
                name = (first + " " + last).trim();
            } else {
                name = u.getUsername() != null ? u.getUsername() : "";
            }
            if (name.isEmpty()) name = "Worker " + u.getId();
            names.put(u.getId(), name);
        }
        return names;
    }

    private Date[] computeDateRange(String range) {
        if (range == null || range.isBlank()) range = "this_month";
        LocalDate now = LocalDate.now(ZONE);
        LocalDate from;
        LocalDate to;

        switch (range.toLowerCase()) {
            case "last_month":
                from = now.minusMonths(1).withDayOfMonth(1);
                to = now.withDayOfMonth(1).minusDays(1);
                break;
            case "last_3_months":
                from = now.minusMonths(3);
                to = now;
                break;
            case "last_6_months":
                from = now.minusMonths(6);
                to = now;
                break;
            case "this_year":
                from = now.withDayOfYear(1);
                to = now;
                break;
            case "last_year":
                from = now.minusYears(1).withDayOfYear(1);
                to = now.minusYears(1).withDayOfYear(1).plusYears(1).minusDays(1);
                break;
            case "all":
                return new Date[]{null, null};
            case "this_month":
            default:
                from = now.withDayOfMonth(1);
                to = now;
                break;
        }

        java.util.Date fromDate = Date.from(from.atStartOfDay(ZONE).toInstant());
        java.util.Date toDate = Date.from(to.atTime(LocalTime.MAX).atZone(ZONE).toInstant());
        return new Date[]{fromDate, toDate};
    }
}
