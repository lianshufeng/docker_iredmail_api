- docker-compose.yml
````shell
version: "3"

services:
  springboot:
    build:
      context: ./iredmail
      dockerfile: Dockerfile
    image: lianshufeng/iredmail_api
    ports:
      - "8080:8080"
    working_dir: /opt/jar
    container_name: iredmail_api
    restart: always
````
   
