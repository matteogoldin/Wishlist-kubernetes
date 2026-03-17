# Kubernetes Q&A — Theoretical Foundations

A study-oriented Q&A document designed to provide a solid theoretical foundation on Kubernetes, with practical references inspired by a Java REST API project deployed on Minikube.

The goal of this document is not only to define Kubernetes concepts, but also to explain **why they matter**, **how they relate to each other**, and **how they emerge in a real project**.

---

## 1. Kubernetes fundamentals

### Q: What is Kubernetes?

**A:** Kubernetes is a container orchestration platform.  
It is designed to deploy, manage, scale, and recover containerised applications.

In simpler terms:

- **Docker** creates and runs containers
- **Kubernetes** decides where containers should run, how many instances should exist, how they should communicate, and what should happen if something fails

Kubernetes is built around a **declarative model**: instead of manually starting and stopping containers one by one, you describe the desired state of the system, and Kubernetes continuously tries to keep the real state aligned with that desired state.

---

### Q: What problem does Kubernetes solve?

**A:** Kubernetes solves several problems that become difficult to manage manually when applications grow beyond a few standalone containers.

Typical problems include:

- restarting failed containers
- scaling applications horizontally
- exposing applications over the network
- connecting multiple services together
- separating configuration from code
- managing secrets
- rolling out new versions
- handling stateful and stateless workloads differently

Without Kubernetes, these tasks are either manual or handled with ad hoc scripts.  
Kubernetes provides a consistent orchestration model for them.

---

### Q: What does “declarative” mean in Kubernetes?

**A:** It means that you describe **what you want**, not **how to do it step by step**.

For example, instead of saying:

- start container A
- then restart it if it crashes
- then create two more copies if CPU rises

you declare something like:

- I want a Deployment called `wishlist-rest`
- I want 3 replicas
- I want them exposed through a Service
- I want them restarted if they fail

Kubernetes then works continuously to reconcile actual state with desired state.

This is one of the most important ideas in the whole platform.

---

### Q: What is a cluster?

**A:** A Kubernetes cluster is the complete environment where Kubernetes runs applications.

A cluster includes:

- the **control plane**
- one or more **nodes**
- the workloads running on those nodes
- the networking and storage configuration supporting those workloads

You can think of the cluster as the full Kubernetes “system”.

---

### Q: What is a node?

**A:** A node is a machine that belongs to the cluster and can run workloads.

A node can be:

- a physical machine
- a virtual machine
- a container-based node in a local environment such as Minikube

Nodes are where Pods actually run.

---

### Q: Can a cluster have multiple nodes?

**A:** Yes. In real environments, clusters usually have multiple nodes.

A multi-node cluster allows Kubernetes to:

- distribute workloads
- use more CPU and memory
- tolerate node failures better
- separate control-plane responsibilities from application workloads

A local Minikube cluster often starts with a **single node** because that is simpler and lighter for learning and development.

---

### Q: What does it mean that Minikube created “one node”?

**A:** It means your local Kubernetes cluster currently has a single machine available to run workloads.

In a one-node Minikube setup:

- the control plane and worker responsibilities are typically concentrated in the same node
- all Pods run on that single node
- scheduling choices are limited because there is only one destination

This is perfectly fine for learning.

---

### Q: What is the control plane?

**A:** The control plane is the part of Kubernetes that manages the whole cluster.

It is responsible for:

- receiving and validating API requests
- storing cluster state
- scheduling Pods
- reconciling desired and actual state
- controlling cluster-level behaviour

Without the control plane, Kubernetes would not know what should exist in the cluster.

---

### Q: Which control-plane components are most important to understand?

**A:** The most important ones are:

- **kube-apiserver**
- **etcd**
- **kube-scheduler**
- **kube-controller-manager**

In many local clusters you will also observe:

- **CoreDNS**
- **kube-proxy**

Each one serves a different role.

---

### Q: What does `kube-apiserver` do?

**A:** It is the front door of the cluster.

Whenever you use `kubectl`, you are talking to the API server.  
It validates requests and exposes the Kubernetes API.

All major cluster interactions pass through it.

---

### Q: What does `etcd` do?

