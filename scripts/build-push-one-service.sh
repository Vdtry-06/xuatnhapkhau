#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 <service_name> [tag] [dockerhub_user]"
  echo "Example: $0 order-service v2026.04.08-r2 vdtry06"
  exit 1
fi

SERVICE="$1"
TAG="${2:-${TAG:-v2026.04.08-r1}}"
DOCKERHUB_USER="${3:-${DOCKERHUB_USER:-vdtry06}}"
PLATFORM="${PLATFORM:-linux/amd64}"

case "${SERVICE}" in
  auth-service|api-gateway|agent-service|product-service|supplier-service|order-service|payment-service|frontend)
    ;;
  *)
    echo "Invalid service: ${SERVICE}"
    exit 1
    ;;
esac

IMAGE="docker.io/${DOCKERHUB_USER}/${SERVICE}:${TAG}"
LATEST_IMAGE="docker.io/${DOCKERHUB_USER}/${SERVICE}:latest"

echo "Building and pushing ${SERVICE}"
echo "Image tags: ${IMAGE}, ${LATEST_IMAGE}"

docker buildx build \
  --platform "${PLATFORM}" \
  -t "${IMAGE}" \
  -t "${LATEST_IMAGE}" \
  --push \
  "./${SERVICE}"

echo "Done: ${SERVICE}"
