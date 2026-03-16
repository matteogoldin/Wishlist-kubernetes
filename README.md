# Wishlists App

A simple Java REST API for managing wishlists, built with **Javalin** and **Hibernate/JPA**, and progressively evolved to run on **Kubernetes**.

The project is not only a deployable application, but also a hands-on learning path covering containerisation, orchestration, scaling, health checks, and persistent storage.

---

## Stack

- **Java 17**
- **Maven** — build and packaging
- **Javalin** — lightweight REST framework
- **Hibernate / JPA** — ORM persistence
- **MySQL** — relational database
- **Log4j2** — logging
- **Docker** — containerisation
- **Kubernetes** — orchestration
- **Minikube** — local Kubernetes cluster for development and experimentation

---

## Project structure

```text
wishlists/
├── pom.xml
├── .docker-wishlists/
│   └── Dockerfile
├── k8s/                        # Kubernetes manifests
│   ├── 00-namespace.yaml
│   ├── 01-secret.yaml
│   ├── 02-configmap.yaml
│   ├── 05-mysql-pvc.yaml
│   ├── 10-mysql-deployments.yaml
│   ├── 11-mysql-service.yaml
│   ├── 20-wishlist-rest-deployment.yaml
│   ├── 21-wishlist-rest-service.yaml
│   └── 22-wishlist-rest-ingress.yaml
└── src/
    └── main/
        ├── java/
        │   ├── api/            # REST layer (DTOs, mappers, router, service)
        │   ├── app/            # Application entry point
        │   ├── daos/           # Data access layer
        │   ├── model/          # Domain model
        │   └── utils/          # Shared utilities
        └── resources/
            └── META-INF/       # persistence.xml (JPA config)
```

---

## Packages

### `api`

REST API layer built on Javalin.

- `dto` — request/response Data Transfer Objects
- `mapper` — conversions between domain model and DTOs
- `router` — HTTP route registration
- `service` — application-facing facade consumed by the router

### `app`

Application entry point (`WishlistRestApp`).
Bootstraps Javalin, wires components, and reads configuration from environment variables or CLI arguments.

### `daos`

Data access layer.
Encapsulates JPA/Hibernate interactions and exposes CRUD operations.

### `model`

Domain objects mapped to the database via JPA annotations.

### `utils`

Shared support code and cross-cutting utilities.

---

## Environment variables

| Variable      | Description                              | Default (`persistence.xml`)                        |
|---------------|------------------------------------------|----------------------------------------------------|
| `PORT`        | HTTP port the server listens on          | `8080`                                             |
| `DB_URL`      | Full JDBC URL of the MySQL instance      | `jdbc:mysql://localhost:3309/wishlists-schema`     |
| `DB_USER`     | Database username                        | `java-client`                                      |
| `DB_PASSWORD` | Database password                        | `password`                                         |
| `POD_NAME`    | Name of the Pod injected by Kubernetes   | `local-dev`                                        |

> **Why environment variables?**
>
> Using environment variables makes the application easier to run in different environments without changing the code:
> - locally
> - inside Docker
> - inside Kubernetes Pods
>
> This is one of the core ideas behind cloud-native applications.

---

## Build

```bash
cd wishlists
mvn clean package -DskipTests
```

The Maven assembly plugin produces a single executable fat jar:

```
target/wishlists-0.0.1-jar-with-dependencies.jar
```

> **Didactic note**
>
> A fat jar is a self-contained executable archive that includes the application and its dependencies.
> This simplifies containerisation because the Docker image only needs Java and the jar itself.

---

## Run locally with Docker

**Build the image:**

```bash
docker build -t wishlist-rest:local -f wishlists/.docker-wishlists/Dockerfile wishlists
```

**Start MySQL:**

```bash
docker run --name wishlist-mysql -d -p 3309:3306 -e MYSQL_ROOT_PASSWORD=password -e MYSQL_DATABASE=wishlists-schema -e MYSQL_USER=java-client -e MYSQL_PASSWORD=password mysql:8.0.33
```

**Start the REST API:**

```bash
docker run --name wishlist-rest -d -p 8080:8080 -e PORT=8080 -e DB_URL=jdbc:mysql://host.docker.internal:3309/wishlists-schema -e DB_USER=java-client -e DB_PASSWORD=password wishlist-rest:local
```

**Health check:**

```bash
curl http://localhost:8080/health
```

> **Didactic note**
>
> At this stage the application is simply running as containers managed by Docker.
> There is no orchestration yet: if a container fails, Docker alone does not provide the same declarative self-healing and scaling model as Kubernetes.

