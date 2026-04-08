#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${1:-default}"
TAIL="${2:-300}"
OUT_DIR="${3:-logs}"

SERVICES=(
  auth-service
  api-gateway
  agent-service
  product-service
  supplier-service
  order-service
  payment-service
  frontend
  mysql
)

mkdir -p "${OUT_DIR}"
STAMP="$(date +%Y%m%d-%H%M%S)"
TARGET_DIR="${OUT_DIR}/${STAMP}"
mkdir -p "${TARGET_DIR}"

echo "Collecting logs to ${TARGET_DIR}"

for svc in "${SERVICES[@]}"; do
  echo "=> ${svc}"
  kubectl logs "deployment/${svc}" -n "${NAMESPACE}" --tail="${TAIL}" > "${TARGET_DIR}/${svc}.log" 2>&1 || true
done

echo "Done. Log bundle: ${TARGET_DIR}"
