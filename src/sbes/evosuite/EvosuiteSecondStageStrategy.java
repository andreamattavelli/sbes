package sbes.evosuite;

import sbes.util.ArrayUtils;

public class EvosuiteSecondStageStrategy extends Evosuite {
	
	public EvosuiteSecondStageStrategy(String classSignature, String methodSignature) {
		super(classSignature, methodSignature);
	}

	public String[] getCommand() {
		return ArrayUtils.add(super.getCommand(), "-stage=2");
	}

	@Override
	protected String getTargetMethodSignature() {
		return "method_under_test()V";
	}
	
}
