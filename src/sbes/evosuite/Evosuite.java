package sbes.evosuite;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sbes.execution.InternalClassloader;
import sbes.logging.Logger;
import sbes.option.Options;
import sbes.util.ClassUtils;

public abstract class Evosuite {

	protected static final Logger logger = new Logger(Evosuite.class);
	
	public static final String jarName = "evosuite.jar";

	protected final ClassLoader classLoader;

	protected final String classSignature;
	protected final String methodSignature;
	protected String outputDir;
	protected String command;
	protected String filename;

	public Evosuite(final String classSignature, final String methodSignature) {
		this.classSignature = classSignature;
		this.methodSignature = methodSignature;
		this.classLoader = InternalClassloader.getInternalClassLoader();
		this.filename = ClassUtils.getSimpleClassnameFromCanonical(classSignature) + "EvoSuiteTest.java";
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
		evo.add(Options.I().getEvosuitePath());
		evo.add("-DCP="+ getClassPath());
		evo.add("-class");
		evo.add(classSignature);
		evo.add("-Dtarget_method=" + getTargetMethodSignature());
		evo.add("-Dsearch_budget=" + getSearchBudget());
		evo.add("-Dtest_dir=" + outputDir);
		evo.add("-Dassertions=false");
		evo.add("-Dhtml=false");
		evo.addAll(getAdditionalParameters());
		this.command = evo.toString();
		return evo.toArray(new String[0]);
	}
	
	protected abstract String getClassPath();
	protected abstract String getTargetMethodSignature();
	protected abstract int getSearchBudget();
	protected abstract Collection<String> getAdditionalParameters();

	public String getTestDirectory() {
		return outputDir;
	}

	public String getTestFilename() {
		return filename;
	}
	
	@Override
	public String toString() {
		return command.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", ""); 
	}

}
