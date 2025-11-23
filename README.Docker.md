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