Start Minikube, and jump into its Docker environment:

    minikube start --vm-driver virtualbox
    eval $(minikube docker-env)

Lovely UI:

    minikube dashboard

Useful addons:

    minikube addons list
    minikube addons enable heapster
    minikube addons enable freshpod

Get to an exposed service:

    minikube service --url name

Resources management:

    kubectl apply -f file.yaml
    kubectl logs pod
    kubectl get services
    kubectl delete service foo-service

Bye-bye:

    minikube stop
    eval $(minikube docker-env -u)
        
    minikube delete
