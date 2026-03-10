package com.renx.mg.request.repository;

import com.renx.mg.request.model.Request;
import com.renx.mg.request.model.RequestStatusType;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public final class RequestSpecifications {

    private RequestSpecifications() {}

    public static Specification<Request> withCreateDateBetween(Date from, Date to) {
        if (from == null && to == null) return (root, query, cb) -> cb.conjunction();
        if (from == null) return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createDate"), to);
        if (to == null) return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createDate"), from);
        return (root, query, cb) -> cb.between(root.get("createDate"), from, to);
    }

    public static Specification<Request> withStatus(RequestStatusType status) {
        if (status == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("requestStatus"), status);
    }

    public static Specification<Request> withPriority(String priority) {
        if (priority == null || priority.isBlank()) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(cb.upper(root.get("priority")), priority.toUpperCase());
    }

    public static Specification<Request> withCompanyId(Long companyId) {
        if (companyId == null) return (root, query, cb) -> cb.conjunction();
        return (root, query, cb) -> cb.equal(root.get("site").get("companyId"), companyId);
    }
}
