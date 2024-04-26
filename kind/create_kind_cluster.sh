#!/bin/bash

echo "------------------------"
echo "K8s Cluster init ..."
kind create cluster --name insight-hoot --config ./k8s_config/kind-config.yaml
echo "Deploying Zookeeper ..."
kubectl apply -f k8s_config/kafka/zookeeper.yaml
echo "Deploying Kafka ..."
kubectl apply -f k8s_config/kafka/kafka.yaml

echo "------------------------"
echo "Building images producer"
echo "Pass"
# cd producer-img
# docker build -t producer-kafka .
# echo "Push Image to cluster"
# kind load docker-image -n insight-hoot producer-kafka producer-kafka
# cd ..


echo "------------------------"
echo "Deploying Kafka-producer.yaml ..."
echo "Pass"
# kubectl apply -f k8s_config/kafka/kafka-producer.yaml

echo "------------------------"
echo "Put Spark image in cluster"
kind load docker-image -n insight-hoot spark:our-own-apache-spark-kb8
kubectl apply -f k8s_config/kafka/spark-sa.yaml


echo "------------------------"
echo "Install helm Chart Spark operator"
echo "Pass"
# helm repo add spark-operator https://kubeflow.github.io/spark-operator
# helm install my-release spark-operator/spark-operator --namespace spark-operator --create-namespace --set webhook.enable=true
#
echo "------------------------"
echo "Kafka Connect Deployment"
kubectl apply -f k8s_config/kafka/kafka-connect.yaml

echo "------------------------"
echo "Kafka UI Deployment"
kubectl apply -f k8s_config/kafka/kafka-ui.yaml
