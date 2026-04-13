# ═══════════════════════════════════════════════════════════════
# SmartBank — Minikube Deploy Script (Windows PowerShell)
# Run from your project root: .\k8s\deploy.ps1
# ═══════════════════════════════════════════════════════════════

Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "   SmartBank — Minikube Deployment" -ForegroundColor Cyan
Write-Host "================================================`n" -ForegroundColor Cyan

# ── STEP 1: Start Minikube ────────────────────
Write-Host "[1/8] Starting Minikube with Docker driver..." -ForegroundColor Yellow
minikube start --driver=docker --cpus=4 --memory=6144 --disk-size=20g
if ($LASTEXITCODE -ne 0) {
  Write-Host "❌ Minikube failed. Make sure Docker Desktop is running!" -ForegroundColor Red
  exit 1
}
Write-Host "✅ Minikube started`n" -ForegroundColor Green

# ── STEP 2: Enable addons ─────────────────────
Write-Host "[2/8] Enabling addons..." -ForegroundColor Yellow
minikube addons enable metrics-server
Write-Host "✅ Addons enabled`n" -ForegroundColor Green

# ── STEP 3: Namespace ─────────────────────────
Write-Host "[3/8] Creating namespace..." -ForegroundColor Yellow
kubectl apply -f k8s/namespace.yml
Write-Host "✅ Namespace ready`n" -ForegroundColor Green

# ── STEP 4: Secrets ───────────────────────────
Write-Host "[4/8] Applying secrets..." -ForegroundColor Yellow
kubectl apply -f k8s/secrets.yml
Write-Host "✅ Secrets applied`n" -ForegroundColor Green

# ── STEP 5: Database ──────────────────────────
Write-Host "[5/8] Deploying MySQL..." -ForegroundColor Yellow
kubectl apply -f k8s/database/mysql.yml
Write-Host "   Waiting for MySQL pod to be ready (~60s)..."
kubectl wait --for=condition=ready pod -l app=smartbank-db -n smartbank --timeout=120s
Write-Host "✅ MySQL ready`n" -ForegroundColor Green

# ── STEP 6: Backend + Frontend ────────────────
Write-Host "[6/8] Deploying Backend and Frontend..." -ForegroundColor Yellow
kubectl apply -f k8s/backend/configmap.yml
kubectl apply -f k8s/backend/deployment.yml
kubectl apply -f k8s/backend/service.yml
kubectl apply -f k8s/frontend/deployment.yml
kubectl apply -f k8s/frontend/service.yml
Write-Host "   Waiting for backend (~90s)..."
kubectl rollout status deployment/smartbank-backend -n smartbank --timeout=180s
kubectl rollout status deployment/smartbank-frontend -n smartbank --timeout=60s
Write-Host "✅ Backend + Frontend ready`n" -ForegroundColor Green

# ── STEP 7: Monitoring ────────────────────────
Write-Host "[7/8] Deploying Prometheus + Grafana..." -ForegroundColor Yellow
kubectl apply -f k8s/monitoring/prometheus/configmap.yml
kubectl apply -f k8s/monitoring/prometheus/deployment.yml
kubectl apply -f k8s/monitoring/grafana/datasource.yml
kubectl apply -f k8s/monitoring/grafana/dashboard-provider.yml
kubectl apply -f k8s/monitoring/grafana/dashboard.yml
kubectl apply -f k8s/monitoring/grafana/deployment.yml
kubectl rollout status deployment/prometheus -n smartbank --timeout=60s
kubectl rollout status deployment/grafana -n smartbank --timeout=60s
Write-Host "✅ Monitoring ready`n" -ForegroundColor Green

# ── STEP 8: Print URLs ────────────────────────
Write-Host "[8/8] Getting access URLs..." -ForegroundColor Yellow
$IP = minikube ip
Write-Host "`n================================================" -ForegroundColor Green
Write-Host "   ✅ ALL DEPLOYED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  🌐 Frontend  : http://${IP}:30080" -ForegroundColor White
Write-Host "  ☕ Backend   : http://${IP}:30090/actuator/health" -ForegroundColor White
Write-Host "  📊 Prometheus: http://${IP}:30091" -ForegroundColor White
Write-Host "  📈 Grafana   : http://${IP}:30030" -ForegroundColor White
Write-Host "     Login: admin / smartbank123" -ForegroundColor Gray
Write-Host ""
Write-Host "  💡 To open services in browser:" -ForegroundColor Yellow
Write-Host "     minikube service smartbank-frontend-service -n smartbank" -ForegroundColor Gray
Write-Host "     minikube service prometheus-service -n smartbank" -ForegroundColor Gray
Write-Host "     minikube service grafana-service -n smartbank" -ForegroundColor Gray
Write-Host "================================================`n" -ForegroundColor Green