**A:** `etcd` is the key-value store that keeps the cluster state.

It stores information such as:

- Deployments
- Services
- Pods
- Secrets
- ConfigMaps
- Namespaces

You can think of it as Kubernetes’ persistent memory.

---

### Q: What does `kube-scheduler` do?

**A:** It decides **on which node** a newly created Pod should run.

When a Pod is created, the scheduler looks at:

- available resources
- scheduling constraints
- affinity and anti-affinity rules
- taints and tolerations
- requests defined on containers

In a one-node cluster, the choice is trivial.  
In a multi-node cluster, the scheduler becomes much more interesting.

---

### Q: What does `kube-controller-manager` do?

**A:** It runs Kubernetes controllers.

A controller continuously checks whether the real state matches the desired state.

For example:

- if a Deployment wants 3 replicas
- but only 2 Pods are running
- a controller notices the mismatch
- and causes another Pod to be created

This continuous reconciliation is a defining feature of Kubernetes.

---

### Q: What does CoreDNS do?

**A:** CoreDNS provides internal DNS for the cluster.

It allows applications to communicate using names instead of IP addresses.

For example, if a MySQL Service is called `mysql`, another Pod can connect to it using:

`mysql:3306`

instead of needing to know the Pod IP.

This makes service-to-service communication stable even when Pods are recreated.

---

### Q: What does `kube-proxy` do?

**A:** `kube-proxy` helps implement Service networking.

Services in Kubernetes provide stable virtual endpoints in front of Pods.  
`kube-proxy` contributes to routing traffic from a Service to the correct backend Pods.

---

## 2. Pods, containers, and workloads

### Q: What is a Pod?

**A:** A Pod is the smallest deployable unit in Kubernetes.

A Pod usually contains one main application container, but it can contain more than one container when those containers are meant to work closely together.

Containers in the same Pod share:

- network namespace
- localhost
- volumes mounted in the Pod

Kubernetes schedules Pods, not individual containers.

---

### Q: What is the difference between a Pod and a container?

**A:** A container is the actual isolated process that runs your application.  
A Pod is the Kubernetes wrapper around one or more containers.

Relationship:

- cluster → contains nodes
- node → runs Pods
- Pod → contains containers

In practice, many applications use:

- **1 Pod = 1 main container**

But Kubernetes still reasons in terms of Pods.

---

### Q: Why does Kubernetes work with Pods instead of individual containers?

**A:** Because a Pod represents a small execution environment with shared networking and storage.

This allows Kubernetes to model patterns such as:

- sidecar containers
- init containers
- tightly coupled containers that must run together

The Pod abstraction is more expressive than a single container abstraction.

---

### Q: What is a Deployment?

**A:** A Deployment is a higher-level Kubernetes resource that manages stateless Pods.

A Deployment allows you to declare:

- which image to run
- how many replicas should exist
- which labels identify the Pods
- what update strategy to use
- what probes and resource rules should be applied

It is one of the most commonly used Kubernetes resources.

---

### Q: What is the relationship between a Deployment and a ReplicaSet?

**A:** A Deployment manages a ReplicaSet, and the ReplicaSet manages the Pods.

In simplified terms:

- the Deployment defines the desired application state
- the ReplicaSet ensures the correct number of Pods exists
- the Pods run the actual containers

Users usually interact with Deployments, not directly with ReplicaSets.

---

### Q: What does `replicas` mean in a Deployment?

**A:** It defines how many Pod instances Kubernetes should keep running.

For example:

- `replicas: 1` → one Pod
- `replicas: 3` → three Pods

If one Pod fails or is deleted, Kubernetes tries to create another so that the total returns to the declared value.

---

### Q: What is self-healing in Kubernetes?

**A:** Self-healing is Kubernetes’ ability to recreate or replace failed workloads to restore the desired state.

For example:

- if a Pod managed by a Deployment crashes
- or if you manually delete it
- Kubernetes notices the missing replica
- and creates a new Pod

This behaviour is a natural consequence of the declarative model.

---

### Q: How can self-healing be demonstrated easily?

**A:** A simple and very effective experiment is:

