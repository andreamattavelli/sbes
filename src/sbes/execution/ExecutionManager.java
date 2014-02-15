package sbes.execution;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import sbes.Options;
import sbes.evosuite.Evosuite;
import sbes.logging.Logger;

public class ExecutionManager {

	private static final Logger logger = new Logger(ExecutionManager.class);

	public ExecutionResult execute(Evosuite evosuite) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Worker worker = new Worker(evosuite);
		
		Future<ExecutionResult> firstResult = executor.submit(worker);
		executor.shutdown();

		boolean result = false;
		try {
			result = executor.awaitTermination(calculateTimeout(), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.fatal("Timeout during test case generation");
			throw new WorkerException("Timeout during test case generation");
		}

		if (!result) {
			executor.shutdownNow();
			// should be infeasible
			logger.fatal("Timeout during test case generation");
			throw new WorkerException("Timeout during test case generation");
		}

		ExecutionResult toReturn = null;
		try {
			toReturn = firstResult.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new WorkerException("Error occurred during test case generation: " + e.getMessage(), e);
		}

		return toReturn;
	}

	private long calculateTimeout() {
		int searchBudget = Options.I().getTestSearchBudget();
		if (searchBudget < 60) {
			return searchBudget * 2;
		}
		else if (searchBudget < 120){
			return (long)(searchBudget * 1.5);			
		}
		else {
			return searchBudget + 60;
		}
	}	
	
}
