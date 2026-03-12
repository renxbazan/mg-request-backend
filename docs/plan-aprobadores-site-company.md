# Plan: Aprobadores por site/company, formulario por company, reglas de aprobación

## Decisiones confirmadas

- **Site en el request**: Se mantiene **obligatorio** en el formulario y en la validación del backend. No se añade `NOT NULL` en la base de datos (`request.site_id` sigue nullable en DB).
- **Backfill de aprobadores**: Tras crear la tabla `request_approver`, una migración **auto-asigna** como aprobadores a nivel **COMPANY** a todos los usuarios que hoy tienen perfil Company Admin y pertenecen a esa company. Después, desde el sistema se podrá configurar manualmente por site/company.
- **Super Admin en el formulario de request**: Cuando se carguen todos los sites (sin filtrar por company), cada opción del selector se mostrará con etiqueta **"**** — ****"** para evitar ambigüedad cuando haya sites con el mismo nombre. Para ello el backend debe exponer `companyName` en el DTO de sites (o el frontend tendrá que resolverlo con companies).

---

## Resumen del plan

1. **Backend**: Nueva tabla `request_approver` (user_id, company_id, site_id nullable). Servicio y API para listar/añadir/quitar aprobadores por company y por site. Emails y `companyHasApprovers` usan esta tabla. Validación en approve/reject: Company Admin solo puede si está asignado al site o a la company del request; Super Admin siempre puede.
2. **Backfill**: Migración que inserta en `request_approver` una fila (user_id, company_id, site_id NULL) por cada usuario con perfil Company Admin cuyo customer pertenezca a esa company.
3. **Frontend – Formulario de request**:
  - Usuario con `companyId` (Requester, Company Admin, etc.): cargar solo sites de su company (`/api/sites?companyId=...`). Site obligatorio.
  - Super Admin (sin `companyId` o ve todo): cargar todos los sites; en el selector mostrar cada opción como **"Empresa — Site"** (requiere `companyName` en `SiteDTO` o mapeo desde companies en frontend). Site obligatorio.
4. **Frontend – Modal aprobadores**: Consumir GET/POST/DELETE de approvers; UI para asignar por company y por site.

---

## Detalle técnico

### Backend

- **Migración V11**: Crear `request_approver` (id, user_id, company_id, site_id nullable). Índices/unique según diseño. **Segunda migración (o mismo script)**:
  - Insertar en `request_approver` una fila por cada par (user_id, company_id) donde el usuario tiene perfil Company Admin y su customer tiene ese company_id; `site_id = NULL` (aprobador de toda la company).
- **SiteDTO**: Añadir campo `companyName` y rellenarlo al mapear desde `Site` (con join a Company) para que el frontend pueda mostrar "Company — Site" sin otra llamada.
- **RequestApprover** (entidad, repositorio, servicio): Listar por company; añadir/quitar asignación (company-level: site_id null; site-level: site_id no null). Método tipo “userIds que pueden aprobar este request” (por company + site del request).
- **RequestService**: `companyHasApprovers(siteId)` basado en `request_approver`. `sendEmailOnCreate` (PENDING_APPROVAL): obtener aprobadores del request (company + site) y enviar solo a esos.
- **RequestApiController approve/reject**: Si no es Super Admin, comprobar que el usuario actual esté en la lista de aprobadores del request (por site o company); si no, 403.
- **API** `GET|POST|DELETE /api/companies/{id}/approvers` con scope COMPANY/SITE y siteId opcional.

### Frontend

- **RequestForm**: Si `user?.companyId` existe → `catalogsApi.sites(user.companyId)`. Si no (Super Admin) → `catalogsApi.sites()`. En el Autocomplete de sites, si hay `companyName` en cada ítem, usar `getOptionLabel: s => s.companyName ? \`${s.companyName} — ${s.name} : s.name`. Site sigue siendo **required**.
- **CompanyList – Modal aprobadores**: Llamar a la nueva API de approvers; secciones “Aprobadores de toda la empresa” y “Por site”, con añadir/quitar según scope.

---

## Orden de implementación sugerido

1. Migración(es): tabla `request_approver` + backfill.
2. Entidad, repositorio, servicio de aprobadores; añadir `companyName` a SiteDTO y mapeo.
3. RequestService: usar nueva tabla en `companyHasApprovers` y en envío de emails.
4. RequestApiController: validación en approve/reject y endpoints de approvers (si se exponen desde aquí o desde CompanyController).
5. Frontend RequestForm: filtro por companyId y etiqueta "Company — Site" para Super Admin.
6. Frontend modal aprobadores: nueva API y UI por company/site.

