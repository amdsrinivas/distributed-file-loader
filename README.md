# distributed-file-loader
Application to load large amounts of structured data into DB in a non-blocking fashion

# Intuition
The idea is to separate the file read and file load. Since serial read is faster and parallel load is faster.
We use one thread to load the file sequentially and create batches from the file that can be loaded into the DB.
We use multiple worker threads to load these batches into DB. The number of worker threads can be configured from properties file.
We additionally keep track of the line number (can be tracked since serial read), and the run number to handle updates.

# Steps to Run the code
#### Running the code directly
Developed the code using Java Springboot. So, the prerequisites are Java, Maven and preferably IntelliJ.

The code expects the data file path as the one and only command line argument. It can be configured on IntelliJ using run configurations.

```sh
java -jar ./target/CSVLoader.jar /path/to/datafile
```

Once run, the DB can be accessed as long as the application is running on localhost:<server.port>/h2-console
The login details and server.port values can be  found in application.properties file.

#### Running using the docker container
Dockerfile is available to run the code as a container.

Run the following command to create a container image from the root of distributed-file-loader project.

```sh
docker build -t distributed-file-loader:latest .
```

Using the created container, run the code using run_container.sh script as below

```sh
./run_container.sh -t <CONTAINER_IMAGE>:<TAG> -c <ABSOLUTE/PATH/TO/PROPERTIES/FILE> -d <ABSOLUTE/PATH/TO/STORE/H2/DB> -p <ABSOLUTE/PATH/TO/DATAFILE -o <PORT/TO/MAP/ON/LOCAL>
```

To save the time in building the image, the container is pushed to [DockerHub-amdsrinivas](https://hub.docker.com/repository/docker/amdsrinivas/distributed-file-loader)

To run using pre-built image:

```sh
./run_container.sh -t amdsrinivas/distributed-file-loader:latest -c <ABSOLUTE/PATH/TO/PROPERTIES/FILE> -d <ABSOLUTE/PATH/TO/STORE/H2/DB> -p <ABSOLUTE/PATH/TO/DATAFILE -o <PORT/TO/MAP/ON/LOCAL>
```

# Tables used

- PRODUCTS_FCT : table to load the data of all products
##### Schema
```sql
CREATE TABLE IF NOT EXISTS PRODUCTS_FCT(
    PRODUCT_ID INTEGER PRIMARY KEY,
    PRODUCT_NAME VARCHAR(100),
    PRODUCT_SKU VARCHAR(100),
    PRODUCT_DESCRIPTION VARCHAR(500),
    LINE_NUMBER INTEGER,
    UPDATED_ON_RUN INTEGER
) ; -- src/main/resources/sql/CREATE_PRODUCTS_FCT.sql
```

- PRODUCTS_VIEW : View to get the aggregate details of all products by name and count
##### Schema
```sql
CREATE OR REPLACE VIEW PRODUCTS_VIEW AS
    SELECT PRODUCT_NAME, COUNT(*) AS NUMBER_OF_PRODUCTS
    FROM PRODUCTS_FCT GROUP BY PRODUCT_NAME
; -- src/main/resources/sql/CREATE_PRODUCTS_VIEW.sql
```

-- RUN_LOGGER : table to keep track of the current run number. Used in updates.
##### Schema
```sql
CREATE TABLE IF NOT EXISTS RUN_LOGGER(
    RUN_NUMBER INTEGER
) ; 

INSERT INTO RUN_LOGGER (RUN_NUMBER)
SELECT 0
WHERE NOT EXISTS (SELECT * FROM RUN_LOGGER) ; -- src/main/resources/sql/CREATE_PRUN_LOGGER.sql
```

# Point achieved

1. The code follows OOPS concepts.
2. The ingestion is non-blocking parallel using threads. On average, ingesting the products.csv takes 0.953 minutes. An update file with 18 rows takes  0.0352 minutes.
3. A column called PRODUCT_ID is generated from 'sku' column using String.hashCode() method of Java. The 'sku' loaded from file is converted to PRODUCT_ID which is used for updates.
4. All product details are ingested to single PRODUCTS_FCT table.
5. Aggregated data is available under view PRODUCTS_VIEW.

# Points unable to achieve -- NA

# Improvements
1. MySQL specific syntax used for loading batches. Would have tried for DB independent syntax.
2. Would have tuned the number of threads, maximum batches and batch size to optimize the waiting time in few scenarios.
3. Uses file based DB. Would have tried to use a container based DB server.
3. More clean code.

