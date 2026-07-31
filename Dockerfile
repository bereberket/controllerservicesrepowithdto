FROM eclipse-temurin:17-jdk-jammy AS BUILD

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

RUN groupadd --system bank && useradd --system --gid bank bank

COPY --from=build --chown=bank:bank /workspace/target/*.jar app.jar

RUN mkdir -p /app/data && chown -R bank:bank /app

USER bank

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]