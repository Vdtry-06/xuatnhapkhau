#!/bin/bash
# ============================================================
# deploy-k8s.sh — Build local images và deploy lên K8s
# Cách dùng: ./deploy-k8s.sh [namespace] [registry]
# ============================================================
set -e

NAMESPACE=${1:-default}
REGISTRY=${2:-${REGISTRY:-xnk}}

echo "🚀 Bắt đầu deploy lên K8s namespace: $NAMESPACE"

# Build tất cả Docker images
SERVICES=(auth-service api-gateway agent-service product-service supplier-service order-service payment-service frontend)
for svc in "${SERVICES[@]}"; do
  echo "📦 Build image: $REGISTRY/$svc:latest"
  docker build -t "$REGISTRY/$svc:latest" "./$svc"
done

# Apply ConfigMap và Secret trước
echo "⚙️  Apply ConfigMap và Secret..."
kubectl apply -f k8s/configmaps/ -n "$NAMESPACE"
kubectl apply -f k8s/secrets/    -n "$NAMESPACE"

# Apply Deployments
echo "🔄 Apply Deployments..."
kubectl apply -f k8s/deployments/ -n "$NAMESPACE"

# Apply Services
echo "🌐 Apply Services..."
kubectl apply -f k8s/services/ -n "$NAMESPACE"

echo ""
echo "✅ Deploy hoàn tất!"
echo "   Frontend: http://localhost:30000"
echo "   API Gateway: http://localhost:30080"
echo ""
echo "Xem trạng thái pods:"
kubectl get pods -n "$NAMESPACE"
