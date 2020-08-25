FROM openjdk:8
RUN mkdir -p /opt/service
ADD ./build/libs/event_app.auth-0.0.1-SNAPSHOT.jar /opt/service
EXPOSE 8081
CMD ["java", "-jar", "/opt/service/event_app.auth-0.0.1-SNAPSHOT.jar"]