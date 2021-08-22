package com.dfl.CSVLoader.runner;

import com.dfl.CSVLoader.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class BatchRunner implements Runnable {
    private List<Product> batchData ;
    private Integer batchId ;
    private Integer currentRun ;

    private JdbcTemplate jdbcTemplate ;


    private static Logger logger = LoggerFactory.getLogger(BatchRunner.class) ;

    public BatchRunner(List<Product> batchData, Integer batchId, JdbcTemplate jdbcTemplate, Integer currentRun) {
        this.batchData = batchData ;
        this.batchId = batchId ;
        this.jdbcTemplate = jdbcTemplate;
        this.currentRun = currentRun ;
    }

    @Override
    public void run() {
        logger.info("Running batch : " + batchId + " of size : " + batchData.size());
        // Insert the batch data into STG table.
        String sql = "INSERT INTO PRODUCTS_FCT VALUES (?,?,?,?,?,?)  " +
                " ON DUPLICATE KEY " +
                " UPDATE " +
                " PRODUCT_NAME= CASE WHEN (UPDATED_ON_RUN < VALUES(UPDATED_ON_RUN) OR LINE_NUMBER < VALUES(LINE_NUMBER)) THEN VALUES(PRODUCT_NAME) ELSE PRODUCT_NAME END , " +
                " PRODUCT_SKU= CASE WHEN (UPDATED_ON_RUN < VALUES(UPDATED_ON_RUN) OR LINE_NUMBER < VALUES(LINE_NUMBER)) THEN VALUES(PRODUCT_SKU) ELSE PRODUCT_SKU END ,  " +
                " PRODUCT_DESCRIPTION=CASE WHEN (UPDATED_ON_RUN < VALUES(UPDATED_ON_RUN) OR LINE_NUMBER < VALUES(LINE_NUMBER)) THEN VALUES(PRODUCT_DESCRIPTION) ELSE PRODUCT_DESCRIPTION END , " +
                " LINE_NUMBER=CASE WHEN (UPDATED_ON_RUN < VALUES(UPDATED_ON_RUN) OR LINE_NUMBER < VALUES(LINE_NUMBER)) THEN VALUES(LINE_NUMBER) ELSE LINE_NUMBER END, " +
                " UPDATED_ON_RUN=CASE WHEN UPDATED_ON_RUN < VALUES(UPDATED_ON_RUN) THEN VALUES(UPDATED_ON_RUN) ELSE UPDATED_ON_RUN END ";
        try{
            // Make a batch update to the DB using JdbcTemplate's batchUpdate functionality to minimize the number of
            // DB connections.
            jdbcTemplate.batchUpdate(
                    sql,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                            Product product = batchData.get(i) ;
                            preparedStatement.setInt(1, product.getId());
                            preparedStatement.setString(2, product.getName());
                            preparedStatement.setString(3, product.getSku());
                            preparedStatement.setString(4, product.getDescription());
                            preparedStatement.setInt(5, product.getBatchNumber()*getBatchSize() + product.getRecordNumber());
                            preparedStatement.setInt(6, currentRun);
                        }

                        @Override
                        public int getBatchSize() {
                            return batchData.size() ;
                        }
                    }
            ) ;
        } catch (Exception e){
            e.printStackTrace();
        }
        logger.info("Stage Load completed for batch : " + batchId + " of size : " + batchData.size());
    }
}
