#!/usr/bin/env bash
set -euo pipefail

DOCKERHUB_USER="${1:-${DOCKERHUB_USER:-vdtry06}}"
TAG="${2:-${TAG:-v2026.04.08-r1}}"
PLATFORM="${PLATFORM:-linux/amd64}"

SERVICES=(
  auth-service
  api-gateway
  agent-service
  product-service
  supplier-service
  order-service
  payment-service
  frontend
)

echo "Building and pushing images to DockerHub user: ${DOCKERHUB_USER}"
echo "Tag: ${TAG} | Platform: ${PLATFORM}"

for svc in "${SERVICES[@]}"; do
  IMAGE="docker.io/${DOCKERHUB_USER}/${svc}:${TAG}"
  LATEST_IMAGE="docker.io/${DOCKERHUB_USER}/${svc}:latest"
  echo "=> ${svc}"
  docker buildx build \
    --platform "${PLATFORM}" \
    -t "${IMAGE}" \
    -t "${LATEST_IMAGE}" \
    --push \
    "./${svc}"
done

echo "Done. All images were pushed to DockerHub."
