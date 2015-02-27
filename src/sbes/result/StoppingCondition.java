package sbes.result;

import java.lang.management.ManagementFactory;

import sbes.option.Options;
import sbes.option.StoppingConditionType;
import sbes.option.TimeMeasure;

public class StoppingCondition {

	private StoppingConditionType stoppingCondition;
	private int stoppingConditionValue;
	private long elapsedStoppingCondition;
	
	public StoppingCondition() {
		this.stoppingCondition = Options.I().getStoppingCondition();
		this.stoppingConditionValue = Options.I().getStoppingConditionValue();
		this.elapsedStoppingCondition = 0L;
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
		if (stoppingCondition == StoppingConditionType.NOSYNTHESIS) {
			if (result == null) {
				elapsedStoppingCondition = Long.MIN_VALUE;
			}
		}
		else if (stoppingCondition == StoppingConditionType.MAXITERATIONSWITHNOSYNTHESIS) {
			if (result == null) {
				elapsedStoppingCondition++;
			}
		}
	}
	
	public boolean isReached() {
		if (stoppingCondition == StoppingConditionType.MAXTIME) {
			long remaining = stoppingConditionValue - (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - elapsedStoppingCondition);
			if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
				remaining = stoppingConditionValue - (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - elapsedStoppingCondition);
			}
			else {
				remaining = stoppingConditionValue - (System.currentTimeMillis() - elapsedStoppingCondition);
			}
			if (remaining < 0) {
				return true;
			}
		}
		else if (stoppingCondition == StoppingConditionType.MAXITERATIONS) {
			if (elapsedStoppingCondition++ == stoppingConditionValue) {
				return true;
			}
		}
		else if (stoppingCondition == StoppingConditionType.MAXITERATIONSWITHNOSYNTHESIS) {
			if (elapsedStoppingCondition == stoppingConditionValue) {
				return true;
			}
		}
		else if (stoppingCondition == StoppingConditionType.NOSYNTHESIS) {
			if (elapsedStoppingCondition < 0) {
				return true;
			}
		}
		return false;
	}
	
}
