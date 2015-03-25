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
	
	private void setStoppingConditionValue() {
		this.stoppingConditionValue = Options.I().getStoppingConditionValue();
		switch(stoppingCondition) {
		case TIME:
			this.stoppingConditionValue = TimeUtils.getNormalizedTime(this.stoppingConditionValue);
			break;
		case NOSYNTHESIS:
			this.stoppingConditionValue = 1L;
			break;
		default:
			break;
		}
	}

	public void init() {
		switch(stoppingCondition) {
		case TIME:
			elapsedStoppingCondition = TimeUtils.getCurrentTime();
			break;
		case ITERATIONS:
			elapsedStoppingCondition = 0L;
			break;
		case MAXWITHOUTSYNTHESIS:
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
		case MAXWITHOUTSYNTHESIS:
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
		case TIME:
			long remaining = stoppingConditionValue - (TimeUtils.getCurrentTime() - elapsedStoppingCondition);
			if (remaining < 0) {
				return true;
			}
			break;
		case ITERATIONS:
			if (elapsedStoppingCondition > stoppingConditionValue) {
				return true;
			}
			break;
		case NOSYNTHESIS:
		case MAXWITHOUTSYNTHESIS:
			if (elapsedStoppingCondition > stoppingConditionValue) {
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
		case TIME:
			long remaining = stoppingConditionValue - (TimeUtils.getCurrentTime() - elapsedStoppingCondition);
			if (remaining < 0) {
				return true;
			}
			break;
		case ITERATIONS:
			if (++elapsedStoppingCondition > stoppingConditionValue) {
				return true;
			}
			break;
		case NOSYNTHESIS:
		case MAXWITHOUTSYNTHESIS:
			if (elapsedStoppingCondition == stoppingConditionValue) {
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}
	
}
