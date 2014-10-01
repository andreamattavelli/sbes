package sbes.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import sbes.logging.Logger;
import sbes.util.DirectoryUtils;

public class Statistics {

	private static final Logger logger = new Logger(Statistics.class);
	
	private long processStartTime;
	private long processEndTime;

	private long iterationStartTime;
	private long iterationEndTime;
	
	private long scenarioStartTime;
	private long scenarioEndTime;
	
	private long synthesisStartTime;
	private long synthesisEndTime;
	
	private long counterexampleStartTime;
	private long counterexampleEndTime;

	// holder for elapsed time for each iteration
	private List<Long> iterations;
	private List<Long> syntheses;
	private List<Long> counterexamples;
	
	public Statistics() {
		iterations = new ArrayList<Long>();
		syntheses = new ArrayList<Long>();
		counterexamples = new ArrayList<Long>();
	}
	
	private void addIteration(long time, List<Long> holder) {
		holder.add(time);
	}
	
	public void processStarted() {
		this.processStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}
	public void processFinished() {
		this.processEndTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}

	public void iterationStarted() {
		this.iterationStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}
	public void iterationFinished() {
		this.iterationEndTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		long time = iterationEndTime - iterationStartTime;
		time = TimeUnit.SECONDS.convert(time, TimeUnit.NANOSECONDS);
		addIteration(time, iterations);
	}
	
	public void scenarioStarted() {
		this.scenarioStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}
	public void scenarioFinished() {
		this.scenarioEndTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}

	public void synthesisStarted() {
		this.synthesisStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}
	public void synthesisFinished() {
		this.synthesisEndTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		long time = synthesisEndTime - synthesisStartTime;
		time = TimeUnit.SECONDS.convert(time, TimeUnit.NANOSECONDS);
		addIteration(time, syntheses);
	}
	
	public void counterexampleStarted() {
		this.counterexampleStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	}
	public void counterexampleFinished() {
		this.counterexampleEndTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		long time = counterexampleEndTime - counterexampleStartTime;
		time = TimeUnit.SECONDS.convert(time, TimeUnit.NANOSECONDS);
		addIteration(time, counterexamples);
	}

	public void writeCSV() {
		String csvName = DirectoryUtils.I().getExperimentDir() + File.separatorChar + "output.csv";
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(csvName)))) {
			bw.write("PROCESS,SCENARIO");
			for (int i = 0; i < syntheses.size(); i++) {
				bw.write(",SYNTHESIS" + (i + 1));
			}
			for (int i = 0; i < counterexamples.size(); i++) {
				bw.write(",COUNTEREXAMPLE" + (i + 1));
			}
			for (int i = 0; i < iterations.size(); i++) {
				bw.write(",ITERATION" + (i + 1));
			}

			bw.newLine();
			
			long process = (processEndTime - processStartTime);
			process = TimeUnit.SECONDS.convert(process, TimeUnit.NANOSECONDS);
			bw.write(Long.toString(process));
			long scenario = (scenarioEndTime - scenarioStartTime);
			scenario = TimeUnit.SECONDS.convert(scenario, TimeUnit.NANOSECONDS);
			bw.write("," + Long.toString(scenario));
			for (int i = 0; i < syntheses.size(); i++) {
				bw.write("," + Long.toString(syntheses.get(i)));
			}
			for (int i = 0; i < counterexamples.size(); i++) {
				bw.write("," + Long.toString(counterexamples.get(i)));
			}
			for (int i = 0; i < iterations.size(); i++) {
				bw.write("," + Long.toString(iterations.get(i)));
			}
		} catch (IOException e) {
			logger.error("Unable to write CSV file", e);
		}
	}
	
}
