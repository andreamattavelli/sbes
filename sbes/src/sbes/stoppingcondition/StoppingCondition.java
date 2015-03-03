package sbes.stoppingcondition;

import sbes.option.Options;
import sbes.result.CarvingResult;
import sbes.util.TimeUtils;

public class StoppingCondition {

	private final StoppingConditionType stoppingCondition;
	private long stoppingConditionValue;
	private long elapsedStoppingCondition;
	
	public StoppingCondition() {
		this.stoppingCondition = Options.I().getStoppingCondition();
		setStoppingConditionValue();
		this.elapsedStoppingCondition = 0L;
	}
	
	/*
	 * If we want a time-limit stopping condition, we need to convert the given
	 * time budget to the specific duration.
	 * For CPUTIME, we need to use nanoseconds, while for GLOBALTIME we need to
	 * use MILLISECONDS.
	 */
	private void setStoppingConditionValue() {
		this.stoppingConditionValue = Options.I().getStoppingConditionValue();
		if (stoppingCondition == StoppingConditionType.MAXTIME) {
			this.stoppingConditionValue = TimeUtils.getNormalizedTime(this.stoppingConditionValue);
		}
	}

	public void init() {
		switch(stoppingCondition) {
		case MAXTIME:
			elapsedStoppingCondition = TimeUtils.getCurrentTime();
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
	
	public void update(final CarvingResult result) {
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
			long remaining = stoppingConditionValue - (TimeUtils.getCurrentTime() - elapsedStoppingCondition);
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
			long remaining = stoppingConditionValue - (TimeUtils.getCurrentTime() - elapsedStoppingCondition);
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
