#!/bin/bash
# Rellena la BD de test con datos demo para explorar dashboards.
# Requiere: backend levantado con perfil test (mvn spring-boot:run -Dspring-boot.run.profiles=test)
# No afecta JUnit (reqtest_) ni E2E (e2e_). Los datos demo usan prefijo demo_.

API_URL="${API_URL:-http://localhost:8080}"

echo "Obteniendo token..."
TOKEN=$(curl -s -X POST "${API_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "Error: No se pudo obtener el token. ¿Backend levantado con perfil test?"
  exit 1
fi

echo "Sembrando datos demo..."
RESP=$(curl -s -w "\n%{http_code}" -X POST "${API_URL}/api/test/seed-demo" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${TOKEN}")

HTTP_CODE=$(echo "$RESP" | tail -n1)
BODY=$(echo "$RESP" | sed '$d')

if [ "$HTTP_CODE" = "200" ]; then
  echo "OK: Datos demo cargados. Navega al dashboard para explorar."
else
  echo "Error HTTP $HTTP_CODE: $BODY"
  exit 1
fi
