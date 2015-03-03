package sbes.stoppingcondition;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import sbes.option.Options;
import sbes.result.CarvingResult;

public class StoppingCondition {

	private final StoppingConditionType stoppingCondition;
	private long stoppingConditionValue;
	private long elapsedStoppingCondition;
	
	public StoppingCondition() {
		this.stoppingCondition = Options.I().getStoppingCondition();
		setStoppingConditionValue();
		this.elapsedStoppingCondition = 0L;
	}
	
	private void setStoppingConditionValue() {
		this.stoppingConditionValue = Options.I().getStoppingConditionValue();
		if (stoppingCondition == StoppingConditionType.MAXTIME) {
			if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
				this.stoppingConditionValue = TimeUnit.NANOSECONDS.convert(this.stoppingConditionValue, TimeUnit.SECONDS);
			}
			else {
				this.stoppingConditionValue = TimeUnit.MILLISECONDS.convert(this.stoppingConditionValue, TimeUnit.SECONDS);
			}
		}
	}

	public void init() {
		switch(stoppingCondition) {
		case MAXTIME:
			if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
				elapsedStoppingCondition = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
			}
			else {
				elapsedStoppingCondition = System.currentTimeMillis();
			}
			break;
		case MAXITERATIONS:
			elapsedStoppingCondition = 0L;
			break;
		case MAXITERATIONSWITHNOSYNTHESIS:
			elapsedStoppingCondition = 0L;
			break;
		case NOSYNTHESIS:
			elapsedStoppingCondition = 0L;
			break;
		default:
			break;
		}
	}
	
	public void update(CarvingResult result) {
		switch(stoppingCondition) {
		case NOSYNTHESIS:
			if (result == null) {
				elapsedStoppingCondition = Long.MIN_VALUE;
			}
			break;
		case MAXITERATIONSWITHNOSYNTHESIS:
			if (result == null) {
				elapsedStoppingCondition++;
			}
			else {
				elapsedStoppingCondition = 0l;
			}
			break;
		default:
			break;
		}
	}
	
	public boolean isInternallyReached() {
		switch(stoppingCondition) {
		case MAXTIME:
			long remaining;
			if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
				remaining = stoppingConditionValue - (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - elapsedStoppingCondition);
			}
			else {
				remaining = stoppingConditionValue - (System.currentTimeMillis() - elapsedStoppingCondition);
			}
			if (remaining < 0) {
				return true;
			}
			break;
		case MAXITERATIONS:
			if (elapsedStoppingCondition > stoppingConditionValue) {
				return true;
			}
			break;
		case MAXITERATIONSWITHNOSYNTHESIS:
			if (elapsedStoppingCondition > stoppingConditionValue) {
				return true;
			}
			break;
		case NOSYNTHESIS:
			if (elapsedStoppingCondition < 0) {
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	public boolean isReached() {
		switch(stoppingCondition) {
		case MAXTIME:
			long remaining;
			if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
				remaining = stoppingConditionValue - (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - elapsedStoppingCondition);
			}
			else {
				remaining = stoppingConditionValue - (System.currentTimeMillis() - elapsedStoppingCondition);
			}
			if (remaining < 0) {
				return true;
			}
			break;
		case MAXITERATIONS:
			if (++elapsedStoppingCondition > stoppingConditionValue) {
				return true;
			}
			break;
		case MAXITERATIONSWITHNOSYNTHESIS:
			if (elapsedStoppingCondition == stoppingConditionValue) {
				return true;
			}
			break;
		case NOSYNTHESIS:
			if (elapsedStoppingCondition < 0) {
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}
	
}
