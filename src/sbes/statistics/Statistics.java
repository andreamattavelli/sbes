package sbes.statistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		this.processStartTime = System.currentTimeMillis();
	}
	public void processFinished() {
		this.processEndTime = System.currentTimeMillis();
	}

	public void iterationStarted() {
		this.iterationStartTime = System.currentTimeMillis();
	}
	public void iterationFinished() {
		this.iterationEndTime = System.currentTimeMillis();
		long time = iterationEndTime - iterationStartTime;
		addIteration(time, iterations);
	}
	
	public void scenarioStarted() {
		this.scenarioStartTime = System.currentTimeMillis();
	}
	public void scenarioFinished() {
		this.scenarioEndTime = System.currentTimeMillis();
	}

	public void synthesisStarted() {
		this.synthesisStartTime = System.currentTimeMillis();
	}
	public void synthesisFinished() {
		this.synthesisEndTime = System.currentTimeMillis();
		long time = synthesisEndTime - synthesisStartTime;
		addIteration(time, syntheses);
	}
	
	public void counterexampleStarted() {
		this.counterexampleStartTime = System.currentTimeMillis();
	}
	public void counterexampleFinished() {
		this.counterexampleEndTime = System.currentTimeMillis();
		long time = counterexampleEndTime - counterexampleStartTime;
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

			long process = (processEndTime - processStartTime) / 1000;
			bw.write(Long.toString(process));
			long scenario = (scenarioEndTime - scenarioStartTime) / 1000;
			bw.write("," + Long.toString(scenario));
			for (int i = 0; i < syntheses.size(); i++) {
				bw.write("," + Long.toString(syntheses.get(i) / 1000));
			}
			for (int i = 0; i < counterexamples.size(); i++) {
				bw.write("," + Long.toString(counterexamples.get(i) / 1000));
			}
			for (int i = 0; i < iterations.size(); i++) {
				bw.write("," + Long.toString(iterations.get(i) / 1000));
			}
		} catch (IOException e) {
			logger.error("Unable to write CSV file", e);
		}
	}
	
}
