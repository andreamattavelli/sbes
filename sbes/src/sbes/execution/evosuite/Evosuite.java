package sbes.execution.evosuite;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sbes.logging.Logger;
import sbes.option.Options;
import sbes.util.ClassUtils;

public abstract class Evosuite {

	protected static final Logger logger = new Logger(Evosuite.class);
	
	public static final String jarName = "evosuite.jar";
	protected static final String evosuiteTestnameExtension = "EvoSuiteTest.java";
	
	protected String classSignature;
	protected String methodSignature;
	protected String outputDir;
	protected String command;

	public Evosuite(final String classSignature, final String methodSignature) {
		this.classSignature = classSignature;
		this.methodSignature = methodSignature;
	}

	public String[] getCommand() {
		List<String> evo = new ArrayList<String>();
		if (!Options.I().getJavaPath().equals("")) {
			evo.add(Options.I().getJavaPath() + File.separatorChar + "java");
		}
		else {
			evo.add("java");
		}
		evo.add("-Xmx4G");
		evo.add("-jar");
		evo.add(Options.I().getEvosuitePath());
		evo.add("-DCP="+ getClassPath());
		evo.add("-class");
		evo.add(classSignature);
		evo.add("-Dtarget_method=" + getTargetMethodSignature());
		evo.add("-Dsearch_budget=" + getSearchBudget());
		evo.add("-Dtest_dir=" + outputDir);
		evo.add("-Dassertions=false");
		evo.add("-Dhtml=false");
		evo.add("-Dvirtual_fs=false"); 
		evo.add("-Dobject_reuse_probability=1");
		evo.add("-mem=2048");
		evo.addAll(getAdditionalParameters());
		this.command = evo.toString();
		return evo.toArray(new String[0]);
	}
	
	protected abstract String getClassPath();
	protected abstract String getTargetMethodSignature();
	protected  abstract int getSearchBudget();
	protected abstract Collection<String> getAdditionalParameters();
	
	public String getTestDirectory() {
		return outputDir;
	}

	public String getTestFilename() {
		return ClassUtils.getSimpleClassnameFromCanonical(classSignature) + evosuiteTestnameExtension;
	}
	
	@Override
	public String toString() {
		return command.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", ""); 
	}

}
