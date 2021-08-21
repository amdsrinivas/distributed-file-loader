FROM maven:3.6.0-jdk-8-alpine

COPY ./pom.xml ./pom.xml
COPY ./src ./src
COPY ./run_CSVLoader.sh ./run_CSVLoader.sh

EXPOSE 10000

RUN mvn -Dmaven.skip.test=true clean package

#CMD ["java", "-jar", "./target/CSVLoader.jar"]
ENTRYPOINT ["/run_CSVLoader.sh"]