package sbes.util;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import sbes.option.Options;
import sbes.stoppingcondition.TimeMeasure;

public class TimeUtils {

	public static long getCurrentTime() {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
		}
		else {
			return System.currentTimeMillis();
		}
	}
	
	public static long getNormalizedTime(long time) {
		if (Options.I().getTimeMeasure() == TimeMeasure.CPUTIME) {
			return TimeUnit.NANOSECONDS.convert(time, TimeUnit.SECONDS);
		}
		else {
			return TimeUnit.MILLISECONDS.convert(time, TimeUnit.SECONDS);
		}
	}
	
}
