package sbes.statistics;

import java.lang.management.ManagementFactory;

import sbes.option.Options;
import sbes.option.StoppingCondition;

public class StoppingConditionManager {

	private StoppingCondition stoppingCondition;
	private int stoppingConditionValue;
	private long elapsedStoppingCondition;
	
	public StoppingConditionManager() {
		this.stoppingCondition = Options.I().getStoppingCondition();
		this.stoppingConditionValue = Options.I().getStoppingConditionValue();
		this.elapsedStoppingCondition = 0L;
	}
	
	public void initStoppingCondition() {
		switch(stoppingCondition) {
		case MAXTIME:
			elapsedStoppingCondition = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
			break;
		case MAXITERATIONS:
			elapsedStoppingCondition = 0L;
			break;
		default:
			break;
		}
	}
	
	public boolean isStoppingConditionReached() {
		if (stoppingCondition == StoppingCondition.MAXTIME) {
			long remaining = stoppingConditionValue - (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - elapsedStoppingCondition);
			if (remaining < 0) {
				return true;
			}
		}
		else if (stoppingCondition == StoppingCondition.MAXITERATIONS) {
			if (elapsedStoppingCondition++ == stoppingConditionValue) {
				return true;
			}
		}
		// NOSYNTHESIS must be managed inside the main loop
		return false;
	}
	
}
