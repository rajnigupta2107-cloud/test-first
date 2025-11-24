
# syntax=docker/dockerfile:1.7
FROM gradle:8.11.1-jdk17 AS build

WORKDIR /home/gradle/src

# Copy Gradle wrapper and build config separately to maximize Docker layer caching
COPY gradlew ./
COPY gradle/wrapper ./gradle/wrapper
COPY build.gradle settings.gradle gradle.properties ./

## Verify Gradle and warm up dependency cache without sources
## Use BuildKit cache mounts to persist Gradle caches between Docker builds
RUN --mount=type=cache,target=/home/gradle/.gradle \
    bash -lc 'set -e; gradle --no-daemon --version; gradle --no-daemon -g /home/gradle/.gradle dependencies || true'

# Note: We copy sources only after dependency cache warm-up so source changes do not
# invalidate dependency layers and keep builds fast.
COPY src ./src

# Build the Spring Boot fat jar (skip tests in image build; tests run in dedicated stage)
# Avoid 'clean' to leverage Gradle incremental compilation across Docker layers
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle --no-daemon -g /home/gradle/.gradle bootJar -x test

################################################################################

# Dedicated stage to run tests for CI
FROM build AS test
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle --no-daemon -g /home/gradle/.gradle test


################################################################################

# Create a new stage for running the application that contains the minimal
# runtime dependencies for the application. This often uses a different base
# image from the install or build stage where the necessary files are copied
# from the install stage.
#
# The example below uses eclipse-turmin's JRE image as the foundation for running the app.
# By specifying the "17-jre-jammy" tag, it will also use whatever happens to be the
# most recent version of that tag when you build your Dockerfile.
# If reproducibility is important, consider using a specific digest SHA, like
# eclipse-temurin@sha256:99cede493dfd88720b610eb8077c8688d3cca50003d76d1d539b0efc8cca72b4.
FROM eclipse-temurin:17-jre-jammy AS final

# Create a non-privileged user that the app will run under.
# See https://docs.docker.com/go/dockerfile-user-best-practices/
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

# Copy the executable from the "package" stage.
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 5001

ENTRYPOINT [ "java", "-jar", "app.jar" ]
