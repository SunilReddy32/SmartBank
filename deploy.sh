#!/bin/bash
echo "Deploying SmartBank to Minikube..."

echo "Step 1: Creating namespace..."
kubectl apply -f k8s/namespace.yml

echo "Step 2: Creating secrets..."
kubectl apply -f k8s/secrets.yml

echo "Step 3: Starting MySQL..."
kubectl apply -f k8s/mysql/mysql.yml

echo "Step 4: Waiting for MySQL to be ready (up to 2 minutes)..."
kubectl wait --for=condition=ready pod \
  -l app=smartbank-mysql \
  -n smartbank \
  --timeout=120s

echo "Step 5: Deploying backend..."
kubectl apply -f k8s/backend/configmap.yml
kubectl apply -f k8s/backend/deployment.yml
kubectl apply -f k8s/backend/service.yml

echo "Step 6: Deploying frontend..."
kubectl apply -f k8s/frontend/deployment.yml
kubectl apply -f k8s/frontend/service.yml

echo "Step 7: Deploying monitoring..."
kubectl apply -f k8s/monitoring/prometheus/configmap.yml
kubectl apply -f k8s/monitoring/prometheus/deployment.yml
kubectl apply -f k8s/monitoring/grafana/datasource.yml
kubectl apply -f k8s/monitoring/grafana/deployment.yml

echo ""
echo "Waiting for all pods to be ready..."
kubectl wait --for=condition=ready pod \
  -l app=smartbank-backend \
  -n smartbank \
  --timeout=180s

echo ""
echo "All pods status:"
kubectl get pods -n smartbank

echo ""
echo "All services:"
kubectl get services -n smartbank

echo ""
echo "Opening app in browser..."
minikube service smartbank-frontend-service -n smartbank