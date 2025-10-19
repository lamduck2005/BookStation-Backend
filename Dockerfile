# Giai đoạn 1: Build ứng dụng
# Sử dụng một image có sẵn Maven và JDK 17 để build file .jar
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy file pom.xml trước để tận dụng cache của Docker
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy toàn bộ code source vào
COPY src ./src

# Build ứng dụng và bỏ qua tests
RUN mvn package -DskipTests

# Giai đoạn 2: Chạy ứng dụng
# Sử dụng một image chỉ chứa Java Runtime Environment (JRE) cho nhẹ
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy file .jar đã được build từ giai đoạn 1 vào
COPY --from=build /app/target/*.jar app.jar

# Lệnh để chạy ứng dụng. Render sẽ tự động cung cấp biến $PORT
CMD ["java", "-jar", "app.jar"]