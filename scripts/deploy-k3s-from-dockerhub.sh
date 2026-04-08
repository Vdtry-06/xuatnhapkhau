#!/usr/bin/env bash
set -euo pipefail

DOCKERHUB_USER="${1:-${DOCKERHUB_USER:-vdtry06}}"
TAG="${2:-${TAG:-v2026.04.08-r1}}"
NAMESPACE="${3:-${NAMESPACE:-default}}"

echo "Deploying to namespace: ${NAMESPACE}"
echo "Using images: docker.io/${DOCKERHUB_USER}/*:${TAG}"

kubectl apply -f k8s/configmaps/ -n "${NAMESPACE}"
kubectl apply -f k8s/secrets/ -n "${NAMESPACE}"
kubectl apply -f k8s/deployments/ -n "${NAMESPACE}"
kubectl apply -f k8s/services/ -n "${NAMESPACE}"

kubectl set image deployment/auth-service auth-service="docker.io/${DOCKERHUB_USER}/auth-service:${TAG}" -n "${NAMESPACE}"
kubectl set image deployment/api-gateway api-gateway="docker.io/${DOCKERHUB_USER}/api-gateway:${TAG}" -n "${NAMESPACE}"
kubectl set image deployment/agent-service agent-service="docker.io/${DOCKERHUB_USER}/agent-service:${TAG}" -n "${NAMESPACE}"
kubectl set image deployment/product-service product-service="docker.io/${DOCKERHUB_USER}/product-service:${TAG}" -n "${NAMESPACE}"
kubectl set image deployment/supplier-service supplier-service="docker.io/${DOCKERHUB_USER}/supplier-service:${TAG}" -n "${NAMESPACE}"
kubectl set image deployment/order-service order-service="docker.io/${DOCKERHUB_USER}/order-service:${TAG}" -n "${NAMESPACE}"
kubectl set image deployment/payment-service payment-service="docker.io/${DOCKERHUB_USER}/payment-service:${TAG}" -n "${NAMESPACE}"
kubectl set image deployment/frontend frontend="docker.io/${DOCKERHUB_USER}/frontend:${TAG}" -n "${NAMESPACE}"

for d in auth-service api-gateway agent-service product-service supplier-service order-service payment-service frontend; do
  kubectl rollout status "deployment/${d}" -n "${NAMESPACE}" --timeout=180s
done

echo "Deploy complete."
kubectl get pods -n "${NAMESPACE}" -o wide
