#!/bin/bash
# Copyright 2018 Julien Ponge
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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
