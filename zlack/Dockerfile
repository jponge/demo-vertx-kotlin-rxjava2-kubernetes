FROM openjdk:8-jdk-alpine

ENV JAR=zlack-0.0.1-SNAPSHOT-fat.jar

COPY kube-application.conf /opt/app/
COPY backend/build/libs/$JAR /opt/app/app.jar

EXPOSE 8080

WORKDIR /opt/app
ENTRYPOINT ["java", "-jar", "app.jar", "-Dconfig.file=kube-application.conf"]
