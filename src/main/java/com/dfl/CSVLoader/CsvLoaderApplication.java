package com.dfl.CSVLoader;

import com.dfl.CSVLoader.dal.DalService;
import com.dfl.CSVLoader.model.Product;
import com.dfl.CSVLoader.runner.BatchRunner;
import com.dfl.CSVLoader.runner.FileLoadRunner;
import com.dfl.CSVLoader.util.SQLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

@SpringBootApplication
public class CsvLoaderApplication {
	private static Logger logger = LoggerFactory.getLogger(CsvLoaderApplication.class) ;

	private static Integer maxBatches ;


	private static Integer batchSize ;


	private static Integer maxThreads ;

	private static JdbcTemplate jdbcTemplate ;

	private static List<String> createSqlList ;

	private static String startRunSql ;

	private static DalService dalService ;

	@Value("${max_batches}")
	public void setMaxBatches(Integer maxBatches) {
		CsvLoaderApplication.maxBatches = maxBatches;
	}

	@Value("${batch_size}")
	public void setBatchSize(Integer batchSize) {
		CsvLoaderApplication.batchSize = batchSize;
	}

	@Value("${max_threads}")
	public void setMaxThreads(Integer maxThreads) {
		CsvLoaderApplication.maxThreads = maxThreads;
	}

	@Value("${setup.sql.list}")
	public void setCreateSqlList(String sqlList){
		CsvLoaderApplication.createSqlList = Arrays.asList(sqlList.split(",")) ;
	}

	@Value("${startrun.sql}")
	public void setStartRunSql(String startRunSql){
		CsvLoaderApplication.startRunSql = startRunSql ;
	}


	@Autowired
	public void setJdbcTemplate(JdbcTemplate template) {
		CsvLoaderApplication.jdbcTemplate = template ;
	}

	@Autowired
	public void setDalService(DalService dalService){
		CsvLoaderApplication.dalService = dalService ;
	}

	public static void main(String[] args) throws IOException {
		int batchId = 0;

		// Check if the command line argument is provided.
		if(args.length < 1){
			logger.error("Invalid arguments. Please provide the path to data file");
			return;
		}
		logger.info("data file being loaded : " + args[0]);

		// Start the spring context only if the correct arguments are provided.
		ConfigurableApplicationContext ctx = SpringApplication.run(CsvLoaderApplication.class, args);

		// Load the SQLs for creating and loading FCT table.
		logger.info("Database setup SQLs configured : " + createSqlList);

		long startTime = new Date().getTime() ;

		// Setup Database Tables.
		dalService.runSqlList(createSqlList);

		//Update Run.
		dalService.runSqlList(Arrays.asList(startRunSql));

		//Get Current Run.
		Integer currentRun = dalService.getCurrentRun() ;

		// Instantiate a blocking queue which will be shared among the File loader and worker threads.
		BlockingQueue<List<Product>> queue = new LinkedBlockingDeque<>(maxBatches);

		// Instantiate the thread pool which will be reused to run batches submitted.
		ExecutorService executorService = Executors.newFixedThreadPool(maxThreads) ;
		logger.info("Thread pool created of size : " + maxThreads);

		// Start the file loader thread to read file sequentially and prepare batches for the workers.
		// The blocking queue instantiated above will be used for holding the batches to be loaded.
		Thread fileLoadThread = new Thread(new FileLoadRunner(queue, batchSize, args[0])) ;
		fileLoadThread.start();

		// Check for available batches in the queue.
		// Check until the file loader thread is still running (as it will add new batches). OR
		// until the queue is empty. Wait until all batches are consumed even if the file loader thread finishes.
		while (fileLoadThread.isAlive() || !queue.isEmpty()){
			logger.debug("Current available batches in the queue : " + queue.size());
			try {
				// if batches are available in the queue, submit to the thread pool.
				if(queue.size()>0){
					logger.info("Submitting batch : " + batchId + " to the thread pool");
					executorService.submit(new BatchRunner(queue.take(), batchId++, jdbcTemplate, currentRun)) ;
				}
				// if batches are not available yet, sleep for 2s and check again.
				else{
					Thread.sleep(2000);
				}
			} catch (InterruptedException e) {
				logger.error(e.getLocalizedMessage());
			}
		}

		// Once all batches are submitted, request for the thread shutdown.
		// It will wait till all submitted threads/jobs are completed before shutdown.
		executorService.shutdown();

		// Wait till the ExecutorService is done processing all batches submitted.
		while (!executorService.isTerminated()){
			// Waiting for all submitted tasks to finish and executor is shutdown.
		}
		// Mapping is completed.
		logger.info("Loading all data into FCT table completed.");


		long endTime = new Date().getTime() ;

		logger.info("Time taken for processing : " + (endTime- startTime)/1000.0/60.0 + " minutes");

		waitOnExit();
		ctx.close();
	}

	private static void waitOnExit(){
		Scanner s = new Scanner(System.in);
		logger.info("Press enter to exit.....");
		s.nextLine();
		logger.info("Switching off the application");
	}

}
