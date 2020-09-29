FROM gradle:jdk10 as builder
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle bootjar

FROM openjdk:8-jdk-alpine
EXPOSE 8081
VOLUME /tmp
ARG LIBS=app/build/libs
ARG apihost=localhost:8080
ENV API_HOST=$apihost
COPY --from=builder ${LIBS}/ /app/lib
ENTRYPOINT ["java", "-jar", "./app/lib/event_app.auth-0.0.1-SNAPSHOT.jar"]