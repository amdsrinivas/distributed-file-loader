FROM maven:3.6.0-jdk-8-alpine

COPY ./pom.xml ./pom.xml

RUN mvn dependency:go-offline -B

COPY ./src ./src
COPY ./run_CSVLoader.sh ./run_CSVLoader.sh

EXPOSE 10000

RUN mvn -Dmaven.skip.test=true clean package

ENTRYPOINT ["/run_CSVLoader.sh"]