package sbes.evosuite;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import sbes.SBESException;
import sbes.option.Options;
import sbes.util.ClassUtils;
import sbes.util.DirectoryUtils;

public class EvosuiteTestScenario extends Evosuite {

	public EvosuiteTestScenario(String classSignature, String methodSignature) {
		super(classSignature, methodSignature);
		this.outputDir = DirectoryUtils.I().getTestScenarioDir();
	}
	
	@Override
	protected String getClassPath() {
		return Options.I().getClassesPath();
	}

	@Override
	protected String getTargetMethodSignature() {
		return getBytecodeSignature(classSignature, methodSignature);
	}
	
	@Override
	public int getSearchBudget() {
		return Options.I().getTestSearchBudget();
	}
	
	@Override
	protected Collection<String> getAdditionalParameters() {
		Collection<String> additional = new ArrayList<String>();
		additional.add("-generateSuite");
		additional.add("-Dshow_progress=false");
		additional.add("-Dcriterion=branch");
		additional.add("-Dtest_factory=RANDOM");
		additional.add("-Dlimit_search_to_target=true");
		return additional;
	}
	
	private String getBytecodeSignature(final String classname, final String methodname) {
		String toReturn = null;
		Class<?> c;
		try {
			c = Class.forName(classname, false, classLoader);
			String method = methodname.split("\\[")[0];
			String args[] = methodname.split("\\[")[1].replaceAll("\\]", "").split(",");
			if (args.length == 1) {
				args = args[0].equals("") ? new String[0] : args;
			}
			for (Method m : c.getDeclaredMethods()) {
				if (m.getName().equals(method) && m.getParameterTypes().length == args.length) {
					int i;
					for (i = 0; i < args.length; i++) {
						if (!m.getParameterTypes()[i].getCanonicalName().contains(args[i])) {
							break;
						}
					}
					if (i == args.length) {
						toReturn = ClassUtils.getBytecodeSignature(m);
						break;
					}
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("Unable to find class", e);
			throw new SBESException("Unable to find class");
		}
		
		if (toReturn == null) {
			throw new SBESException("Method not found: " + classname + "." + methodname);
		}
		
		return toReturn;
	}
	
}
