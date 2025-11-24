### How to test this service (Docker)

This is a simple Spring Boot HTTP service exposing the root endpoint `/`.
The container listens on port `5001`.

Prerequisites:
- Docker (with the Compose plugin) installed
- Optional: `curl` for command‑line testing

1) Build and run with Docker Compose
- Start the service in the background:
  - `docker compose up --build -d`
- Verify it’s running:
  - `docker compose ps`
- Tail logs (optional):
  - `docker compose logs -f server`
- Test the HTTP endpoint:
  - `curl -i http://localhost:5001/`
  - Expected response body: `hello world!!`
- Stop everything when done:
  - `docker compose down`

2) Build and run with plain Docker
- Build the image:
  - `docker build -t myapp .`
- Run the container, mapping port 5001:
  - `docker run --rm -p 5001:5001 myapp`
- In another terminal, test the endpoint:
  - `curl -i http://localhost:5001/`

3) Run unit tests inside Docker
There is a dedicated Dockerfile stage named `test` which executes the Gradle test task during build.

- Run tests via the Dockerfile `test` stage (exit code indicates pass/fail):
  - `docker build --target test --progress=plain .`

- Alternatively, run Gradle tests in an ephemeral Gradle container mounting your source:
  - `docker run --rm -v "$PWD":/home/gradle/src -w /home/gradle/src gradle:8.10.2-jdk17 gradle test`

4) What URL should I use?
- Local Docker/Compose: `http://localhost:5001/`
- Expected 200 OK with body: `hello world!!`

### Deploying your application to the cloud

First, build your image, e.g.: `docker build -t myapp .`.
If your cloud uses a different CPU architecture than your development
machine (e.g., you are on a Mac M1 and your cloud provider is amd64),
you'll want to build the image for that platform, e.g.:
`docker build --platform=linux/amd64 -t myapp .`.

Then, push it to your registry, e.g. `docker push myregistry.com/myapp`.

Consult Docker's [getting started](https://docs.docker.com/go/get-started-sharing/)
docs for more detail on building and pushing.

### See Kafka events and captured emails locally (Docker Compose)

This project includes optional local developer tooling to observe events and emails:

- Redpanda (Kafka-compatible broker)
- Kafdrop (web UI to browse topics/messages)
- MailHog (captures SMTP emails with a web UI)

1) Start the full stack
- `docker compose up --build -d`
- Services started:
  - server (your app) on http://localhost:5001
  - Redpanda broker on redpanda:9092 (inside) and localhost:19092 (outside)
  - Kafdrop UI on http://localhost:9000
  - MailHog UI on http://localhost:8025

2) Create a student to trigger an event and an email
- `curl -s -X POST -H 'Content-Type: application/json' \
   -d '{"name":"Alice","email":"alice@example.com"}' http://localhost:5001/students | jq .`

3) View the Kafka event in Kafdrop
- Open http://localhost:9000
- Brokers should show as connected (redpanda:9092)
- Find topic `students.created`
- Click the topic → View Messages to see the JSON payload

4) View the email in MailHog
- Open http://localhost:8025
- You should see a new message sent to `alice@example.com`
- Click to view details; SMTP is configured via compose to route to MailHog

Notes
- Kafka is enabled for the compose stack via environment variables in compose.yaml
  (`app.kafka.enabled=true`, `spring.kafka.bootstrap-servers=redpanda:9092`).
- Real SMTP is not required; MailHog acts as a local SMTP sink. If you prefer real email,
  remove MailHog-related mail settings in compose and set your SMTP settings in
  `application.properties` or environment variables.

### Share records between local Docker and Kubernetes (two-way)

Goal: Write Student records locally and read them inside a Kubernetes pod, and vice‑versa.

Overview of approach
- Use an H2 file‑based database that stores files under /data inside the container (profile: h2file).
- Mount a host directory to /data so the DB files persist outside containers.
- In Docker Compose we already mount ./data -> /data and set SPRING_PROFILES_ACTIVE=h2file.
- In Kubernetes, we mount /data using a hostPath that points to the node’s directory /tmp/test-first-data.

Important safety note
- Do not have two running processes (local Docker and Kubernetes pod) write to the same H2 files at the same time. Stop one before starting the other to avoid corruption. H2 is great for dev but not a multi‑writer network DB.

1) Write locally (Docker Compose) → Read in Kubernetes
- Start locally:
  - `docker compose up --build -d`
- Create data locally:
  - `curl -s -X POST -H 'Content-Type: application/json' \
     -d '{"name":"Alice","email":"alice@example.com"}' http://localhost:5001/students | jq .`
  - `curl -s http://localhost:5001/students | jq .`
- Stop local container so files are closed:
  - `docker compose down`
- Ensure the H2 files exist under ./data on your host. They will be in ./data/h2/…
- For a local Kubernetes cluster (minikube recommended):
  - In terminal A, mount the host folder into the node:
    - `minikube mount "$(pwd)/data:/tmp/test-first-data"`
    - Leave this terminal running.
  - In terminal B, deploy the manifest:
    - `kubectl apply -f docker-java-kubernetes.yaml`
  - Port‑forward to access the service:
    - `kubectl port-forward svc/service-entrypoint 5001:5001`
  - Read back the same records (now served from the pod using the same files):
    - `curl -s http://localhost:5001/students | jq .`

2) Write in Kubernetes → Read locally
- With the pod running and port‑forward still active, create a record:
  - `curl -s -X POST -H 'Content-Type: application/json' \
     -d '{"name":"Bob","email":"bob@example.com"}' http://localhost:5001/students | jq .`
- Stop the pod so files are closed:
  - `kubectl delete -f docker-java-kubernetes.yaml`
  - Stop the minikube mount (Ctrl+C in terminal A).
- The new H2 files are on your host under ./data (synced via minikube mount). Start Docker Compose and read them locally:
  - `docker compose up -d`
  - `curl -s http://localhost:5001/students | jq .`

3) What did we change in this repo to enable this?
- Added a Spring profile h2file (application-h2file.properties) that stores H2 at /data/h2/testdb.
- compose.yaml mounts ./data -> /data and activates the h2file profile.
- docker-java-kubernetes.yaml sets SPRING_PROFILES_ACTIVE=h2file and mounts /data via hostPath at /tmp/test-first-data on the node.

Alternative: Use a real DB for multi‑env access
- For anything beyond local dev, consider using a database service (e.g., Postgres) reachable from both your machine and the cluster. This avoids file‑sharing and concurrency issues and scales better. A simple swap is to add a Postgres service and change Spring datasource settings accordingly.