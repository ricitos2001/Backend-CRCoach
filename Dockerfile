FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /usr/src/app

ARG NODE_ENV
ARG PORT

ENV PGHOST=""
ENV PGPORT=""
ENV PGDATABASE=""
ENV PGUSER=""
ENV PGPASSWORD=""
ENV SPRING_MAIL_USERNAME=""
ENV SPRING_MAIL_PASSWORD=""
ENV APP_FRONTEND_BASE_URL=""


ENV PORT=8080

COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /usr/src/app/target/Backend-CRCoach-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-Xms256m","-Xmx512m","-XX:+UseG1GC","-jar","/app/app.jar"]
