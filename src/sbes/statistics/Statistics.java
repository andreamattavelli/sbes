package sbes.statistics;

import java.util.ArrayList;
import java.util.List;

public class Statistics {

	private long startTime;
	private long endTime;

	private long iterationStartTime;
	private long iterationEndTime;
	
	private List<Long> iterations;

	public Statistics() {
		iterations = new ArrayList<Long>();
	}

	public void iterationStarted() {
		this.iterationStartTime = System.currentTimeMillis();
	}

	public void iterationFinished() {
		this.iterationEndTime = System.currentTimeMillis();
		addIteration();
	}
	
	private void addIteration() {
		long time = iterationEndTime - iterationStartTime;
		iterations.add(time);
	}

	public void synthesisStarted() {
		this.startTime = System.currentTimeMillis();
	}

	public void synthesisFinished() {
		this.endTime = System.currentTimeMillis();
	}

	public void print() {
		System.out.println("Duration: " + (endTime - startTime));
		System.out.println("Iterations:");
		int i = 0;
		for (Long iteration : iterations) {
			System.out.println(" * " + i + " time: " + iteration);
		}
	}
}
