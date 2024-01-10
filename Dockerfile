FROM openjdk:17-jdk-alpine

COPY . /app
WORKDIR /app

RUN ./gradlew build
ENTRYPOINT ["java", "-jar", "build/libs/miner-0.0.1.jar"]
