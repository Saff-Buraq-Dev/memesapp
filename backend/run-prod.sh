#!/bin/bash
export SPRING_PROFILES_ACTIVE=prod
export MYSQL_URL=jdbc:mysql://localhost:3306/memevote?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=
export JWT_SECRET=memevoteSecretKeyProduction
export CORS_ALLOWED_ORIGINS=http://localhost:4200
./mvnw spring-boot:run
