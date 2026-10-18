FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src src
RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:17-jre-jammy
RUN groupadd --system vetturno && useradd --system --gid vetturno vetturno
WORKDIR /app
COPY --from=build /build/target/vetturno-1.0.0.jar app.jar
USER vetturno
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