1. create a Deployment with one or more replicas
2. list Pods with `kubectl get pods -w`
3. manually delete one Pod
4. observe Kubernetes creating a replacement

This shows that Kubernetes is not simply “running containers”; it is maintaining a declared target state.

---

### Q: What happens if a node fails?

**A:** In a single-node cluster, failure of the node usually means the workloads stop.  
In a multi-node cluster, Kubernetes may reschedule workloads onto other nodes, depending on configuration and control-plane health.

This is one reason why multi-node clusters are important in production.

---

## 3. Minikube and the local lab environment

### Q: What is Minikube?

**A:** Minikube is a tool for running Kubernetes locally.

It creates a local cluster suitable for:

- learning
- development
- experiments
- small demos

It is not meant to replace full production infrastructure, but it behaves like a real Kubernetes environment in many important ways.

---

### Q: Why is Minikube useful for learning Kubernetes?

**A:** Because it allows you to practice real Kubernetes concepts on your own machine:

- creating Deployments
- exposing Services
- scaling Pods
- using ConfigMaps and Secrets
- working with volumes
- testing HPA
- enabling Ingress
- inspecting logs and events

This makes it ideal for project-based learning.

---

### Q: Does Minikube always use virtual machines?

**A:** No. Minikube supports different drivers.

Depending on the environment, Minikube can use:

- a VM-based driver
- Docker
- other supported backends

A common local setup is Minikube with the **Docker driver**.

---

### Q: If Minikube uses the Docker driver, is there a double layer of containerisation?

**A:** In a simplified sense, yes.

The mental model is often:

- host machine
    - Minikube node environment
        - Pods
            - containers

When Docker is used as the driver, the Minikube node is hosted through Docker on the local machine.  
Then Kubernetes runs Pods on that node.

This means there is an extra abstraction layer compared with plain `docker run`, but for learning purposes the important concept is still that Kubernetes runs Pods on nodes.

---

### Q: Does Minikube with Docker mean my normal Docker containers become Kubernetes Pods?

**A:** No.

Your regular Docker containers remain regular Docker containers.  
Minikube creates its own Kubernetes environment, and Kubernetes runs Pods inside that environment.

This is why building an image locally on the host does not automatically mean the cluster can use it.

---

### Q: What does `minikube image build` do?

**A:** It builds a container image directly in the Minikube environment so that the local cluster can use it immediately.

This is useful in development because it avoids the need to:

1. build locally
2. push to an external registry
3. pull from the cluster

Instead, the image becomes available directly to the local cluster.

---

### Q: Why did `minikube image build` need to be executed from the module folder in the project?

**A:** Because the command uses a **build context**, and the Dockerfile contains `COPY` instructions that depend on the correct relative paths.

If the Dockerfile expects to copy files like:

- `pom.xml`
- `src/`

then the build context must be the folder where those files exist.

This illustrates a key Docker concept:

- the **Dockerfile path** tells the builder which Dockerfile to use
- the **build context** tells the builder which files are visible to the build

---

### Q: What is the difference between `minikube stop` and `minikube delete`?

**A:** `minikube stop` shuts down the cluster but preserves it so that you can restart it later.

`minikube delete` removes the cluster.

Use:

- `stop` when you want to pause work
- `delete` when you want to clean up the environment completely

Deleting only the underlying Docker container is not the recommended cleanup flow.

---

## 4. Namespaces, configuration, and secrets

### Q: What is a Namespace?

**A:** A Namespace is a logical partition inside a Kubernetes cluster.

It helps organise resources by creating separate scopes for names and grouping.

Namespaces are useful for:

- separating applications
- avoiding name collisions
- grouping related resources
- applying policies

In a project, using a dedicated namespace such as `wishlist` keeps the application resources separate from system resources.

---

### Q: Why do we need ConfigMaps?

**A:** ConfigMaps are used to store **non-sensitive configuration** outside the application code.

Examples include:

- port numbers
- URLs
- feature flags
- environment-specific settings

They help keep the application portable and configurable across environments.

---

### Q: Why do we need Secrets?

**A:** Secrets store **sensitive values**, such as:

- passwords
- tokens
- credentials

They are separate from ConfigMaps because sensitive data should not be treated like normal configuration.

