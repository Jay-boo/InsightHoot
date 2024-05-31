#!/bin/bash

cd ..
echo "------------------------"
echo "K8s Cluster init ..."
kind create cluster --name insight-hoot --config ./kind/k8s_config/kind-config.yaml
echo "Deploying Default Namespace: Zookeeper, Kafka, Kafka Connect, Kafka UI ..."
kubectl apply -f ./kind/k8s_config/kafka/
kubectl apply -f ./kind/k8s_config/django/

echo "------------------------"
echo "Building images producer"
echo "Pass"

echo "------------------------"
echo "Install helm Chart Spark operator"
echo "Pass"
helm repo add spark-operator https://kubeflow.github.io/spark-operator
helm install my-release spark-operator/spark-operator --version 1.2.7 --namespace spark-operator --create-namespace --set webhook.enable=true --debug



echo "------------------------"
echo "Spark Application"
kubectl apply -f ./kind/k8s_config/spark-pi.yaml
