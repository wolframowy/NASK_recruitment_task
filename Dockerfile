FROM openjdk:11
EXPOSE 8080
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} nask_task.jar
ENTRYPOINT ["java","-jar","/nask_task.jar"]