FROM nginx:1.19.6-alpine

RUN rm /etc/nginx/conf.d/default.conf

COPY nginx.conf /etc/nginx/conf.d/default.conf

 # Remover(rm) o arquivo de configuraca do ngins padrao (default.conf)
 # Depois copiar(COPY) nginx.conf criado para substituir o padrao
 # O servidor ngnix