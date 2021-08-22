#!/usr/bin/env sh

echo "Arguments passed: $@"

echo 'Running : java -jar ./target/CSVLoader.jar -Dspring.config.location=/ -Dspring.config.name=application $1'
java -jar ./target/CSVLoader.jar $1 -Dspring.config.location=/ -Dspring.config.name=application