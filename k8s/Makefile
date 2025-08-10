NAMESPACE = livechatting-app
APP_NAME = spring-app-lvc
MINIKUBE_PROFILE = minikube

GREEN = \033[0;32m
YELLOW = \033[0;33m
RED = \033[0;31m
NC = \033[0m

.PHONY: help setup build deploy status logs clean restart port-forward

help:
	@echo "$(GREEN)Available targets:$(NC)"
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make $(GREEN)<target>$(NC)\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  $(GREEN)%-15s$(NC) %s\n", $$1, $$2 } /^##@/ { printf "\n$(YELLOW)%s$(NC)\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

setup:
	@echo "$(GREEN)Starting minikube...$(NC)"
	minikube start --profile $(MINIKUBE_PROFILE)
	@echo "$(GREEN)Configuring Docker to use minikube daemon...$(NC)"
	@echo "Run: eval $$(minikube docker-env)"

build:
	@echo "$(GREEN)Building Docker image...$(NC)"
	docker build -t $(APP_NAME):latest .
	@echo "$(GREEN)Verifying image exists...$(NC)"
	docker images | grep $(APP_NAME)

deploy:
	@echo "$(GREEN)Deploying to Kubernetes...$(NC)"
	kubectl apply -f namespace.yaml
	kubectl apply -f postgres.yaml
	kubectl apply -f maildev.yaml
	kubectl apply -f backend.yaml

deploy-postgres:
	kubectl apply -f namespace.yaml
	kubectl apply -f postgres.yaml

deploy-maildev:
	kubectl apply -f namespace.yaml
	kubectl apply -f maildev.yaml

deploy-backend:
	kubectl apply -f namespace.yaml
	kubectl apply -f backend.yaml

status:
	@echo "$(GREEN)Checking deployment status...$(NC)"
	kubectl get all -n $(NAMESPACE)
	@echo "\n$(GREEN)Checking persistent volumes...$(NC)"
	kubectl get pv,pvc -n $(NAMESPACE)

pods:
	kubectl get pods -n $(NAMESPACE) -o wide

events:
	kubectl get events -n $(NAMESPACE) --sort-by='.lastTimestamp'

describe-backend:
	kubectl describe pod -l app=backend -n $(NAMESPACE)

describe-postgres:
	kubectl describe pod -l app=postgres -n $(NAMESPACE)

logs:
	kubectl logs -f deployment/backend -n $(NAMESPACE)

logs-postgres:
	kubectl logs -f deployment/postgres -n $(NAMESPACE)

logs-maildev:
	kubectl logs -f deployment/mail-dev -n $(NAMESPACE)

logs-all:
	kubectl logs -f deployment/backend -n $(NAMESPACE) &
	kubectl logs -f deployment/postgres -n $(NAMESPACE) &
	kubectl logs -f deployment/mail-dev -n $(NAMESPACE) &
	wait

open:
	@echo "$(GREEN)Opening Backend...$(NC)"
	minikube service backend