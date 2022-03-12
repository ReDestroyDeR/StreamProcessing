#!/bin/bash
./mvnw clean package
mkdir -p target/dependency
cd target/dependency
jar -xf ../*.jar
cd ../../
docker build .