---

## Kubernetes deployment

All manifests are stored in `wishlists/k8s/`.

Apply them in order:

```bash
kubectl apply -f wishlists/k8s/00-namespace.yaml
kubectl apply -f wishlists/k8s/01-secret.yaml
kubectl apply -f wishlists/k8s/02-configmap.yaml
kubectl apply -f wishlists/k8s/05-mysql-pvc.yaml
kubectl apply -f wishlists/k8s/10-mysql-deployments.yaml
kubectl apply -f wishlists/k8s/11-mysql-service.yaml
kubectl apply -f wishlists/k8s/20-wishlist-rest-deployment.yaml
kubectl apply -f wishlists/k8s/21-wishlist-rest-service.yaml
kubectl apply -f wishlists/k8s/22-wishlist-rest-ingress.yaml
```

Or apply the whole directory at once:

```bash
kubectl apply -f wishlists/k8s/
```

### Manifest overview

| File                              | What it creates                              |
|-----------------------------------|----------------------------------------------|
| `00-namespace.yaml`               | Dedicated namespace for the application      |
| `01-secret.yaml`                  | DB credentials stored as a Kubernetes Secret |
| `02-configmap.yaml`               | Non-sensitive config such as port and DB URL |
| `05-mysql-pvc.yaml`               | PersistentVolumeClaim for MySQL data         |
| `10-mysql-deployments.yaml`       | MySQL Deployment                             |
| `11-mysql-service.yaml`           | MySQL ClusterIP Service                      |
| `20-wishlist-rest-deployment.yaml`| REST API Deployment                          |
| `21-wishlist-rest-service.yaml`   | REST API Service                             |
| `22-wishlist-rest-ingress.yaml`   | Ingress rule for host-based routing          |

### Kubernetes configuration model

The REST API Pod receives configuration from Kubernetes resources instead of hardcoding values.

Example:

```yaml
env:
  - name: PORT
    valueFrom:
      configMapKeyRef:
        name: wishlist-config
        key: PORT
  - name: DB_URL
    valueFrom:
      configMapKeyRef:
        name: wishlist-config
        key: DB_URL
  - name: DB_USER
    valueFrom:
      secretKeyRef:
        name: wishlist-db-secret
        key: MYSQL_USER
  - name: DB_PASSWORD
    valueFrom:
      secretKeyRef:
        name: wishlist-db-secret
        key: MYSQL_PASSWORD
```

> **Didactic note**
>
> This separation reflects a standard Kubernetes design pattern:
> - **ConfigMap** → non-sensitive configuration
> - **Secret** → sensitive values such as passwords
> - **Deployment** → desired number of running Pods
> - **Service** → stable network endpoint for communication inside the cluster

---

## Running the project on Minikube

This project has been developed and tested on Minikube, which provides a local Kubernetes cluster.

### 1. Start Minikube

```bash
minikube start --driver=docker
```

### 2. Verify the cluster status

```bash
kubectl get nodes
minikube status
kubectl cluster-info
kubectl get pods -A
```

> **Didactic note**
>
> These commands are useful to understand the structure of a running Kubernetes cluster:
> - `kubectl get nodes` shows the available cluster nodes
> - `minikube status` shows whether the local cluster is running
> - `kubectl cluster-info` prints the control plane endpoints
> - `kubectl get pods -A` lists Pods in all namespaces, including Kubernetes system components
>
> The Pods shown in `kube-system` belong to Kubernetes itself, not to the application.

### Building the application image for Minikube

From inside the `wishlists` folder:

```bash
minikube image build -t wishlist-rest:local -f .docker-wishlists/Dockerfile .
```

> **Didactic note**
>
> This command builds the image directly in the Minikube environment, making it immediately available to the local cluster without pushing it to an external registry.
> This is especially convenient during development and experimentation.

---

## Useful Kubernetes commands used during development

**Apply manifests:**

```bash
kubectl apply -f wishlists/k8s/
```

**Inspect all resources in the application namespace:**

```bash
kubectl get all -n wishlist
kubectl get pods -n wishlist
```

**Describe a Pod in detail:**

```bash
kubectl describe pod wishlist-rest-d9d76fb87-wsbhn -n wishlist
```

**Forward a Service port to the local machine:**

```bash
kubectl port-forward svc/wishlist-rest 8080:8080 -n wishlist
```

**Delete a Pod to observe self-healing:**

