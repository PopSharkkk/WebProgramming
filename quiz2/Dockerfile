# Build stage
FROM --platform=linux/arm64 eclipse-temurin:21-jdk-alpine AS build

RUN apk add --no-cache maven

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
COPY database ./database

RUN mvn package -DskipTests -B


# Runtime stage
FROM --platform=linux/arm64 eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/classes ./target/classes
COPY --from=build /app/target/dependency ./target/dependency
COPY --from=build /app/src/main/webapp ./src/main/webapp
COPY --from=build /app/database ./database

RUN mkdir -p /app/data

ENV PORT=8080
ENV DB_URL=jdbc:sqlite:/app/data/wild_indonesia.db

EXPOSE 8080

CMD ["java", "-cp", "target/classes:target/dependency/*", "com.wildindonesia.AppServer"]