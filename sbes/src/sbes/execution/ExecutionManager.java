package sbes.execution;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import sbes.exceptions.WorkerException;
import sbes.execution.evosuite.EvosuiteFirstStage;
import sbes.logging.Logger;
import sbes.option.Options;

public class ExecutionManager {

	private static final Logger logger = new Logger(ExecutionManager.class);

	public static ExecutionResult execute(Tool tool) {
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Worker worker = WorkerFactory.getWorker(tool);

		Future<ExecutionResult> executorResult = executor.submit(worker);
		executor.shutdown();

		boolean terminated = false;
		try {
			terminated = executor.awaitTermination(calculateTimeout(tool), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.fatal("Timeout during test case generation");
			throw new WorkerException("Timeout during test case generation");
		}

		if (!terminated) {
			executor.shutdownNow();
			logger.fatal("Timeout during test case generation");
			throw new WorkerException("Timeout during test case generation");
		}

		ExecutionResult toReturn = null;
		try {
			toReturn = executorResult.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new WorkerException("Error occurred during test case generation: " + e.getMessage(), e);
		}

		return toReturn;
	}

	private static long calculateTimeout(Tool tool) {
		int searchBudget;
		if (tool instanceof EvosuiteFirstStage) {
			searchBudget = Options.I().getSearchBudget();
		} else {
			// this includes both EvosuiteSecondStage and JBSE
			searchBudget = Options.I().getCounterexampleBudget();
		}
		
		// increase time limit to avoid problems
		if (searchBudget <= 60) {
			return searchBudget * 3;
		} else if (searchBudget <= 120) {
			return searchBudget * 2;
		} else {
			return searchBudget + 60;
		}
	}
	
}
