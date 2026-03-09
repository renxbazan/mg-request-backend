# Plan: Fecha/hora del request e historial limpio (SPA)

Frontend: **/Users/renzobazan/mg/mg-request-frontend**  
Backend: este repo (mg-request).

---

## 1. Fecha y hora de la solicitud

- **Backend:** RequestDTO ya tiene `createDate` (Date); en JSON se serializa con hora (ISO-8601). No requiere cambios.
- **Frontend:** En [RequestDetail.tsx](/Users/renzobazan/mg/mg-request-frontend/src/pages/RequestDetail.tsx) ya se muestra con hora (línea 118: `new Date(request.createDate).toLocaleString()`). Opcional: en [RequestList.tsx](/Users/renzobazan/mg/mg-request-frontend/src/pages/RequestList.tsx) cambiar `toLocaleDateString()` por `toLocaleString()` si se quiere ver hora también en el listado.

---

## 2. Backend: exponer historial en la API

### 2.1 RequestHistoryDTO

- Crear **RequestHistoryDTO** en este repo con: `id`, `requestStatus`, `comments`, `rating`, `createDate` (Date). Opcional: `userName` (quien hizo el cambio).

### 2.2 RequestDTO

- En [RequestDTO.java](src/main/java/com/renx/mg/request/dto/RequestDTO.java) añadir:  
  `private List<RequestHistoryDTO> history;`  
  con getter/setter.

### 2.3 RequestHistoryRepository

- En [RequestHistoryRepository.java](src/main/java/com/renx/mg/request/repository/RequestHistoryRepository.java) añadir:  
  `List<RequestHistory> findByRequestIdOrderByCreateDateDesc(Long requestId);`

### 2.4 RequestApiController

- En el endpoint **GET /api/requests/{id}** (get by id): cargar historial con `findByRequestIdOrderByCreateDateDesc(id)`, mapear a `RequestHistoryDTO`, asignar a `dto.setHistory(...)` antes de devolver. No incluir `history` en list/my/assigned para no sobrecargar.

---

## 3. Frontend (mg-request-frontend): historial limpio

### 3.1 Tipos y API

- En [api/requests.ts](/Users/renzobazan/mg/mg-request-frontend/src/api/requests.ts):
  - Definir **RequestHistoryDto** (o similar): `id`, `requestStatus`, `comments`, `rating`, `createDate` (string), opcional `userName`.
  - En **RequestDto** añadir: `history?: RequestHistoryDto[]`.

### 3.2 Vista RequestDetail

- En [RequestDetail.tsx](/Users/renzobazan/mg/mg-request-frontend/src/pages/RequestDetail.tsx):
  - Añadir sección **"Historial de la solicitud"** (o clave i18n tipo `requests.history`).
  - Si `request.history` existe y tiene longitud > 0, mostrar una **timeline vertical** (o tabla clara):
    - Cada entrada: **fecha y hora** (formatear `createDate` con `toLocaleString()`), **estado** (badge usando STATUS_KEYS existentes), **comentario** (si hay), **valoración** (si hay; usar `<StarRating value={...} readOnly />`).
  - Orden: el que devuelve la API (más reciente primero).
  - Estilos: espaciado, badges por estado, sin bloques de texto densos.

### 3.3 i18n

- Añadir clave `requests.history` (o equivalente) en [es.json](/Users/renzobazan/mg/mg-request-frontend/src/i18n/locales/es.json) y en en.json.

---

## 4. Resumen de archivos

| Repo / Ruta | Archivo | Cambio |
|-------------|---------|--------|
| Backend | Nuevo: `dto/RequestHistoryDTO.java` | Campos: id, requestStatus, comments, rating, createDate; opcional userName. |
| Backend | [RequestDTO.java](src/main/java/com/renx/mg/request/dto/RequestDTO.java) | Añadir `List<RequestHistoryDTO> history`. |
| Backend | [RequestHistoryRepository](src/main/java/com/renx/mg/request/repository/RequestHistoryRepository.java) | `findByRequestIdOrderByCreateDateDesc(Long)`. |
| Backend | [RequestApiController](src/main/java/com/renx/mg/request/controller/api/RequestApiController.java) | En GET por id: cargar historial, mapear a DTOs, setear en dto. |
| Frontend | [api/requests.ts](/Users/renzobazan/mg/mg-request-frontend/src/api/requests.ts) | RequestHistoryDto; RequestDto.history. |
| Frontend | [RequestDetail.tsx](/Users/renzobazan/mg/mg-request-frontend/src/pages/RequestDetail.tsx) | Sección historial (timeline o tabla). |
| Frontend | locales es.json / en.json | Clave requests.history. |