```bash
kubectl delete pod wishlist-rest-d9d76fb87-wsbhn -n wishlist
```

**Inspect logs from all REST API Pods:**

```bash
kubectl logs deployment/wishlist-rest -n wishlist --all-containers=true --prefix=true
```

**Re-deploy after an image update:**

```bash
minikube image build -t wishlist-rest:local -f .docker-wishlists/Dockerfile .
kubectl rollout restart deployment wishlist-rest -n wishlist
kubectl rollout status deployment wishlist-rest -n wishlist
```

> **Didactic note**
>
> Since the image tag `wishlist-rest:local` does not change, Kubernetes would not detect any update on its own.
> `kubectl rollout restart` forces the Deployment to recreate its Pods, picking up the newly built image.
> `kubectl rollout status` then monitors the rollout until all Pods are ready.

> **Didactic note**
>
> These commands cover some of the most important day-to-day Kubernetes activities:
> - `apply` → declare the desired state
> - `get` → inspect the current state
> - `describe` → obtain detailed diagnostic information
> - `logs` → inspect application behaviour
> - `port-forward` → expose an internal Service temporarily on localhost
> - `delete pod` → observe Kubernetes self-healing in action

---

## Health checks and probes

The application exposes:

```
GET /health
```

This endpoint is used by Kubernetes probes.

### Probe types used in the project

- **readinessProbe** — determines when a Pod is ready to receive traffic
- **livenessProbe** — determines when a Pod should be restarted
- **startupProbe** — protects slow-starting containers during bootstrap

### Why `startupProbe` was introduced

When scaling the REST API from one replica to multiple replicas, some Pods failed to become ready if started simultaneously.
The cause was not port conflict, since each Pod has its own IP, but rather the fact that startup could take longer than expected under concurrent initialisation.

Introducing a `startupProbe` gave the application enough time to complete bootstrap before liveness checks began to restart the container.

> **Didactic note**
>
> A common Kubernetes lesson is that:
> - a container being `Running` does not automatically mean it is `Ready`
> - health checks must be tuned according to real application startup behaviour

---

## Persistent storage for MySQL

Initially, MySQL used an `emptyDir` volume.
This meant the database files were tied to the Pod lifecycle and could be lost when the Pod was recreated.

The project was then improved by introducing:

- a `PersistentVolumeClaim`
- a persistent mount for `/var/lib/mysql`

### Why this matters

This change highlights one of the most important distinctions in Kubernetes:

- **REST API** → mostly stateless
- **MySQL** → clearly stateful

Stateful workloads need persistent storage if their data must survive Pod recreation.

---

## Incremental project progress

This Kubernetes setup was not created all at once.
It evolved through a sequence of incremental steps, each introducing new concepts and solving new problems.

### 1. Initial deployment: one Pod per component

The first working version deployed:

- one MySQL Pod
- one REST API Pod

This phase focused on:

- understanding Deployments and Services
- wiring environment variables via ConfigMap and Secret
- verifying connectivity between the API and MySQL

### 2. Manual scaling of the REST API

The REST API Deployment was then scaled manually from one replica to multiple replicas.

This helped demonstrate:

- the declarative nature of Deployments
- Kubernetes self-healing
- how multiple Pods can serve the same application behind a Service

### 3. Debugging multi-replica startup issues

When multiple REST Pods were started simultaneously, some failed readiness and liveness checks.

**Observed issue:**

- the application did not always open port 8080 quickly enough under concurrent startup
- readiness and liveness probes began too early
- Kubernetes restarted the Pods before startup completed

**Resolution:**

- introduce a `startupProbe`
- make probe timing more tolerant of slower bootstrap

### 4. Observing self-healing

Deleting a Pod manually showed that Kubernetes recreated it automatically.

This demonstrated one of the platform's core ideas:

- the user declares the desired number of replicas
- Kubernetes continuously reconciles actual state with desired state

### 5. Moving from ephemeral to persistent database storage

The original MySQL setup used temporary storage.
A later improvement introduced a `PersistentVolumeClaim`, so that database data could survive Pod recreation.

This made the system more realistic and introduced persistent storage as a key Kubernetes concept.

### 6. Ingress configuration and load balancing verification

With multiple REST API replicas already running, an Ingress resource was introduced to provide host-based routing and to observe load balancing in action.

**Steps performed:**

1. Enable the NGINX Ingress controller addon on Minikube:

```bash
minikube addons enable ingress
```

2. Apply the Ingress manifest:

