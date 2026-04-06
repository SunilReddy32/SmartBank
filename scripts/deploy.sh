#!/bin/bash
# ─────────────────────────────────────────────────────────────
# SmartBank Deploy Script
# Run this every time you want to deploy a new version
# ─────────────────────────────────────────────────────────────

echo "🚀 Deploying SmartBank to Kubernetes..."

# Apply all configs
kubectl apply -f k8s/namespace.yml
kubectl apply -f k8s/secrets.yml
kubectl apply -f k8s/backend/configmap.yml
kubectl apply -f k8s/backend/deployment.yml
kubectl apply -f k8s/backend/service.yml
kubectl apply -f k8s/frontend/deployment.yml
kubectl apply -f k8s/frontend/service.yml
kubectl apply -f k8s/monitoring/prometheus/configmap.yml
kubectl apply -f k8s/monitoring/prometheus/deployment.yml
kubectl apply -f k8s/monitoring/grafana/datasource.yml
kubectl apply -f k8s/monitoring/grafana/deployment.yml

echo ""
echo "⏳ Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod -l app=smartbank-backend -n smartbank --timeout=120s
kubectl wait --for=condition=ready pod -l app=smartbank-frontend -n smartbank --timeout=60s

echo ""
echo "✅ All pods are running!"
echo ""
echo "📊 Pod status:"
kubectl get pods -n smartbank

echo ""
echo "🌐 Services:"
kubectl get services -n smartbank

echo ""
echo "🔗 Your app URL:"
kubectl get service smartbank-frontend-service -n smartbank \
  -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
echo ""