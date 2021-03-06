package sbes.execution.evosuite;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import sbes.exceptions.SBESException;
import sbes.execution.InternalClassloader;
import sbes.option.Options;
import sbes.util.DirectoryUtils;
import sbes.util.ReflectionUtils;

public class EvosuiteSecondStage extends Evosuite {
	
	private final String additionalClasspath;
	
	public EvosuiteSecondStage(String classSignature, String methodSignature, String additionalClasspath) {
		super(classSignature, methodSignature);
		this.additionalClasspath = additionalClasspath;
		this.outputDir = DirectoryUtils.I().getSecondStubEvosuiteDir();
	}
	
	@Override
	protected String getClassPath() {
		return Options.I().getClassesPath() + File.pathSeparatorChar + additionalClasspath;
	}
	
	@Override
	protected int getSearchBudget() {
		return Options.I().getCounterexampleBudget();
	}
	
	@Override
	protected Collection<String> getAdditionalParameters() {
		Collection<String> additional = new ArrayList<String>();
		if (Options.I().isAlternativeCounterexample()) {
			additional.add("-Dsbes_phase=3");
		}
		else {
			additional.add("-Dsbes_phase=2");
		}
		additional.add("-Dgeneric_primitives=true");
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
			InternalClassloader ic = new InternalClassloader(Options.I().getClassesPath() + File.pathSeparatorChar + DirectoryUtils.I().getSecondStubDir());
			c = Class.forName(classSignature, false, ic.getClassLoader());
			String method = "method_under_test";
			for (Method m : c.getMethods()) {
				// Simple case, we define only a method in the stub
				if (m.getName().equals(method)) {
					toReturn = ReflectionUtils.getBytecodeSignature(m);
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
