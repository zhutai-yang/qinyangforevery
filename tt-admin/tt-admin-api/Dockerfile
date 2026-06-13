# 构建：在仓库根目录 docker compose build api
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml pom.xml
COPY tt-shared tt-shared
COPY tt-admin tt-admin
RUN mvn -q -DskipTests package -pl tt-admin/tt-admin-api -am \
    && cp tt-admin/tt-admin-api/target/tt-admin-api-*.jar /src/app.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /src/app.jar /app/app.jar
RUN mkdir -p /data/uploads /app/logs
EXPOSE 8096
ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8096
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dfile.encoding=UTF-8", \
  "-jar", "/app/app.jar"]
