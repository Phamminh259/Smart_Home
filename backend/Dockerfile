FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
RUN mkdir /tmp/temp
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]