package sbes.evosuite;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import sbes.option.Options;
import sbes.util.DirectoryUtils;

public class EvosuiteSecondStage extends Evosuite {
	
	private final String additionalClasspath;
	
	public EvosuiteSecondStage(String classSignature, String methodSignature, String additionalClasspath) {
		super(classSignature, methodSignature);
		this.additionalClasspath = additionalClasspath;
		this.outputDir = DirectoryUtils.I().getSecondStubEvosuiteDir();
	}
	
	@Override
	protected String getClassPath() {
		return Options.I().getClassesPath() + File.pathSeparatorChar + "." + File.pathSeparatorChar + additionalClasspath;
	}
	
	@Override
	protected int getSearchBudget() {
		return Options.I().getSearchBudget();
	}
	
	@Override
	protected Collection<String> getAdditionalParameters() {
		Collection<String> additional = new ArrayList<String>();
		additional.add("-Dstage=2");
		return additional;
	}

	@Override
	protected String getTargetMethodSignature() {
		return "method_under_test()V";
	}
	
}