```bash
kubectl apply -f wishlists/k8s/22-wishlist-rest-ingress.yaml
```

3. Open a tunnel so that the Minikube cluster IP becomes reachable on `127.0.0.1`:

```bash
minikube tunnel
```

4. Verify the Ingress was assigned an address and inspect its backends:

```bash
kubectl get ingress -n wishlist
kubectl describe ingress wishlist-rest-ingress -n wishlist
```

The `describe` output confirmed that the Ingress was routing to all five running Pod IPs on port 8080:

```
Rules:
  Host           Path  Backends
  ----           ----  --------
  wishlist.test
                 /   wishlist-rest:8080 (10.244.0.28:8080,10.244.0.29:8080,10.244.0.30:8080 + 2 more...)
```

5. Send repeated requests using the `Host` header and observe which Pod responds:

```bash
curl -H "Host: wishlist.test" http://127.0.0.1/health
# {"status":"UP","podName":"wishlist-rest-79dff6b5f7-7xxbq"}

curl -H "Host: wishlist.test" http://127.0.0.1/health
# {"podName":"wishlist-rest-79dff6b5f7-4jwkc","status":"UP"}
```

Different `podName` values across responses confirmed that traffic was being distributed across multiple replicas.

**Why `minikube tunnel` is necessary:**

In a standard cloud environment, a LoadBalancer Service or an Ingress controller is automatically assigned a public IP by the cloud provider.
Minikube runs inside a virtual machine or a Docker network and is therefore isolated from the host machine's network.
`minikube tunnel` creates a network route between the host and the Minikube cluster IP (`192.168.49.2` in this case), making the cluster reachable on `127.0.0.1`.
Without the tunnel, the Ingress address would be assigned but unreachable from the host.

> **Didactic note — how load balancing works in Kubernetes**
>
> When a Service of type `ClusterIP` sits in front of multiple Pods, `kube-proxy` maintains a set of `iptables` (or IPVS) rules that distribute incoming connections across all healthy Pod endpoints in a round-robin fashion.
> The Ingress controller sits in front of the Service and forwards each HTTP request to one of the available backends listed in the Ingress rules.
> Because each Pod has a unique name injected via the Downward API, calling `/health` repeatedly and comparing `podName` values is a simple but effective way to observe that requests are reaching different Pods — and therefore that load balancing is working as expected.

---

## What this project currently demonstrates

At its current stage, the project demonstrates:

- Docker containerisation of a Java REST API
- deployment of a multi-component application on Kubernetes
- use of Namespace, ConfigMap, Secret, Deployment and Service
- health checks with readiness, liveness and startup probes
- manual horizontal scaling of the REST API
- Kubernetes self-healing behaviour
- persistent storage for a stateful component (MySQL)
- per-Pod observability via the Kubernetes Downward API (`POD_NAME` in `/health`)
- Ingress configuration with host-based routing via the NGINX controller
- load balancing verification across multiple replicas using `podName`
- local Kubernetes development with Minikube

---

## Next possible steps

Possible future improvements include:

- Horizontal Pod Autoscaler (HPA) for the REST API
- resource requests and limits
- multi-node Minikube experiments
- CI/CD pipeline for automatic image build and deployment

---

## Main endpoints

| Method   | Path                    |
|----------|-------------------------|
| `GET`    | `/health`               |
| `GET`    | `/api/wishlists`        |
| `GET`    | `/api/wishlists/{id}`   |
| `POST`   | `/api/wishlists`        |
| `DELETE` | `/api/wishlists/{id}`   |

The `/health` endpoint returns a JSON response that includes the name of the serving Pod:

```json
{
  "status": "UP",
  "podName": "wishlist-rest-d9d76fb87-wsbhn"
}
```

> **Didactic note**
>
> Including `podName` in the health response makes it immediately visible which Pod handled the request.
> This is useful when multiple replicas are running: repeated calls to `/health` (or any load-balanced endpoint)
> may return different Pod names, demonstrating that traffic is distributed across replicas.
> The value is injected via the Kubernetes Downward API (`fieldRef: metadata.name`) rather than hardcoded.

---

## Summary

This project started as a simple Java REST API and gradually evolved into a practical Kubernetes learning lab.

Rather than only showing the final deployment result, it documents the progression from:

- single-container execution,
- to orchestrated multi-Pod deployment,
- to health-check tuning,
- to persistent storage and scaling behaviour.

For this reason, it serves both as:

- a deployable application,
- and a compact portfolio project on Kubernetes fundamentals.
