#FROM openjdk
#WORKDIR /app
#COPY target/eduka-0.0.1-SNAPSHOT.jar eduka-0.0.1-SNAPSHOT.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","eduka-0.0.1-SNAPSHOT.jar"]

# OU

# Stage 1 - Build
FROM maven:3.9.6-eclipse-temurin-11 AS build

WORKDIR /app

COPY pom.xml .
COPY libs/portal-sdk.jar /tmp/portal-sdk.jar

RUN mvn install:install-file \
    -Dfile=/tmp/portal-sdk.jar \
    -DgroupId=com.fc.sdk \
    -DartifactId=portal-sdk \
    -Dversion=1.0 \
    -Dpackaging=jar

COPY src ./src

RUN mvn clean package -DskipTests


# Stage 2 - Runtime
FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=build /app/target/*.jar kudondza.jar

EXPOSE 8080

CMD ["java", "-jar", "kudondza.jar"]


# Passo 1: Build da aplicacao
# ./mvnw clean package
# ./mvnw clean package -DskipTests
# Depois de fazer o build deve aparecer o arquivo com nome kudondza-001-SNAPSHOT.jar na pasta target. Esse é o arquivo que vai rodar no nosso container da aplicacao

# Passo 2: Ciar a imagem. Executar os comandos dentro da pasta do projecto, o ponto significa directorio actual
# docker image build -t kudondza-backend:1.0 .
# verificar se a imagem ja existe no docker
# docker container run --rm -p 8080:8080 kudondza-backend:1.0
# Nota: é possivel que dê erro de conexao com a base de dados, isso porque o IP da BD deve ser IP do docker. Lembrando que a app está a rodar no docker

# Passo 3: Criando uma network e conectando dois containers
# docker network ls
# docker network create --driver bridge eduka-backend-network