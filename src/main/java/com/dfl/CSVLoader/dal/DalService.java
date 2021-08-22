package com.dfl.CSVLoader.dal;

import com.dfl.CSVLoader.util.SQLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class DalService {
    private static Logger logger = LoggerFactory.getLogger(DalService.class) ;

    @Autowired
    private JdbcTemplate jdbcTemplate ;

    private void runSql(String sql){
        try{
            jdbcTemplate.execute(sql);
        }
        catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    public void runSqlList(List<String> sql){
        try{
            sql.stream().forEach(sqlItem -> {
                try {
                    runSql(SQLUtils.getSql(sqlItem));
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            });
        }
        catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    public Integer getCurrentRun(){
        try{
            String query = "SELECT RUN_NUMBER FROM RUN_LOGGER" ;
            Integer currentRun = jdbcTemplate.queryForObject(query, Integer.class) ;
            logger.info("Current Run Number using the DB : " + currentRun);
            return currentRun ;
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return null ;
        }
    }
}