In a typical project:

- database URL → ConfigMap
- database password → Secret

---

### Q: Why is separating config from code important?

**A:** It supports portability and environment independence.

The same application image can run:

- locally
- in Docker
- in Kubernetes
- in different environments

without code changes, because configuration is injected externally.

This is an important cloud-native design principle.

---

## 5. Services, networking, and load balancing

### Q: What is a Service in Kubernetes?

**A:** A Service is a stable network endpoint in front of one or more Pods.

Pods are ephemeral: they can be recreated and their IPs can change.  
A Service solves this by providing:

- a stable name
- a stable virtual IP
- a mechanism for routing traffic to the correct backend Pods

---

### Q: Why not connect directly to Pod IPs?

**A:** Because Pod IPs are not stable over time.

If a Pod is recreated, its IP can change.  
A Service allows clients to keep using the same address even as Pods come and go.

---

### Q: Does Kubernetes already do load balancing between Pods?

**A:** Yes.

A normal Kubernetes Service distributes traffic across the backend Pods that are considered **ready**.

This means a Service acts as an internal load-balancing layer within the cluster.

---

### Q: What kind of load balancing does a `ClusterIP` Service provide?

**A:** `ClusterIP` provides internal access and load balancing **inside the cluster**.

It does not expose the application directly to the outside world, but it provides a stable internal endpoint for other Pods and for tools such as `kubectl port-forward`.

---

### Q: How can we verify that a Service is distributing requests to different Pods?

**A:** There are several ways:

1. inspect the Service endpoints
2. watch logs from all Pods with Pod prefixes
3. make the application return the identity of the Pod that served the request

The third option is often the clearest.

---

### Q: How can a Pod identify itself in a response?

**A:** Kubernetes can inject the Pod name into the container using the **Downward API**.

The container receives the Pod name as an environment variable such as `POD_NAME`, and the application can return it in a JSON response or HTTP header.

This is very useful for:

- debugging
- demonstrating load balancing
- observing traffic distribution during scaling tests

---

### Q: If multiple Pods expose the same container port, do they conflict?

**A:** No.

Each Pod has its own network namespace and its own IP address.  
Therefore, multiple Pods can all listen on port `8080` without conflict, even if they run on the same node.

This is a very common misunderstanding for people coming from plain Docker.

---

### Q: What is `port-forward`?

**A:** `kubectl port-forward` temporarily maps a port from your local machine to a Pod or Service in the cluster.

It is useful for:

- local testing
- debugging
- development

It is not the same thing as publishing a production endpoint, but it is extremely convenient in a local lab.

---

## 6. Health checks and probes

### Q: What are probes in Kubernetes?

**A:** Probes are health checks used by Kubernetes to evaluate containers.

The main probe types are:

- **readinessProbe**
- **livenessProbe**
- **startupProbe**

Each one answers a different question.

---

### Q: What does `readinessProbe` mean?

**A:** It asks: **Is this container ready to receive traffic?**

If the readiness probe fails:

- the Pod is not considered ready
- the Service should stop sending traffic to that Pod

This does not necessarily mean the container is dead.  
It may simply still be starting up or waiting on a dependency.

---

### Q: What does `livenessProbe` mean?

**A:** It asks: **Is this container still healthy enough to keep running?**

If the liveness probe fails repeatedly, Kubernetes may restart the container.

This is useful when a process becomes stuck or unhealthy.

---

### Q: What does `startupProbe` mean?

**A:** It is used for slow-starting applications.

It asks: **Has startup completed yet?**

While the startup probe is still failing, Kubernetes postpones the normal liveness and readiness logic.  
This prevents a slow but healthy application from being killed too early.

---

### Q: Why was `startupProbe` needed in the project?

**A:** Because when multiple REST API Pods were started simultaneously, some of them did not become ready quickly enough.

The observed behaviour was:

- the application took longer to open port `8080`
- readiness probe started checking too early
- liveness probe also started too early
- Kubernetes restarted the container before startup finished

Adding a startup probe solved this by giving the application more time during bootstrap.

---

### Q: What is the key lesson behind readiness, liveness, and startup probes?

**A:** A container being **Running** does not mean it is **Ready**.

