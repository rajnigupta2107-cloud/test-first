
FROM gradle:8.11.1-jdk17 AS build

WORKDIR /home/gradle/src

# Cache dependencies by copying only the build configuration first
COPY build.gradle settings.gradle ./
# Generate and pin Gradle Wrapper to a version that supports newer JDKs
RUN gradle --no-daemon wrapper --gradle-version 8.11.1 \
  && ./gradlew --no-daemon --version \
  && ./gradlew --no-daemon dependencies || true

# Copy the application sources
COPY src ./src

# Build the Spring Boot fat jar
RUN ./gradlew --no-daemon clean bootJar -x test

################################################################################

# Dedicated stage to run tests for CI
FROM build AS test
RUN ./gradlew --no-daemon test


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
