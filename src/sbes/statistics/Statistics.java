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
import sbes.option.Options;
import sbes.stoppingcondition.TimeMeasure;
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
	
	private void addIteration(final long time, final List<Long> holder) {
		holder.add(time);
	}
	
	public void processStarted() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			this.processStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			this.processStartTime = System.currentTimeMillis();
		}
	}
	public void processFinished() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			this.processEndTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			this.processEndTime = System.currentTimeMillis();
		}
	}

	public void iterationStarted() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			this.iterationStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			this.iterationStartTime = System.currentTimeMillis();
		}
	}
	public void iterationFinished() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			this.iterationEndTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			this.iterationEndTime = System.currentTimeMillis();
		}
		long time = iterationEndTime - iterationStartTime;
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			time = TimeUnit.SECONDS.convert(time, TimeUnit.NANOSECONDS);
		}
		else {
			time = TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS);
		}
		addIteration(time, iterations);
	}
	
	public void scenarioStarted() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			this.scenarioStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			this.scenarioStartTime = System.currentTimeMillis();
		}
	}
	public void scenarioFinished() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			this.scenarioEndTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			this.scenarioEndTime = System.currentTimeMillis();
		}
	}

	public void synthesisStarted() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			this.synthesisStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			this.synthesisStartTime = System.currentTimeMillis();
		}
	}
	public void synthesisFinished() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			this.synthesisEndTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			this.synthesisEndTime = System.currentTimeMillis();
		}
		long time = synthesisEndTime - synthesisStartTime;
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			time = TimeUnit.SECONDS.convert(time, TimeUnit.NANOSECONDS);
		}
		else {
			time = TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS);
		}
		addIteration(time, syntheses);
	}
	
	public void counterexampleStarted() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			this.counterexampleStartTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			this.counterexampleStartTime = System.currentTimeMillis();
		}
	}
	public void counterexampleFinished() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			this.counterexampleEndTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			this.counterexampleEndTime = System.currentTimeMillis();
		}
		long time = counterexampleEndTime - counterexampleStartTime;
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			time = TimeUnit.SECONDS.convert(time, TimeUnit.NANOSECONDS);
		}
		else {
			time = TimeUnit.SECONDS.convert(time, TimeUnit.MILLISECONDS);
		}
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
			if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
				process = TimeUnit.SECONDS.convert(process, TimeUnit.NANOSECONDS);
			}
			else {
				process = TimeUnit.SECONDS.convert(process, TimeUnit.MILLISECONDS);
			}
			bw.write(Long.toString(process));
			long scenario = (scenarioEndTime - scenarioStartTime);
			if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
				scenario = TimeUnit.SECONDS.convert(scenario, TimeUnit.NANOSECONDS);
			}
			else {
				scenario = TimeUnit.SECONDS.convert(scenario, TimeUnit.MILLISECONDS);
			}
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
