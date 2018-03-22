FROM openjdk:8-jdk-alpine

ENV JAR=temperature-to-zlack-service-0.0.1-SNAPSHOT-fat.jar

COPY build/libs/$JAR /opt/app/app.jar

EXPOSE 8080

WORKDIR /opt/app
ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Dvertx.cacheDirBase=/tmp", "-jar", "app.jar"]
