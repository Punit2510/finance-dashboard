FROM maven:3.9.6-eclipse-temurin-21

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/finance-dashboard-1.0.0.jar"]