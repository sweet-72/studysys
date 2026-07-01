FROM maven:3.8.6-openjdk-18-slim AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests package

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /build/target/*.jar /app/app.jar

ENV SERVER_PORT=8101
ENV SPRING_PROFILES_ACTIVE=prod
ENV SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/smart_class
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=change_me
ENV SPRING_REDIS_HOST=localhost
ENV SPRING_REDIS_PORT=6379
ENV SPRING_REDIS_PASSWORD=
ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8101

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -Dserver.port=$SERVER_PORT -Dspring.datasource.url=$SPRING_DATASOURCE_URL -Dspring.datasource.username=$SPRING_DATASOURCE_USERNAME -Dspring.datasource.password=$SPRING_DATASOURCE_PASSWORD -Dspring.redis.host=$SPRING_REDIS_HOST -Dspring.redis.port=$SPRING_REDIS_PORT -Dspring.redis.password=$SPRING_REDIS_PASSWORD -jar /app/app.jar"]