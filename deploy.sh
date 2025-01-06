#!/bin/bash

# Execute este código depois de criar a infraestrutura na AWS.
# Execute dentro deste arquivo: ./deploy.sh

# Variáveis
AWS_ACCOUNT_ID="471112791344"  # Substitua pelo seu ID da conta AWS
AWS_REGION="us-east-1"         # Região da AWS
ECR_REPO_NAME="ku-dondza-repo" # Nome do repositório ECR
APP_NAME="ku-dondza-backend"   # Nome da aplicação

# Verificar se o Maven está instalado
if ! command -v mvn &> /dev/null; then
    echo "Maven não encontrado. Por favor, instale o Maven e tente novamente."
    exit 1
fi

# Verificar se o Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "Docker não encontrado. Por favor, instale o Docker e tente novamente."
    exit 1
fi

# Verificar se o AWS CLI está instalado
if ! command -v aws &> /dev/null; then
    echo "AWS CLI não encontrado. Por favor, instale o AWS CLI e tente novamente."
    exit 1
fi

# Verificar se o repositório ECR existe
if ! aws ecr describe-repositories --repository-names $ECR_REPO_NAME --region $AWS_REGION &> /dev/null; then
    echo "Repositório ECR '$ECR_REPO_NAME' não encontrado. Por favor, crie o repositório e tente novamente."
    exit 1
fi

# Compilar o projeto Spring
echo "Compilando o projeto Spring..."
if ! mvn clean package; then
    echo "Falha ao compilar o projeto Spring. Verifique o código e tente novamente."
    exit 1
fi

# Construir a imagem Docker
echo "Construindo a imagem Docker..."
if ! docker build -t $APP_NAME .; then
    echo "Falha ao construir a imagem Docker. Verifique o Dockerfile e tente novamente."
    exit 1
fi

# Autenticar no ECR
echo "Autenticando no ECR..."
if ! aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com; then
    echo "Falha ao autenticar no ECR. Verifique as credenciais da AWS e tente novamente."
    exit 1
fi

# Taggear a imagem Docker
echo "Taggeando a imagem Docker..."
if ! docker tag $APP_NAME:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME:latest; then
    echo "Falha ao taggear a imagem Docker. Verifique o nome da imagem e tente novamente."
    exit 1
fi

# Enviar a imagem para o ECR
echo "Enviando a imagem para o ECR..."
if ! docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME:latest; then
    echo "Falha ao enviar a imagem para o ECR. Verifique o repositório e tente novamente."
    exit 1
fi

echo "Deploy concluído com sucesso!"

# Manter o terminal aberto (Linux)
read -p "Pressione qualquer tecla para continuar..."