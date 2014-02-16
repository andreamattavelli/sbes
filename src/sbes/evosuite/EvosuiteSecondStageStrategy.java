package sbes.evosuite;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sbes.Options;
import sbes.util.DirectoryUtils;

public class EvosuiteSecondStageStrategy extends Evosuite {
	
	private final String additionalClasspath;
	
	public EvosuiteSecondStageStrategy(String classSignature, String methodSignature, String additionalClasspath) {
		super(classSignature, methodSignature);
		this.additionalClasspath = additionalClasspath;
		this.outputDir = DirectoryUtils.I().getFirstStubEvosuiteDir();
	}

	public String[] getCommand() {
		List<String> evo = new ArrayList<String>();
		evo.addAll(Arrays.asList(javaCommand));
		evo.add("-jar");
		evo.add(jarName);
		evo.add("-DCP="+ Options.I().getClassesPath() + File.pathSeparatorChar + "." + File.pathSeparatorChar + additionalClasspath);
		evo.add("-class");
		evo.add(classSignature);
		evo.add("-Dtarget_method=" + getTargetMethodSignature());
		evo.add("-Dsearch_budget=" + Options.I().getTestSearchBudget());
		evo.add("-Dtest_dir=" + outputDir);
		evo.add("-Dassertions=false");
		evo.add("-Dhtml=false");
		evo.add("-Dstage=2");
		this.command = evo.toString();
		return evo.toArray(new String[0]);
	}

	@Override
	protected String getTargetMethodSignature() {
		return "method_under_test()V";
	}
	
}
