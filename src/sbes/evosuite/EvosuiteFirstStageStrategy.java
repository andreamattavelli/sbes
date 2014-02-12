package sbes.evosuite;

import java.util.Arrays;

import sbes.util.ArrayUtils;

public class EvosuiteFirstStageStrategy extends Evosuite {

	public EvosuiteFirstStageStrategy(String classSignature, String methodSignature) {
		super(classSignature, methodSignature);
	}

	public String[] getCommand() {
		String[] evo = ArrayUtils.add(super.getCommand(), "-Dstage=1");
		this.command = Arrays.toString(evo);
		return evo;
	}

	@Override
	protected String getTargetMethodSignature() {
		return "method_under_test()V";
	}
	
}
