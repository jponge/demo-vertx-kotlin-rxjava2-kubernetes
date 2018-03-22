#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

eval $(minikube docker-env)

Services=('temperature-service' 'temperature-gateway' 'zlack' 'temperature-to-zlack-service')

for service in "${Services[@]}"; do
  cd $service
  docker build -t $service:latest .
  cd ..
done

K8sResources=('mongo' 'zlack' 'temperature-service-1' 'temperature-service-2' 'temperature-service-3' 'temperature-gateway' 'temperature-to-zlack-service')

for yaml in "${K8sResources[@]}"; do
  kubectl apply -f kubernetes/${yaml}.yaml
done
