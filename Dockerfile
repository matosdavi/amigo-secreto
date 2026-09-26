# Build em DUAS etapas (multi-stage):
#   1. "build": uma imagem com o JDK e o Maven compila o .jar;
#   2. "final": uma imagem mínima recebe SÓ o .jar. Sem compilador, sem shell,
#      sem código-fonte: menor e com menos superfície de ataque.

# ---- 1. build -------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

# Copia o pom.xml ANTES do código: o Docker guarda esta camada em cache, e as
# dependências só são baixadas de novo quando o pom.xml muda.
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests && cp target/*.jar /src/app.jar

# ---- 2. final -------------------------------------------------------------------
# distroless/java21: só o Java e um usuário sem privilégios (nonroot).
FROM gcr.io/distroless/java21-debian12:nonroot
COPY --from=build /src/app.jar /app/app.jar
USER nonroot:nonroot
EXPOSE 8080
ENV APP_ENV=production PORT=8080
# MaxRAMPercentage: o Java usa até 75% da memória do container (512 MB no Render grátis)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
