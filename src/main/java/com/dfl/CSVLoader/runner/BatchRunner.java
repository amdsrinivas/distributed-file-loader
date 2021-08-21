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

    private JdbcTemplate jdbcTemplate ;

    private static Logger logger = LoggerFactory.getLogger(BatchRunner.class) ;

    public BatchRunner(List<Product> batchData, Integer batchId, JdbcTemplate jdbcTemplate) {
        this.batchData = batchData ;
        this.batchId = batchId ;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run() {
        logger.info("Running batch : " + batchId + " of size : " + batchData.size());
        // Insert the batch data into STG table.
        String sql = "INSERT INTO PRODUCTS_STG VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE PRODUCT_NAME=VALUES(PRODUCT_NAME), PRODUCT_DESCRIPTION=VALUES(PRODUCT_DESCRIPTION)" ;
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
                            //preparedStatement.setInt(5,batchId*getBatchSize()+product.getLineNumber());
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