This is a fundamental Kubernetes lesson:

- **Running** means the process exists
- **Ready** means it can safely receive traffic

Probe timing should reflect real application startup behaviour.

---

### Q: Why did MySQL show “connection refused” during readiness checks even though it later worked?

**A:** Because container startup and service readiness are not the same thing.

The MySQL container process may have started, but the database engine was not yet accepting TCP connections on port `3306`.

This is normal for databases and other services that need time to initialise.

---

## 7. Storage and stateful workloads

### Q: What is the difference between stateless and stateful workloads?

**A:** A **stateless** workload does not rely on local state tied to a Pod instance.  
A **stateful** workload depends on data that must survive Pod restarts or recreation.

Examples:

- REST API → usually stateless
- database → stateful

This distinction matters deeply in Kubernetes.

---

### Q: What is `emptyDir`?

**A:** `emptyDir` is a Pod-local temporary volume.

It exists for the life of the Pod.  
If the Pod is deleted and recreated, the data in `emptyDir` is lost.

This makes it useful for temporary scratch storage, but not for persistent databases.

---

### Q: Why was `emptyDir` not sufficient for MySQL?

**A:** Because a database is a stateful workload.

If MySQL stores its files in `emptyDir`, deleting or recreating the Pod can mean losing the database files.  
That is unacceptable for any persistence requirement.

---

### Q: What is a PersistentVolumeClaim (PVC)?

**A:** A PVC is a request for persistent storage in Kubernetes.

A Pod can mount a PVC and use it as persistent storage.  
The PVC is not tied to a single Pod lifecycle in the same way as `emptyDir`.

This is the normal approach for giving a Pod persistent storage.

---

### Q: What is a PersistentVolume (PV)?

**A:** A PV is the actual storage resource that satisfies a PVC.

In many modern setups, including local development setups, PVs may be provisioned dynamically rather than created manually.

---

### Q: What is dynamic provisioning?

**A:** It is when Kubernetes automatically creates the backing storage required by a PVC, typically through a StorageClass.

This simplifies storage management because users can declare the storage they need without manually creating the physical volume first.

---

### Q: Why is adding a PVC an important learning step?

**A:** Because it introduces one of the most important Kubernetes distinctions:

- stateless workloads can be recreated easily
- stateful workloads require persistence and more careful design

This is a foundational idea in Kubernetes architecture.

---

### Q: Is a Deployment ideal for a database?

**A:** It works for simple learning scenarios, but in more advanced or production-like scenarios databases are often better represented with a **StatefulSet**.

A StatefulSet provides stronger guarantees about identity and storage for stateful applications.

However, for a learning project, Deployment + PVC is a perfectly reasonable intermediate step.

---

## 8. Resources, scheduling, and autoscaling

### Q: What are resource requests?

**A:** Resource requests declare the minimum CPU and memory a container needs.

They are mainly used by the scheduler when deciding whether a node has enough capacity to host a Pod.

Requests therefore influence placement and scheduling.

---

### Q: What are resource limits?

**A:** Resource limits declare the maximum CPU and memory a container is allowed to use.

They help prevent a single container from consuming unbounded resources on a node.

Limits are enforced at runtime.

---

### Q: Why are requests and limits important?

**A:** They make cluster behaviour more realistic and predictable.

Without them:

- the scheduler has less information
- resource usage is less controlled
- autoscaling metrics become less meaningful

They are especially important when moving beyond a toy environment.

---

### Q: Are resource requests a prerequisite for autoscaling?

**A:** For CPU-based Horizontal Pod Autoscaling, yes, they are strongly recommended and effectively required for meaningful behaviour.

This is because HPA uses CPU utilisation as a percentage of the **CPU request**.

Without a request, the autoscaling target is not grounded properly.

---

### Q: What is the Horizontal Pod Autoscaler (HPA)?

**A:** HPA automatically changes the number of Pod replicas in a Deployment (or another scalable workload) based on observed metrics.

Typical metric examples include:

- CPU utilisation
- memory utilisation
- custom metrics

In a typical REST API, HPA is applied to the API Deployment, not to the database.

---

