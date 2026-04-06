#!/bin/bash
# ─────────────────────────────────────────────────────────────
# SmartBank Cleanup Script
# Run this to DELETE everything and stop AWS charges
# ─────────────────────────────────────────────────────────────

echo "⚠️  WARNING: This will delete all AWS resources!"
echo "Press Ctrl+C to cancel, or wait 5 seconds to continue..."
sleep 5

echo "🗑️  Deleting Kubernetes resources..."
kubectl delete namespace smartbank

echo "🗑️  Deleting RDS instance..."
aws rds delete-db-instance \
  --db-instance-identifier smartbank-db \
  --skip-final-snapshot \
  --region ap-south-1

echo "🗑️  Deleting EKS cluster..."
eksctl delete cluster -f cluster.yml

echo "✅ All resources deleted. No more AWS charges!"