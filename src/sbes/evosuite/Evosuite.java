package sbes.evosuite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sbes.Options;
import sbes.execution.InternalClassloader;
import sbes.logging.Logger;

public abstract class Evosuite {

	protected static final Logger logger = new Logger(Evosuite.class);
	protected static final String javaCommand[] = {"java", "-Xmx2G"};
	public static final String jarName = "evosuite.jar";

	protected final ClassLoader classLoader;

	protected final String classSignature;
	protected final String methodSignature;
	protected final String outputDir;
	protected String command;

	public Evosuite(final String classSignature, final String methodSignature) {
		this.classSignature = classSignature;
		this.methodSignature = methodSignature;
		this.classLoader = InternalClassloader.getInternalClassLoader();
		this.outputDir = "evosuite-test";
	}

	public String[] getCommand() {
		List<String> evo = new ArrayList<String>();
		evo.addAll(Arrays.asList(javaCommand));
		evo.add("-jar");
		evo.add(jarName);
		evo.add("-DCP="+ Options.I().getClassesPath());
		evo.add("-class");
		evo.add(this.classSignature);
		evo.add("-Dtarget_method=" + getTargetMethodSignature());
		evo.add("-Dsearch_budget=" + Options.I().getSearchBudget());
		evo.add("-Dtest_dir=" + outputDir);
		evo.add("-Dassertions=false");
		evo.add("-Dhtml=false");
		return evo.toArray(new String[0]);
	}
	
	protected abstract String getTargetMethodSignature();

	public String getTestDirectory() {
		return outputDir;
	}

	public static String getTestName(final String classname) {
		return "Test" + classname + ".java";
	}
	
	@Override
	public String toString() {
		return command.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(",", ""); 
	}

}