### Q: Why is HPA usually appropriate for a REST API but not for MySQL?

**A:** A REST API is usually stateless and designed to scale horizontally.  
A database is stateful and scaling it is much more complex.

For learning projects and many real systems, scaling the API layer is straightforward, while scaling the database layer requires more specialised approaches.

---

### Q: What additional component is needed for HPA based on CPU or memory?

**A:** The cluster needs a metrics pipeline, commonly provided by **metrics-server**.

Without resource metrics, HPA cannot make decisions based on CPU or memory usage.

---

### Q: How does HPA behave when load increases?

**A:** When the observed metric exceeds the configured target, HPA can increase the number of replicas, within the allowed bounds.

Example:

- target CPU utilisation = 70%
- current average CPU utilisation rises above that threshold
- HPA increases replicas

---

### Q: How does HPA behave when load stops?

**A:** Downscaling is generally more conservative than upscaling.

Even if load drops, Kubernetes usually does not remove replicas immediately.  
This is deliberate, to avoid rapid oscillation (also called flapping).

In practice, after stopping the load, you should expect replicas to scale down gradually rather than instantly.

---

### Q: Why is downscaling slower than upscaling?

**A:** Because removing Pods too quickly can destabilise an application and create oscillation if load returns immediately afterward.

Kubernetes intentionally uses more cautious behaviour for scale down.

This is one of the reasons autoscaling feels smarter than simply reacting instantly in both directions.

---

## 9. Ingress and external access

### Q: What is Ingress?

**A:** Ingress is a Kubernetes resource used to define HTTP and HTTPS routing into the cluster.

Ingress allows you to express rules such as:

- host-based routing
- path-based routing
- centralised entry points

It is typically used to expose web applications more cleanly than ad hoc `port-forward` commands.

---

### Q: Is an Ingress resource enough by itself?

**A:** No.

An Ingress resource requires an **Ingress controller** running in the cluster.

The controller is what actually interprets the Ingress rules and implements them.

Without a controller, the Ingress object exists only as configuration data with no active effect.

---

### Q: Why is Ingress a useful next step after Services?

**A:** Because it introduces a more realistic entry point for HTTP applications.

After learning:

- Pods
- Deployments
- Services
- port-forwarding

Ingress is the natural next concept for exposing applications in a more production-like way.

---

## 10. Observability and debugging

### Q: Why are `kubectl get`, `describe`, and `logs` so important?

**A:** Because they form the core basic debugging workflow in Kubernetes.

- `kubectl get` → see current state
- `kubectl describe` → see detailed configuration and events
- `kubectl logs` → see application output

Together they let you diagnose most early problems.

---

### Q: What does `kubectl describe pod` tell us that `kubectl get pods` does not?

**A:** `kubectl get pods` is a summary view.  
`kubectl describe pod` gives much richer information, including:

- events
- scheduling details
- probe failures
- restart history
- environment
- mounted volumes

It is often the first place to look when something is not starting.

---

### Q: How can we view logs from multiple Pods at once?

**A:** Kubernetes allows aggregated log inspection using:

- a Deployment target
- label selectors
- pod prefixes

This is especially helpful in scaled scenarios, where you want to see which Pod served which request or which Pod produced an error.

---

### Q: Why was returning the Pod name in `/health` useful?

**A:** Because it made load balancing visible from the outside.

Without Pod identity in the response, a scaled Service may behave correctly but still look opaque.  
Returning `podName` turns an abstract balancing mechanism into an observable experiment.

---

## 11. CI/CD and local deployment automation

### Q: Does CI/CD still make sense if the cluster is only local?

**A:** Yes, especially for a portfolio project.

Even if the cluster is local, a CI/CD pipeline can still demonstrate:

- build automation
- test execution
- image creation
- deployment workflows
- release discipline

The educational and portfolio value remains high.

---

### Q: What is a self-hosted runner?

**A:** A self-hosted runner is a machine that **you manage** and that executes GitHub Actions jobs.

It does not have to be a dedicated server.  
It can be:

- your development PC
- a virtual machine
- a cloud instance
- another machine you control

---

### Q: Do I need my own server to use a self-hosted runner?

**A:** No.

