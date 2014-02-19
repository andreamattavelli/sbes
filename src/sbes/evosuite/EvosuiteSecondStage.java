package sbes.evosuite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sbes.option.Options;
import sbes.util.DirectoryUtils;

public class EvosuiteSecondStage extends Evosuite {
	
	private final String additionalClasspath;
	
	public EvosuiteSecondStage(String classSignature, String methodSignature, String additionalClasspath) {
		super(classSignature, methodSignature);
		this.additionalClasspath = additionalClasspath;
		this.outputDir = DirectoryUtils.I().getSecondStubEvosuiteDir();
	}

	public String[] getCommand() {
		List<String> evo = new ArrayList<String>();
		if (!Options.I().getJavaPath().equals("")) {
			evo.add(Options.I().getJavaPath() + File.separatorChar + "java");
		}
		else {
			evo.add("java");
		}
		evo.add("-Xmx2G");
		evo.add("-jar");
		evo.add(Options.I().getEvosuitePath() + File.separatorChar + jarName);
		evo.add("-DCP="+ Options.I().getClassesPath() + File.pathSeparatorChar + "." + File.pathSeparatorChar + additionalClasspath);
		evo.add("-class");
		evo.add(classSignature);
		evo.add("-Dtarget_method=" + getTargetMethodSignature());
		evo.add("-Dsearch_budget=" + Options.I().getSearchBudget());
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
