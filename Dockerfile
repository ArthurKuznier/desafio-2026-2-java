# Etapa 1: build - compila o projeto e gera o jar
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia so os arquivos de definicao de dependencia primeiro, pra aproveitar
# cache do Docker (se o pom.xml nao mudar, nao baixa tudo de novo)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: execucao - imagem final, so com o jar e o JRE (bem menor que a de build)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]