A self-hosted runner can run on your own computer.  
For a local Minikube-based portfolio, this is often the most practical setup.

The trade-off is that:

- your computer must be on
- the runner must be active
- Minikube must be running

So it is suitable for demos and learning, not as a permanent production target.

---

### Q: What is a realistic CI/CD strategy for a local Kubernetes portfolio?

**A:** A very sensible progression is:

1. start with **CI only**
    - build
    - tests
    - image creation
2. optionally add **CD**
    - automated deployment to local Minikube
    - likely using a self-hosted runner on your own machine

This gives strong portfolio value without pretending the local lab is production infrastructure.

---

## 12. Common misconceptions and key lessons

### Q: Does Kubernetes build my application?

**A:** No.

Kubernetes runs container images.  
It does not compile your source code or build your JAR by itself.

The normal flow is:

1. build the application
2. build the image
3. deploy the image in Kubernetes

---

### Q: If I change the Java source code, do running Pods automatically see the change?

**A:** No.

Pods run from images, not from your live source tree.

After changing the source code, you need to:

1. rebuild the image
2. make Kubernetes start new Pods from that image

This is a crucial difference between local source development and containerised deployment.

---

### Q: If I scale to multiple Pods and they all expose port 8080, is that a problem?

**A:** No.

Each Pod has its own IP address and network namespace, so multiple Pods can all listen on port `8080` simultaneously without conflict.

---

### Q: If multiple Pods start at the same time and some fail probes, is that automatically a scaling problem?

**A:** Not necessarily.

It may simply mean:

- startup is slower under concurrent load
- probes are too aggressive
- dependencies such as the database are not yet ready

The issue may be startup timing, not the idea of scaling itself.

---

### Q: What is one of the most important mental shifts when learning Kubernetes?

**A:** Moving from “I start containers manually” to “I declare desired system state and Kubernetes maintains it”.

Once this shift becomes natural, many Kubernetes features make more sense:

- Deployments
- probes
- Services
- self-healing
- scaling
- HPA

---

## 13. Suggested learning path

### Q: What is a good conceptual order for learning Kubernetes through a project?

**A:** A very effective order is:

1. learn cluster basics and nodes
2. understand Pods and Deployments
3. expose the application with a Service
4. add ConfigMaps and Secrets
5. introduce probes
6. observe self-healing
7. add persistent storage
8. define resource requests and limits
9. introduce autoscaling
10. add Ingress
11. explore CI/CD
12. optionally experiment with multi-node clusters

This progression mirrors how understanding usually deepens in real project work.

---

### Q: Why is this incremental path valuable?

**A:** Because Kubernetes becomes much easier to understand when each new concept solves a real problem you have already encountered.

For example:

- multiple Pods introduce load balancing and probe timing concerns
- databases introduce persistence concerns
- resource management prepares autoscaling
- Ingress becomes meaningful after Services
- CI/CD becomes meaningful after the manifests stabilise

This project-driven learning pattern is often more effective than studying concepts in isolation.

---

## 14. Final recap

### Q: What are the main conceptual takeaways from this Kubernetes learning path?

**A:** The key takeaways are:

- Kubernetes is declarative orchestration, not just container execution
- Pods are the basic deployable unit
- Deployments manage stateless workloads and replica counts
- Services provide stable networking and internal load balancing
- readiness, liveness, and startup probes answer different health questions
- stateless and stateful workloads must be treated differently
- persistent storage is essential for databases
- resource requests and limits improve realism and enable better autoscaling
- HPA scales stateless workloads based on observed metrics
- Ingress provides cleaner HTTP entry into the cluster
- Minikube is an excellent local lab for learning these ideas
- CI/CD remains valuable even in a local-lab portfolio when framed honestly

---

### Q: Why is a project-based Kubernetes lab useful?

**A:** Because it turns abstract platform concepts into observable behaviours.

Instead of only memorising definitions, you can actually see:

- Pods being recreated
- Services routing requests
- probes affecting readiness
- PVCs preserving state
- HPA scaling up and down
- Ingress exposing traffic
- CI pipelines building deployable artefacts

That combination of theory and direct observation makes Kubernetes much easier to understand and explain.