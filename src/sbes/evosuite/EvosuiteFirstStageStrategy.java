package sbes.evosuite;

import sbes.util.ArrayUtils;

public class EvosuiteFirstStageStrategy extends Evosuite {

	public EvosuiteFirstStageStrategy(String classSignature, String methodSignature) {
		super(classSignature, methodSignature);
	}

	public String[] getCommand() {
		return ArrayUtils.add(super.getCommand(), "-stage=1");
	}

	@Override
	protected String getTargetMethodSignature() {
		return "method_under_test()V";
	}
	
}
