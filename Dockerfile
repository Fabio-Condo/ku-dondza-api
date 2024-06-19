#FROM openjdk
#WORKDIR /app
#COPY target/sci-whereHouse-0.0.1-SNAPSHOT.jar sci-whereHouse-0.0.1-SNAPSHOT.jar
#EXPOSE 8080
#ENTRYPOINT ["java","-jar","sci-whereHouse-0.0.1-SNAPSHOT.jar"]

# OU

FROM openjdk:11-jre
WORKDIR /app
COPY target/*.jar /app/warehouse.jar
EXPOSE 8080
CMD ["java", "-jar", "warehouse.jar"]


# Passo 1: Build da aplicacao
# ./mvnw clean package
# ./mvnw clean package -DskipTests
# Depois de fazer o build deve aparecer o arquivo com nome sci-whereHouse-001-SNAPSHOT.jar na pasta target. Esse é o arquivo que vai rodar no nosso container da aplicacao

# Passo 2: Ciar a imagem. Executar os comandos dentro da pasta do projecto, o ponto significa directorio actual
# docker image build -t warehouse-backend:1.0 .
# verificar se a imagem ja existe no docker
# docker container run --rm -p 8080:8080 warehouse-backend:1.0
# Nota: é possivel que dê erro de conexao com a base de dados, isso porque o IP da BD deve ser IP do docker. Lembrando que a app está a rodar no docker

# Passo 3: Criando uma network e conectando dois containers
# docker network ls
# docker network create --driver bridge warehouse-backend-network