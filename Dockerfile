# Etapa 1: compila o projeto com Maven e gera o .jar com todas as dependencias
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY Codigo/pom.xml .
RUN mvn -q -B dependency:go-offline
COPY Codigo/src ./src
RUN mvn -q -B clean package -DskipTests && cp target/backend-fixed-*.jar app.jar

# Etapa 2: imagem final leve, so com o Java para executar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar
ENV PORT=8080
EXPOSE 8080
CMD ["java", "-Xmx300m", "-jar", "app.jar"]
