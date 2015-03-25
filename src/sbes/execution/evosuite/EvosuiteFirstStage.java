package sbes.execution.evosuite;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import sbes.option.Options;
import sbes.util.DirectoryUtils;

public class EvosuiteFirstStage extends Evosuite {

	private final String additionalClasspath;

	public EvosuiteFirstStage(final String classSignature, final String methodSignature, final String additionalClasspath) {
		super(classSignature, methodSignature);
		this.additionalClasspath = additionalClasspath;
		this.outputDir = DirectoryUtils.I().getFirstStubEvosuiteDir();
	}

	@Override
	protected String getClassPath() {
		return Options.I().getClassesPath() + File.pathSeparatorChar + additionalClasspath;
	}
	
	@Override
	protected int getSearchBudget() {
		return Options.I().getSearchBudget();
	}
	
	@Override
	protected Collection<String> getAdditionalParameters() {
		Collection<String> additional = new ArrayList<String>();
		additional.add("-Dsbes_phase=1");
		if (Options.I().getLocalSearchRate() > 0) {
			additional.add("-Dlocal_search_rate=" + Options.I().getLocalSearchRate());	
		}
		return additional;
	}
	
	@Override
	protected String getTargetMethodSignature() {
		return "method_under_test()V";
	}

}
