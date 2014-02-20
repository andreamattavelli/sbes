package sbes.evosuite;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import sbes.SBESException;
import sbes.execution.InternalClassloader;
import sbes.option.Options;
import sbes.util.ClassUtils;
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
	public int getSearchBudget() {
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
		return getBytecodeSignature();
	}

	private String getBytecodeSignature() {
		String toReturn = null;
		Class<?> c;
		try {
			InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath()
																+ File.pathSeparatorChar
																+ DirectoryUtils.I().getSecondStubDir());
			c = Class.forName(classSignature, false, ic.getClassLoader());
			String method = "method_under_test";
			for (Method m : c.getMethods()) {
				// Simple case, we define only a method in the stub
				if (m.getName().equals(method)) {
					toReturn = ClassUtils.getBytecodeSignature(m);
					break;
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("Unable to find class", e);
			throw new SBESException("Unable to find class");
		}
		
		if (toReturn == null) {
			throw new SBESException("Method not found: " + classSignature + "." + methodSignature);
		}
		
		return toReturn;
	}
	
}
