# Demonstration of Eclipse Vert.x, Kotlin, RxJava2 and Kubernetes

This repository contains several reactive services to be deployed in Kubernetes.

1. [temperature-service](temperature-service) is a service simulating a temperature sensor.
   This is basic Vert.x with callbacks, multiple verticles and communication over the event bus.
   _(Vert.x web, Kotlin support)_
2. [temperature-gateway](temperature-gateway) is a service that aggregates temperature data from the available temperature services.
   RxJava2/Kotlin simplifies the reasoning for doing concurrent HTTP requests.
   _(Vert.x web / web client / service discovery, RxJava2)_
3. [zlack](zlack) is a chat application, exposing an API and a reactive VueJS interface.
   It provides real-time message deliveries and shows how the Vert.x event bus can be extended to client-side applications and offer a unified message-passing programming model.
   _(Vert.x web / web client / SockJS event bus bridge / RxJava 2 / MongoDB / VueJS)_ 
4. [temperature-to-zlack-service](temperature-to-zlack-service) is a service that gathers all temperatures from the gateway, and notifies in the Zlack chat of all sensors having a temperature above a threshold.
   It uses Kotlin coroutines to show how a more traditional _"synchronous-style"_ programming model can be used to coordinate operations.
   _(Vert.x web client / Kotlin coroutines / minimal liveness reporting)_

The [kubernetes](kubernetes) folder contains resource descriptors and notes for Kubernetes and `minikube`.

## Building

Building all services should be as simple as:

    ./gradlew assemble

While developing a Vert.x service you can have live-reload, as in:

    ./gradlew :temperature-gateway:vertxRun

# Deploying

_The following assumes a local testing environment with `minikube`._

Building all Docker images and creating Kubernetes resources can then be done using:

    ./deploy-to-kube.sh

...or calling the Gradle task that delegates to this script:

    ./gradlew deployToKube

_If you are not deploying to `minikube`:_

1. all sub-projects have corresponding `Dockerfile` files, and
2. all Kubernetes resource files are in [kubernetes/](kubernetes).

## Legal stuff

This was originally written by [Julien Ponge](https://julien.ponge.org/) and is distributed under the terms of the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt) 

    Copyright 2018 Julien Ponge
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## Contributing

Contributions are welcome, please use GitHub pull requests!
