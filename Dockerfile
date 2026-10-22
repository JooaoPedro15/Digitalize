# Etapa 1: compila o projeto com Maven e gera o .jar do Spring Boot
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY Codigo/pom.xml .
RUN mvn -q -B dependency:go-offline
COPY Codigo/src ./src
RUN mvn -q -B package -DskipTests

# Etapa 2: imagem final leve, só com o Java para executar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/digitalize.jar app.jar
ENV PORT=8080
EXPOSE 8080
# Limites de memória para caber nos 512 MB do plano gratuito do Render
CMD ["java", "-Xmx256m", "-Xss512k", "-XX:MaxMetaspaceSize=160m", "-XX:ReservedCodeCacheSize=64m", \
     "-XX:+UseSerialGC", "-XX:TieredStopAtLevel=1", "-jar", "app.jar"]
