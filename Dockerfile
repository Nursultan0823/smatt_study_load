# FROM maven:3.9.9-eclipse-temurin-21 AS builder

# WORKDIR /app

# COPY pom.xml .
# COPY src ./src

# RUN mvn clean package -DskipTests

# FROM eclipse-temurin:21-jre-alpine AS runner
# WORKDIR /app

# COPY --from=builder /app/target/*.jar app.jar

# EXPOSE 8034

# ENTRYPOINT ["java", "-jar", "app.jar"]

FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine AS runner

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

USER spring

ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx320m -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError"

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]