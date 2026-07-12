#!/bin/bash

set -e

echo "========================================="
echo "Building Docker image for minikube..."
echo "========================================="
minikube image build -t auth-service:latest .

echo ""
echo "========================================="
echo "Creating namespace..."
echo "========================================="
kubectl apply -f k8s/namespace.yaml

echo ""
echo "========================================="
echo "Applying ConfigMap and Secret..."
echo "========================================="
kubectl apply -f k8s/configmap.yaml -n online-store
kubectl apply -f k8s/secret.yaml -n online-store

echo ""
echo "========================================="
echo "Deploying PostgreSQL..."
echo "========================================="
kubectl apply -f k8s/postgres-auth-pvc.yaml -n online-store
kubectl apply -f k8s/postgres-auth-deployment.yaml -n online-store
kubectl apply -f k8s/postgres-auth-service.yaml -n online-store

echo ""
echo "Waiting for PostgreSQL to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres-auth --timeout=120s -n online-store

echo ""
echo "========================================="
echo "Deploying auth-service..."
echo "========================================="
kubectl apply -f k8s/auth-service-deployment.yaml -n online-store
kubectl apply -f k8s/auth-service-svc.yaml -n online-store

echo ""
echo "Waiting for auth-service rollout to complete..."
kubectl rollout status deployment/auth-service -n online-store

echo ""
echo "========================================="
echo "Deployment completed successfully!"
echo "========================================="
echo ""
echo "To check logs:"
echo "  kubectl logs -l app=auth-service -n online-store -f"
echo ""
echo "To port-forward and test health endpoint:"
echo "  kubectl port-forward svc/auth-service 8081:8081 -n online-store"
echo ""
echo "Then access health check at:"
echo "  http://localhost:8081/actuator/health"
