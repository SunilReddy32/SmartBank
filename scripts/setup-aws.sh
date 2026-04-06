#!/bin/bash
# ─────────────────────────────────────────────────────────────
# SmartBank AWS Setup Script
# Run this step by step — do NOT run all at once
# Each section has instructions on what to do
# ─────────────────────────────────────────────────────────────

set -e   # stop on any error

echo "🚀 SmartBank AWS Setup"
echo "======================"

# ── STEP 1: Configure AWS CLI ─────────────────────────────────
echo ""
echo "STEP 1: Configure AWS credentials"
echo "Run: aws configure"
echo "Enter your Access Key ID, Secret Access Key, region: ap-south-1"

# ── STEP 2: Create EKS Cluster ───────────────────────────────
echo ""
echo "STEP 2: Create EKS Cluster (~15 minutes)"
echo "Run: eksctl create cluster -f cluster.yml"
echo "⚠️  This takes 15-20 minutes. Go get a coffee ☕"

# ── STEP 3: Verify cluster connection ────────────────────────
echo ""
echo "STEP 3: Verify kubectl can talk to your cluster"
echo "Run: kubectl get nodes"
echo "You should see 2 nodes with status: Ready"

# ── STEP 4: Create RDS MySQL ──────────────────────────────────
echo ""
echo "STEP 4: Create RDS MySQL database"
echo "Run this AWS CLI command:"
echo ""
echo 'aws rds create-db-instance \'
echo '  --db-instance-identifier smartbank-db \'
echo '  --db-instance-class db.t3.micro \'
echo '  --engine mysql \'
echo '  --engine-version 8.0 \'
echo '  --master-username smartbank_user \'
echo '  --master-user-password YourStrongPassword123 \'
echo '  --allocated-storage 20 \'
echo '  --db-name smartbank \'
echo '  --publicly-accessible \'
echo '  --region ap-south-1'
echo ""
echo "⚠️  Wait ~10 minutes for RDS to start"
echo "Then get the endpoint:"
echo 'aws rds describe-db-instances --db-instance-identifier smartbank-db --query "DBInstances[0].Endpoint.Address" --output text'

# ── STEP 5: Update deployment with RDS endpoint ───────────────
echo ""
echo "STEP 5: Update k8s/backend/deployment.yml"
echo "Replace YOUR_RDS_ENDPOINT with the endpoint from Step 4"
echo "Replace YOUR_DOCKERHUB_USERNAME with your Docker Hub username"

# ── STEP 6: Deploy to Kubernetes ─────────────────────────────
echo ""
echo "STEP 6: Deploy everything to Kubernetes"
echo "Run: kubectl apply -f k8s/namespace.yml"
echo "Run: kubectl apply -f k8s/secrets.yml"
echo "Run: kubectl apply -f k8s/backend/"
echo "Run: kubectl apply -f k8s/frontend/"
echo "Run: kubectl apply -f k8s/monitoring/"

# ── STEP 7: Get public URL ────────────────────────────────────
echo ""
echo "STEP 7: Get your app's public URL"
echo "Run: kubectl get service smartbank-frontend-service -n smartbank"
echo "Look for the EXTERNAL-IP column — that's your app URL!"
echo ""
echo "✅ Setup complete!"