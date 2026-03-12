-- Asignación de aprobadores por company (toda la empresa) o por site.
-- scope_type: 0 = aprobador de toda la company, 1 = aprobador solo del site.
-- Cuando scope_type = 0, site_id debe ser NULL. Cuando scope_type = 1, site_id es obligatorio.
CREATE TABLE request_approver (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    scope_type TINYINT NOT NULL COMMENT '0=COMPANY, 1=SITE',
    site_id BIGINT NULL,
    CONSTRAINT fk_request_approver_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_request_approver_company FOREIGN KEY (company_id) REFERENCES company(id),
    CONSTRAINT fk_request_approver_site FOREIGN KEY (site_id) REFERENCES site(id),
    UNIQUE KEY uk_ra_scope (user_id, company_id, scope_type, site_id)
);

-- Backfill: asignar como aprobadores a nivel COMPANY a todos los usuarios con perfil Company Admin (idempotente).
INSERT INTO request_approver (user_id, company_id, scope_type, site_id)
SELECT u.id, c.company_id, 0, NULL
FROM users u
INNER JOIN customer c ON u.customer_id = c.id
WHERE u.profile_id = 3
  AND c.company_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM request_approver ra
    WHERE ra.user_id = u.id AND ra.company_id = c.company_id AND ra.scope_type = 0 AND ra.site_id IS NULL
  );
